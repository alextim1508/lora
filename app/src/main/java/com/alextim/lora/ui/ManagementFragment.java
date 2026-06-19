package com.alextim.lora.ui;

import static com.alextim.lora.service.message.BleMessages.*;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.R;
import com.alextim.lora.client.ble.BluetoothService;
import com.alextim.lora.service.message.LoRaConfigurator;
import com.alextim.lora.service.message.LoRaConfigurator.LoRaModule;
import com.alextim.lora.service.message.LoRaConfigurator.ModuleConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementFragment extends Fragment {

    private static final String TAG = "ManagementFragment";

    private static final int COLOR_IDLE = Color.parseColor("#808080");
    private static final int COLOR_SENDING = Color.parseColor("#FFA500");
    private static final int COLOR_SUCCESS = Color.parseColor("#4CAF50");
    private static final int COLOR_ERROR = Color.parseColor("#F44336");
    private static final int COLOR_TIMEOUT = Color.parseColor("#FF5722");

    private BluetoothSetupActivity parentActivity;
    private BluetoothService bluetoothService;
    private boolean serviceBound = false;

    private Spinner activeDeviceSpinner;
    private ArrayAdapter<String> deviceSpinnerAdapter;
    private String currentActiveDeviceAddress = null;

    private Spinner loraTypeSpinner;
    private Spinner loraRateIndexSpinner;
    private Spinner loraPowerIndexSpinner;
    private Spinner loraChannelIndexSpinner;

    private Button sendGetVersionButton;
    private Button sendRestartButton;
    private Button sendGetConfigButton;
    private Button sendSetConfigButton;

    private TextView versionLabel;
    private TextView voltageLabel;
    private TextView temperatureLabel;
    private TextView responseCodeLabel;
    private TextView statusText;
    private ImageView statusIndicator;


    private final Map<LoRaModule, ModuleConfig> moduleConfigs = new HashMap<>();
    private final Map<Integer, Integer> rateCodeToSpinnerIndex = new HashMap<>();
    private final Map<Integer, Integer> powerCodeToSpinnerIndex = new HashMap<>();
    private final Map<Integer, Integer> channelCodeToSpinnerIndex = new HashMap<>();
    private final Map<Integer, Integer> spinnerIndexToRateCode = new HashMap<>();
    private final Map<Integer, Integer> spinnerIndexToPowerCode = new HashMap<>();
    private final Map<Integer, Integer> spinnerIndexToChannelCode = new HashMap<>();

    private LoRaModule currentModule = LoRaModule.LORA_UNKNOWN;

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private static final long RESPONSE_TIMEOUT_MS = 5_000;
    private Runnable timeoutRunnable;

    private String pendingCommandType = "";
    private long commandStartTime = 0;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String receivedDeviceAddress = intent.getStringExtra("device_address");

            Log.d(TAG, "onReceive action " + action + " from device: " + receivedDeviceAddress);

            if (receivedDeviceAddress != null && receivedDeviceAddress.equals(currentActiveDeviceAddress)) {
                Log.d(TAG, "Processing message for active device: " + receivedDeviceAddress);
                if ("ACTION_GET_CONFIG_RESPONSE".equals(action)) {
                    handleGetConfigResponse(intent);
                } else if ("ACTION_SET_CONFIG_RESPONSE".equals(action)) {
                    handleSetConfigResponse(intent);
                } else if ("ACTION_GET_VERSION_RESPONSE".equals(action)) {
                    handleGetVersionResponse(intent);
                } else if ("ACTION_STATUS_EVENT".equals(action)) {
                    handleStatusEvent(intent);
                } else {
                    Log.e(TAG, "Unknown message for active device: " + action);
                }
            } else {
                Log.d(TAG, "Ignoring message for inactive device: " + receivedDeviceAddress + ", Active: " + currentActiveDeviceAddress);
            }
        }

        private void handleGetConfigResponse(Intent intent) {
            clearPendingCommand();

            byte loraTypeByte = intent.getByteExtra("loraType", (byte) 0);
            byte powerIndexByte = intent.getByteExtra("powerIndex", (byte) 0);
            byte rateIndexByte = intent.getByteExtra("rateIndex", (byte) 0);
            byte channelIndexByte = intent.getByteExtra("channelIndex", (byte) 0);

            LoRaModule receivedModule = LoRaModule.fromCode(loraTypeByte);
            int powerCode = powerIndexByte & 0xFF;
            int rateCode = rateIndexByte & 0xFF;
            int channelCode = channelIndexByte & 0xFF;

            setLoRaModule(receivedModule);
            setPowerSelection(powerCode);
            setRateSelection(rateCode);
            setChannelSelection(channelCode);

            byte errorCode = intent.getByteExtra("errorCode", (byte) -1);
            updateResponseCodeLabel(errorCode);

            if (errorCode == 0) {
                setStatus("Config received", COLOR_SUCCESS);
            } else {
                setStatus("Config error", COLOR_ERROR);
            }
        }

        private void handleSetConfigResponse(Intent intent) {
            clearPendingCommand();

            byte errorCode = intent.getByteExtra("errorCode", (byte) -1);
            updateResponseCodeLabel(errorCode);

            if (errorCode == 0) {
                setStatus("Config set", COLOR_SUCCESS);
            } else {
                setStatus("Set config error", COLOR_ERROR);
            }
        }

        private void handleGetVersionResponse(Intent intent) {
            clearPendingCommand();

            String version = intent.getStringExtra("version");
            versionLabel.setText("Version: " + version);

            byte errorCode = intent.getByteExtra("errorCode", (byte) -1);
            updateResponseCodeLabel(errorCode);

            if (errorCode == 0) {
                setStatus("Version received", COLOR_SUCCESS);
            } else {
                setStatus("Version error", COLOR_ERROR);
            }
        }

        private void handleStatusEvent(Intent intent) {
            int power = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);

            voltageLabel.setText("Voltage: " + power + " V");
            temperatureLabel.setText("Temperature: " + temperature + " °C");
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BluetoothSetupActivity) {
            parentActivity = (BluetoothSetupActivity) context;
        }

        initModuleConfigs();
    }

    private void initModuleConfigs() {
        moduleConfigs.put(LoRaModule.LORA_XL1278, new LoRaConfigurator.XL1278Config());
        moduleConfigs.put(LoRaModule.LORA_E32, new LoRaConfigurator.E32Config());
        moduleConfigs.put(LoRaModule.LORA_E22, new LoRaConfigurator.E22Config());
        moduleConfigs.put(LoRaModule.LORA_E34, new LoRaConfigurator.E34Config());

        moduleConfigs.put(LoRaModule.LORA_UNKNOWN, new LoRaConfigurator.E32Config());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (parentActivity != null && parentActivity.serviceBound) {
            bluetoothService = parentActivity.bluetoothService;
            serviceBound = true;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_STATUS_EVENT");
        filter.addAction("ACTION_GET_CONFIG_RESPONSE");
        filter.addAction("ACTION_SET_CONFIG_RESPONSE");
        filter.addAction("ACTION_GET_VERSION_RESPONSE");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, filter);

        setStatus("Ready", COLOR_IDLE);

        // Обновляем список устройств и выбор при возобновлении
        updateDeviceSpinner();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_management, container, false);

        initViews(view);
        setupDeviceSpinner();
        setupSpinners();
        setupClickListeners();

        setLoRaModule(LoRaModule.LORA_UNKNOWN);

        return view;
    }

    private void initViews(View view) {
        activeDeviceSpinner = view.findViewById(R.id.activeDeviceSpinner);

        loraTypeSpinner = view.findViewById(R.id.loraTypeSpinner);
        loraPowerIndexSpinner = view.findViewById(R.id.loraPowerIndexSpinner);
        loraRateIndexSpinner = view.findViewById(R.id.loraRateIndexSpinner);
        loraChannelIndexSpinner = view.findViewById(R.id.loraChannelIndexSpinner);

        sendGetVersionButton = view.findViewById(R.id.sendGetVersionButton);
        sendRestartButton = view.findViewById(R.id.sendRestartButton);
        sendGetConfigButton = view.findViewById(R.id.sendGetConfigButton);
        sendSetConfigButton = view.findViewById(R.id.sendSetConfigButton);

        versionLabel = view.findViewById(R.id.versionLabel);
        voltageLabel = view.findViewById(R.id.voltageLabel);
        temperatureLabel = view.findViewById(R.id.temperatureLabel);
        responseCodeLabel = view.findViewById(R.id.responseCodeLabel);
        statusText = view.findViewById(R.id.statusText);
        statusIndicator = view.findViewById(R.id.statusIndicator);
    }

    private void setupDeviceSpinner() {
        deviceSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        deviceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activeDeviceSpinner.setAdapter(deviceSpinnerAdapter);

        activeDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAddress = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Selected device from spinner: " + selectedAddress);
                currentActiveDeviceAddress = selectedAddress;

                setStatus("Ready", COLOR_IDLE);
                responseCodeLabel.setText("Code: --");
                versionLabel.setText("Version: --");
                voltageLabel.setText("Voltage: -- V");
                temperatureLabel.setText("Temperature: -- °C");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentActiveDeviceAddress = null;
                setStatus("No device selected", COLOR_ERROR);
            }
        });
    }

    private void updateDeviceSpinner() {
        if (bluetoothService != null) {
            List<String> deviceNames = bluetoothService.getConnectedDeviceNames();
            Log.d(TAG, "Updating device spinner with " + deviceNames.size() + " devices.");
            deviceSpinnerAdapter.clear();
            deviceSpinnerAdapter.addAll(deviceNames);
            deviceSpinnerAdapter.notifyDataSetChanged();

            if (currentActiveDeviceAddress != null && !deviceNames.contains(currentActiveDeviceAddress)) {
                Log.d(TAG, "Previously selected device " + currentActiveDeviceAddress + " is no longer connected. Resetting selection.");
                currentActiveDeviceAddress = null;
                activeDeviceSpinner.setSelection(-1);
                setStatus("Selected device disconnected", COLOR_ERROR);
            }

            if (currentActiveDeviceAddress == null && !deviceNames.isEmpty()) {
                activeDeviceSpinner.setSelection(0);
            }
        } else {
            Log.d(TAG, "Cannot update device spinner, bluetoothService is null.");
            deviceSpinnerAdapter.clear();
            deviceSpinnerAdapter.notifyDataSetChanged();
            currentActiveDeviceAddress = null;
            setStatus("Service not bound", COLOR_ERROR);
        }
    }

    private void setupSpinners() {
        List<String> loraTypes = new ArrayList<>();
        for (LoRaModule module : LoRaModule.values()) {
            loraTypes.add(module.name());
        }

        ArrayAdapter<String> loraTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, loraTypes);
        loraTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraTypeSpinner.setAdapter(loraTypeAdapter);


        ArrayAdapter<String> loraPowerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        loraPowerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraPowerIndexSpinner.setAdapter(loraPowerAdapter);

        ArrayAdapter<String> loraRateAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        loraRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraRateIndexSpinner.setAdapter(loraRateAdapter);

        ArrayAdapter<String> loraChannelAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        loraChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraChannelIndexSpinner.setAdapter(loraChannelAdapter);

        loraTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LoRaModule selectedModule = LoRaModule.values()[position];
                if (selectedModule != currentModule) {
                    setLoRaModule(selectedModule);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setLoRaModule(LoRaModule module) {
        currentModule = module;

        int position = module.ordinal();
        if (position < loraTypeSpinner.getCount()) {
            loraTypeSpinner.setSelection(position);
        }

        ModuleConfig config = moduleConfigs.get(module);
        if (config == null) {
            config = moduleConfigs.get(LoRaModule.LORA_UNKNOWN);
        }

        updatePowerSpinner(config);
        updateRateSpinner(config);
        updateChannelSpinner(config);
    }


    private void updatePowerSpinner(ModuleConfig config) {
        powerCodeToSpinnerIndex.clear();
        spinnerIndexToPowerCode.clear();

        int[] availablePowers = config.getAvailablePowers();
        List<String> powerDescriptions = new ArrayList<>();

        for (int i = 0; i < availablePowers.length; i++) {
            int powerCode = availablePowers[i];
            String description = config.getPowerDescription(powerCode);
            powerDescriptions.add(description);

            powerCodeToSpinnerIndex.put(powerCode, i);
            spinnerIndexToPowerCode.put(i, powerCode);
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) loraPowerIndexSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(powerDescriptions);
        adapter.notifyDataSetChanged();

        if (!powerDescriptions.isEmpty()) {
            loraPowerIndexSpinner.setSelection(0);
        }
    }


    private void updateRateSpinner(ModuleConfig config) {
        rateCodeToSpinnerIndex.clear();
        spinnerIndexToRateCode.clear();

        int[] availableRates = config.getAvailableRates();
        List<String> rateDescriptions = new ArrayList<>();

        for (int i = 0; i < availableRates.length; i++) {
            int rateCode = availableRates[i];
            String description = config.getRateDescription(rateCode);
            rateDescriptions.add(description);

            rateCodeToSpinnerIndex.put(rateCode, i);
            spinnerIndexToRateCode.put(i, rateCode);
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) loraRateIndexSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(rateDescriptions);
        adapter.notifyDataSetChanged();

        if (!rateDescriptions.isEmpty()) {
            loraRateIndexSpinner.setSelection(0);
        }
    }

    private void updateChannelSpinner(ModuleConfig config) {
        channelCodeToSpinnerIndex.clear();
        spinnerIndexToChannelCode.clear();

        int[] availableChannels = config.getAvailableChannels();
        List<String> channelDescriptions = new ArrayList<>();

        for (int i = 0; i < availableChannels.length; i++) {
            int channelCode = availableChannels[i];
            String description = config.getChannelDescription(channelCode);
            channelDescriptions.add(description);

            channelCodeToSpinnerIndex.put(channelCode, i);
            spinnerIndexToChannelCode.put(i, channelCode);
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) loraChannelIndexSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(channelDescriptions);
        adapter.notifyDataSetChanged();

        if (!channelDescriptions.isEmpty()) {
            loraChannelIndexSpinner.setSelection(0);
        }
    }


    private void setPowerSelection(int powerCode) {
        Integer spinnerIndex = powerCodeToSpinnerIndex.get(powerCode);
        if (spinnerIndex != null && spinnerIndex < loraPowerIndexSpinner.getCount()) {
            loraPowerIndexSpinner.setSelection(spinnerIndex);
        } else {
            if (loraPowerIndexSpinner.getCount() > 0) {
                loraPowerIndexSpinner.setSelection(0);
            }
        }
    }

    private void setRateSelection(int rateCode) {
        Integer spinnerIndex = rateCodeToSpinnerIndex.get(rateCode);
        if (spinnerIndex != null && spinnerIndex < loraRateIndexSpinner.getCount()) {
            loraRateIndexSpinner.setSelection(spinnerIndex);
        } else {
            if (loraRateIndexSpinner.getCount() > 0) {
                loraRateIndexSpinner.setSelection(0);
            }
        }
    }

    private void setChannelSelection(int channelCode) {
        Integer spinnerIndex = channelCodeToSpinnerIndex.get(channelCode);
        if (spinnerIndex != null && spinnerIndex < loraChannelIndexSpinner.getCount()) {
            loraChannelIndexSpinner.setSelection(spinnerIndex);
        } else {
            if (loraChannelIndexSpinner.getCount() > 0) {
                loraChannelIndexSpinner.setSelection(0);
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupClickListeners() {
        sendGetVersionButton.setOnClickListener(v -> {
            if (serviceBound && bluetoothService != null && currentActiveDeviceAddress != null) {

                bluetoothService.sendMessage(new GetVersionCommand(), currentActiveDeviceAddress);

                Toast.makeText(getContext(), "CMD_GET_VERSION sent to " + currentActiveDeviceAddress, Toast.LENGTH_SHORT).show();
                setStatus("Getting version...", COLOR_SENDING);
                setPendingCommand("GET_VERSION");
            } else {
                String errorMsg = "Service not connected or no device selected";
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                setStatus(errorMsg, COLOR_ERROR);
            }
        });

        sendRestartButton.setOnClickListener(v -> {
            if (serviceBound && bluetoothService != null && currentActiveDeviceAddress != null) {

                bluetoothService.sendMessage(new RestartCommand(), currentActiveDeviceAddress);

                Toast.makeText(getContext(), "CMD_RESTART sent to " + currentActiveDeviceAddress, Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = "Service not connected or no device selected";
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                setStatus(errorMsg, COLOR_ERROR);
            }
        });

        sendGetConfigButton.setOnClickListener(v -> {
            if (serviceBound && bluetoothService != null && currentActiveDeviceAddress != null) {

                bluetoothService.sendMessage(new GetConfigurationCommand(), currentActiveDeviceAddress);

                Toast.makeText(getContext(), "CMD_GET_CONFIGURATION sent to " + currentActiveDeviceAddress, Toast.LENGTH_SHORT).show();
                setStatus("Getting config...", COLOR_SENDING);
                setPendingCommand("GET_CONFIG");
            } else {
                String errorMsg = "Service not connected or no device selected";
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                setStatus(errorMsg, COLOR_ERROR);
            }
        });

        sendSetConfigButton.setOnClickListener(v -> {
            if (serviceBound && bluetoothService != null && currentActiveDeviceAddress != null) {
                int modulePosition = loraTypeSpinner.getSelectedItemPosition();
                LoRaModule selectedModule = LoRaModule.values()[modulePosition];

                int powerSpinnerIndex = loraPowerIndexSpinner.getSelectedItemPosition();
                int rateSpinnerIndex = loraRateIndexSpinner.getSelectedItemPosition();
                int channelSpinnerIndex = loraChannelIndexSpinner.getSelectedItemPosition();

                int powerCode = spinnerIndexToPowerCode.getOrDefault(powerSpinnerIndex, 0);
                int rateCode = spinnerIndexToRateCode.getOrDefault(rateSpinnerIndex, 0);
                int channelCode = spinnerIndexToChannelCode.getOrDefault(channelSpinnerIndex, 0);


                bluetoothService.sendMessage(new SetConfigurationCommand(
                        (byte) selectedModule.getCode(),
                        (byte) powerCode,
                        (byte) rateCode,
                        (byte) channelCode), currentActiveDeviceAddress);

                Toast.makeText(getContext(), "CMD_SET_CONFIGURATION sent to " + currentActiveDeviceAddress, Toast.LENGTH_SHORT).show();
                setStatus("Setting config...", COLOR_SENDING);
                setPendingCommand("SET_CONFIG");
            } else {
                String errorMsg = "Service not connected or no device selected";
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                setStatus(errorMsg, COLOR_ERROR);
            }
        });
    }

    private void setStatus(String text, int color) {
        if (statusText != null) {
            statusText.setText(text);
        }

        GradientDrawable drawable = (GradientDrawable) statusIndicator.getBackground();
        if (drawable != null) {
            drawable.setColor(color);
        } else {
            drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setStroke(2, Color.BLACK);
            statusIndicator.setBackground(drawable);
        }
    }

    private void setPendingCommand(String commandType) {
        clearPendingCommand();

        pendingCommandType = commandType;
        commandStartTime = System.currentTimeMillis();

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (!pendingCommandType.isEmpty() && currentActiveDeviceAddress != null) { // Проверяем адрес
                    long elapsedTime = System.currentTimeMillis() - commandStartTime;
                    Log.d(TAG, "Timeout for command: " + pendingCommandType + " to device " + currentActiveDeviceAddress + " after " + elapsedTime + "ms");

                    setStatus("Timeout: " + pendingCommandType, COLOR_TIMEOUT);
                    pendingCommandType = "";
                }
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, RESPONSE_TIMEOUT_MS);
    }

    private void clearPendingCommand() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
        pendingCommandType = "";
        commandStartTime = 0;
    }

    private void updateResponseCodeLabel(byte errorCode) {
        String text = "Code: " + String.format("0x%02X", errorCode);
        responseCodeLabel.setText(text);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);
        clearPendingCommand();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearPendingCommand();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}