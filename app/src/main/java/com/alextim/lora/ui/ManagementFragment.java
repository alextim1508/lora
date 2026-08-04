package com.alextim.lora.ui;

import static com.alextim.lora.service.constants.LoraActions.ACTION_GET_CONFIG_RESPONSE;
import static com.alextim.lora.service.constants.LoraActions.ACTION_GET_VERSION_RESPONSE;
import static com.alextim.lora.service.constants.LoraActions.ACTION_SET_CONFIG_RESPONSE;
import static com.alextim.lora.service.constants.LoraActions.ACTION_STATUS_EVENT;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_CHANNEL_INDEX;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_DEVICE_ADDRESS;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_ERROR_CODE;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_LORA_NAME;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_POWER_INDEX;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_RATE_INDEX;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_TEMPERATURE;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_VERSION;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_VOLTAGE;
import static com.alextim.lora.service.message.BleMessages.GetConfigurationCommand;
import static com.alextim.lora.service.message.BleMessages.GetVersionCommand;
import static com.alextim.lora.service.message.BleMessages.RestartCommand;
import static com.alextim.lora.service.message.BleMessages.SetConfigurationCommand;
import static com.alextim.lora.service.protocol.ErrorCode.findTitleByCode;
import static com.alextim.lora.ui.util.StatusColor.COLOR_ERROR;
import static com.alextim.lora.ui.util.StatusColor.COLOR_IDLE;
import static com.alextim.lora.ui.util.StatusColor.COLOR_SENDING;
import static com.alextim.lora.ui.util.StatusColor.COLOR_SUCCESS;
import static com.alextim.lora.ui.util.StatusColor.COLOR_TIMEOUT;

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
import com.alextim.lora.service.lora.LoraConfig;
import com.alextim.lora.service.lora.ModuleConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ManagementFragment extends Fragment {

    private static final String TAG = "ManagementFragment";
    private static final byte DEFAULT_CONFIG_VERSION = 0x01;

    private MainActivity parentActivity;
    private BluetoothService bluetoothService;
    private boolean serviceBound = false;

    private Spinner activeDeviceSpinner;
    private ArrayAdapter<String> deviceSpinnerAdapter;
    private String currentActiveDeviceAddress = null;

    private TextView loraNameDisplay;
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

    private final LoraConfig loraConfig = new LoraConfig();

    private final Map<String, DeviceManagementState> deviceStateMap = new ConcurrentHashMap<>();

    private static class DeviceManagementState {
        String receiverLoraName;

        final Map<Integer, Integer> rateCodeToSpinnerIndex = new HashMap<>();
        final Map<Integer, Integer> powerCodeToSpinnerIndex = new HashMap<>();
        final Map<Integer, Integer> channelCodeToSpinnerIndex = new HashMap<>();
        final Map<Integer, Integer> spinnerIndexToRateCode = new HashMap<>();
        final Map<Integer, Integer> spinnerIndexToPowerCode = new HashMap<>();
        final Map<Integer, Integer> spinnerIndexToChannelCode = new HashMap<>();

        Runnable timeoutRunnable;
        String pendingCommandType = "";
        long commandStartTime = 0;
    }

    private DeviceManagementState activeDeviceState() {
        return currentActiveDeviceAddress != null
                ? deviceStateMap.computeIfAbsent(currentActiveDeviceAddress, k -> new DeviceManagementState())
                : null;
    }

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private static final long RESPONSE_TIMEOUT_MS = 5_000;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String receivedDeviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

            Log.d(TAG, "onReceive action " + action + " from device: " + receivedDeviceAddress);

            if (receivedDeviceAddress == null) {
                Log.w(TAG, "Received message without device address: " + action);
                return;
            }

            DeviceManagementState state = deviceStateMap.computeIfAbsent(receivedDeviceAddress, k -> new DeviceManagementState());
            boolean isActive = receivedDeviceAddress.equals(currentActiveDeviceAddress);

            if (ACTION_GET_CONFIG_RESPONSE.equals(action)) {
                handleGetConfigResponse(intent, state, isActive);
            } else if (ACTION_SET_CONFIG_RESPONSE.equals(action)) {
                handleSetConfigResponse(intent, state, isActive);
            } else if (ACTION_GET_VERSION_RESPONSE.equals(action)) {
                handleGetVersionResponse(intent, state, isActive);
            } else if (ACTION_STATUS_EVENT.equals(action)) {
                handleStatusEvent(intent, isActive);
            } else {
                Log.e(TAG, "Unknown message: " + action);
            }
        }
    };

    private void handleGetConfigResponse(Intent intent, DeviceManagementState state, boolean isActive) {
        clearPendingCommand(state);

        byte version = intent.getByteExtra(EXTRA_VERSION, (byte) 0);
        String loraName = intent.getStringExtra(EXTRA_LORA_NAME);
        byte powerIndexByte = intent.getByteExtra(EXTRA_POWER_INDEX, (byte) 0);
        byte rateIndexByte = intent.getByteExtra(EXTRA_RATE_INDEX, (byte) 0);
        byte channelIndexByte = intent.getByteExtra(EXTRA_CHANNEL_INDEX, (byte) 0);
        byte errorCode = intent.getByteExtra(EXTRA_ERROR_CODE, (byte) -1);

        Log.d(TAG, "handleGetConfigResponse: Received - loraName='" + loraName + "', version=" + version +
                ", powerIndex=" + powerIndexByte +
                ", rateIndex=" + rateIndexByte + ", channelIndex=" + channelIndexByte);

        ModuleConfig receivedConfig = null;
        if (loraName != null) {
            state.receiverLoraName = loraName;
            receivedConfig = loraConfig.findByTitle(loraName);
            if (receivedConfig == null) {
                Log.w(TAG, "handleGetConfigResponse: Received unknown config key: " + loraName);
                if (isActive) {
                    Toast.makeText(getContext(), "Получена неизвестная конфигурация LoRa: " + loraName, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.w(TAG, "handleGetConfigResponse: loraName is null, falling back to default.");
            if (isActive) {
                Toast.makeText(getContext(), "Получена пустая конфигурация LoRa", Toast.LENGTH_SHORT).show();
            }
        }

        int powerCode = powerIndexByte & 0xFF;
        int rateCode = rateIndexByte & 0xFF;
        int channelCode = channelIndexByte & 0xFF;

        if (!isActive) {
            return;
        }

        setConfig(state, receivedConfig);
        setPowerSelection(state, powerCode);
        setRateSelection(state, rateCode);
        setChannelSelection(state, channelCode);

        Log.d(TAG, "handleGetConfigResponse: Response errorCode=" + errorCode);
        updateResponseCodeLabel(errorCode);

        if (errorCode == 0) {
            setStatus("Конфигурация получена", COLOR_SUCCESS);
        } else {
            setStatus("Ошибка конфигурации", COLOR_ERROR);
        }
    }

    private void handleSetConfigResponse(Intent intent, DeviceManagementState state, boolean isActive) {
        clearPendingCommand(state);

        if (!isActive) {
            return;
        }

        byte errorCode = intent.getByteExtra(EXTRA_ERROR_CODE, (byte) -1);
        updateResponseCodeLabel(errorCode);

        if (errorCode == 0) {
            setStatus("Конфигурация установлена", COLOR_SUCCESS);
        } else {
            setStatus("Ошибка установки конфигурации", COLOR_ERROR);
        }
    }

    private void handleGetVersionResponse(Intent intent, DeviceManagementState state, boolean isActive) {
        clearPendingCommand(state);

        if (!isActive) {
            return;
        }

        String version = intent.getStringExtra(EXTRA_VERSION);
        versionLabel.setText("Версия: " + version);

        byte errorCode = intent.getByteExtra(EXTRA_ERROR_CODE, (byte) -1);
        updateResponseCodeLabel(errorCode);

        if (errorCode == 0) {
            setStatus("Версия получена", COLOR_SUCCESS);
        } else {
            setStatus("Ошибка получения версии", COLOR_ERROR);
        }
    }

    private void handleStatusEvent(Intent intent, boolean isActive) {
        if (!isActive) {
            return;
        }

        int voltage = intent.getIntExtra(EXTRA_VOLTAGE, 0);
        int temperature = intent.getIntExtra(EXTRA_TEMPERATURE, 0);

        voltageLabel.setText("Напряжение: " + voltage/1_000. + " В");
        temperatureLabel.setText("Температура: " + temperature/10.0 + " °C");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            parentActivity = (MainActivity) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (parentActivity != null && parentActivity.serviceBound) {
            bluetoothService = parentActivity.bluetoothService;
            serviceBound = true;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATUS_EVENT);
        filter.addAction(ACTION_GET_CONFIG_RESPONSE);
        filter.addAction(ACTION_SET_CONFIG_RESPONSE);
        filter.addAction(ACTION_GET_VERSION_RESPONSE);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, filter);

        setStatus("Готово", COLOR_IDLE);

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

        return view;
    }

    private void initViews(View view) {
        activeDeviceSpinner = view.findViewById(R.id.activeDeviceSpinner);

        loraNameDisplay = view.findViewById(R.id.loraNameDisplay);
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
        deviceSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        deviceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activeDeviceSpinner.setAdapter(deviceSpinnerAdapter);

        activeDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAddress = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Selected device from spinner: " + selectedAddress);
                currentActiveDeviceAddress = selectedAddress;

                refreshUiFromDeviceState(activeDeviceState());
                setStatus("Готово", COLOR_IDLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentActiveDeviceAddress = null;
                setStatus("Устройство не выбрано", COLOR_ERROR);
            }
        });
    }

    private void refreshUiFromDeviceState(DeviceManagementState state) {
        responseCodeLabel.setText("Код: --");
        versionLabel.setText("Версия: --");
        voltageLabel.setText("Напряжение: -- В");
        temperatureLabel.setText("Температура: -- °C");

        ModuleConfig config = state != null && state.receiverLoraName != null
                ? loraConfig.findByTitle(state.receiverLoraName)
                : null;
        setConfig(state, config);
    }

    private void updateDeviceSpinner() {
        if (bluetoothService != null) {
            List<String> deviceAddresses = bluetoothService.getConnectedDeviceAddresses();
            Log.d(TAG, "Updating device spinner with " + deviceAddresses.size() + " devices.");
            deviceSpinnerAdapter.clear();
            deviceSpinnerAdapter.addAll(deviceAddresses);
            deviceSpinnerAdapter.notifyDataSetChanged();

            if (currentActiveDeviceAddress != null && !deviceAddresses.contains(currentActiveDeviceAddress)) {
                Log.d(TAG, "Previously selected device " + currentActiveDeviceAddress + " is no longer connected. Resetting selection.");
                currentActiveDeviceAddress = null;
                activeDeviceSpinner.setSelection(-1);
                setStatus("Выбранное устройство отключено", COLOR_ERROR);
            }

            if (currentActiveDeviceAddress == null && !deviceAddresses.isEmpty()) {
                activeDeviceSpinner.setSelection(0);
            }
        } else {
            Log.d(TAG, "Cannot update device spinner, bluetoothService is null.");
            deviceSpinnerAdapter.clear();
            deviceSpinnerAdapter.notifyDataSetChanged();
            currentActiveDeviceAddress = null;
            setStatus("Сервис не подключен", COLOR_ERROR);
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> powerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        powerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraPowerIndexSpinner.setAdapter(powerAdapter);

        ArrayAdapter<String> rateAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraRateIndexSpinner.setAdapter(rateAdapter);

        ArrayAdapter<String> channelAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loraChannelIndexSpinner.setAdapter(channelAdapter);
    }

    private void setConfig(DeviceManagementState state, ModuleConfig config) {
        loraNameDisplay.setText(config != null ? config.getTitle() : "");
        updatePowerSpinner(state, config);
        updateRateSpinner(state, config);
        updateChannelSpinner(state, config);
        Log.d(TAG, "Configuration set to: " + config);
    }

    private void updatePowerSpinner(DeviceManagementState state, ModuleConfig config) {
        state.powerCodeToSpinnerIndex.clear();
        state.spinnerIndexToPowerCode.clear();

        List<String> powerDescriptions = new ArrayList<>();
        if (config != null) {
            int[] availablePowers = config.getAvailablePowers();
            for (int i = 0; i < availablePowers.length; i++) {
                int powerCode = availablePowers[i];
                String description = config.getPowerDescription(powerCode);
                powerDescriptions.add(description);

                state.powerCodeToSpinnerIndex.put(powerCode, i);
                state.spinnerIndexToPowerCode.put(i, powerCode);
            }
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) loraPowerIndexSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(powerDescriptions);
        adapter.notifyDataSetChanged();

        if (!powerDescriptions.isEmpty()) {
            loraPowerIndexSpinner.setSelection(0);
        }
    }

    private void updateRateSpinner(DeviceManagementState state, ModuleConfig config) {
        state.rateCodeToSpinnerIndex.clear();
        state.spinnerIndexToRateCode.clear();

        List<String> rateDescriptions = new ArrayList<>();
        if (config != null) {
            int[] availableRates = config.getAvailableRates();
            for (int i = 0; i < availableRates.length; i++) {
                int rateCode = availableRates[i];
                String description = config.getRateDescription(rateCode);
                rateDescriptions.add(description);

                state.rateCodeToSpinnerIndex.put(rateCode, i);
                state.spinnerIndexToRateCode.put(i, rateCode);
            }
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) loraRateIndexSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(rateDescriptions);
        adapter.notifyDataSetChanged();

        if (!rateDescriptions.isEmpty()) {
            loraRateIndexSpinner.setSelection(0);
        }
    }

    private void updateChannelSpinner(DeviceManagementState state, ModuleConfig config) {
        state.channelCodeToSpinnerIndex.clear();
        state.spinnerIndexToChannelCode.clear();

        List<String> channelDescriptions = new ArrayList<>();
        if (config != null) {
            int[] availableChannels = config.getAvailableChannels();
            if (availableChannels != null) {
                for (int i = 0; i < availableChannels.length; i++) {
                    int channelCode = availableChannels[i];
                    String description = config.getChannelDescription(channelCode);
                    channelDescriptions.add(description);

                    state.channelCodeToSpinnerIndex.put(channelCode, i);
                    state.spinnerIndexToChannelCode.put(i, channelCode);
                }
            }
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) loraChannelIndexSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(channelDescriptions);
        adapter.notifyDataSetChanged();

        if (!channelDescriptions.isEmpty()) {
            loraChannelIndexSpinner.setSelection(0);
        }
    }

    private void setPowerSelection(DeviceManagementState state, int powerCode) {
        Integer spinnerIndex = state.powerCodeToSpinnerIndex.get(powerCode);
        if (spinnerIndex != null && spinnerIndex < loraPowerIndexSpinner.getCount()) {
            loraPowerIndexSpinner.setSelection(spinnerIndex);
        } else if (loraPowerIndexSpinner.getCount() > 0) {
            loraPowerIndexSpinner.setSelection(0);
        }
    }

    private void setRateSelection(DeviceManagementState state, int rateCode) {
        Integer spinnerIndex = state.rateCodeToSpinnerIndex.get(rateCode);
        if (spinnerIndex != null && spinnerIndex < loraRateIndexSpinner.getCount()) {
            loraRateIndexSpinner.setSelection(spinnerIndex);
        } else if (loraRateIndexSpinner.getCount() > 0) {
            loraRateIndexSpinner.setSelection(0);
        }
    }

    private void setChannelSelection(DeviceManagementState state, int channelCode) {
        Integer spinnerIndex = state.channelCodeToSpinnerIndex.get(channelCode);
        if (spinnerIndex != null && spinnerIndex < loraChannelIndexSpinner.getCount()) {
            loraChannelIndexSpinner.setSelection(spinnerIndex);
        } else if (loraChannelIndexSpinner.getCount() > 0) {
            loraChannelIndexSpinner.setSelection(0);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupClickListeners() {
        sendGetVersionButton.setOnClickListener(v -> {
            if (isActiveDeviceReady()) {
                bluetoothService.sendMessage(new GetVersionCommand(), currentActiveDeviceAddress);

                setStatus("Получение версии...", COLOR_SENDING);
                setPendingCommand(activeDeviceState(), "GET_VERSION");
            } else {
                showNotReadyError();
            }
        });

        sendRestartButton.setOnClickListener(v -> {
            if (isActiveDeviceReady()) {
                bluetoothService.sendMessage(new RestartCommand(), currentActiveDeviceAddress);
            } else {
                showNotReadyError();
            }
        });

        sendGetConfigButton.setOnClickListener(v -> {
            if (isActiveDeviceReady()) {
                bluetoothService.sendMessage(new GetConfigurationCommand(), currentActiveDeviceAddress);

                setStatus("Получение конфигурации...", COLOR_SENDING);
                setPendingCommand(activeDeviceState(), "GET_CONFIG");
            } else {
                showNotReadyError();
            }
        });

        sendSetConfigButton.setOnClickListener(v -> {
            if (isActiveDeviceReady()) {
                DeviceManagementState state = activeDeviceState();

                if (state.receiverLoraName == null) {
                    Toast.makeText(getContext(), "Сначала прочитайте конфигурацию с устройства (Прочитать конфиг-ю)", Toast.LENGTH_SHORT).show();
                    setStatus("Имя LoRa неизвестно", COLOR_ERROR);
                    return;
                }

                int powerSpinnerIndex = loraPowerIndexSpinner.getSelectedItemPosition();
                int rateSpinnerIndex = loraRateIndexSpinner.getSelectedItemPosition();
                int channelSpinnerIndex = loraChannelIndexSpinner.getSelectedItemPosition();

                int powerCode = state.spinnerIndexToPowerCode.getOrDefault(powerSpinnerIndex, 0);
                int rateCode = state.spinnerIndexToRateCode.getOrDefault(rateSpinnerIndex, 0);
                int channelCode = state.spinnerIndexToChannelCode.getOrDefault(channelSpinnerIndex, 0);

                bluetoothService.sendMessage(new SetConfigurationCommand(
                        state.receiverLoraName,
                        DEFAULT_CONFIG_VERSION,
                        (byte) powerCode,
                        (byte) rateCode,
                        (byte) channelCode), currentActiveDeviceAddress);

                setStatus("Установка конфигурации...", COLOR_SENDING);
                setPendingCommand(state, "SET_CONFIG");

                Log.d(TAG, "Setting config to: " + state.receiverLoraName);
            } else {
                showNotReadyError();
            }
        });
    }

    private boolean isActiveDeviceReady() {
        return serviceBound && bluetoothService != null && currentActiveDeviceAddress != null
                && bluetoothService.isReadyToSendMessage(currentActiveDeviceAddress);
    }

    private void showNotReadyError() {
        String msg;
        if (!serviceBound || bluetoothService == null) {
            msg = "Сервис не подключен";
        } else if (currentActiveDeviceAddress == null) {
            msg = "Устройство не выбрано";
        } else if (!bluetoothService.isConnectedToDevice(currentActiveDeviceAddress)) {
            msg = "Устройство не подключено";
        } else {
            msg = "Устройство еще не готово (поиск сервисов...)";
        }
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        setStatus(msg, COLOR_ERROR);
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

    private void setPendingCommand(DeviceManagementState state, String commandType) {
        if (state == null)
            return;

        clearPendingCommand(state);

        state.pendingCommandType = commandType;
        state.commandStartTime = System.currentTimeMillis();

        Runnable timeoutRunnable = () -> {
            if (!state.pendingCommandType.isEmpty()) {
                long elapsedTime = System.currentTimeMillis() - state.commandStartTime;
                Log.d(TAG, "Timeout for command: " + state.pendingCommandType + " after " + elapsedTime + "ms");

                if (state == activeDeviceState()) {
                    setStatus("Тайм-аут: " + state.pendingCommandType, COLOR_TIMEOUT);
                }
                state.pendingCommandType = "";
            }
        };
        state.timeoutRunnable = timeoutRunnable;

        timeoutHandler.postDelayed(timeoutRunnable, RESPONSE_TIMEOUT_MS);
    }

    private void clearPendingCommand(DeviceManagementState state) {
        if (state.timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(state.timeoutRunnable);
            state.timeoutRunnable = null;
        }
        state.pendingCommandType = "";
        state.commandStartTime = 0;
    }

    private void updateResponseCodeLabel(byte errorCode) {
        responseCodeLabel.setText("Код: " + findTitleByCode(errorCode));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);

        for (DeviceManagementState state : deviceStateMap.values()) {
            clearPendingCommand(state);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}
