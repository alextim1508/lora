package com.alextim.lora.ui;

import static com.alextim.lora.client.ble.BleMessage.bytesToHex;
import static com.alextim.lora.service.constants.LoraActions.ACTION_GENERATE_DATA_EVENT;
import static com.alextim.lora.service.constants.LoraActions.ACTION_GET_CONFIG_RESPONSE;
import static com.alextim.lora.service.constants.LoraActions.ACTION_GET_DEVICE_CONFIG_RESPONSE;
import static com.alextim.lora.service.constants.LoraActions.ACTION_RECEIVE_DATA_EVENT;
import static com.alextim.lora.service.constants.LoraActions.ACTION_SET_DEVICE_CONFIG_RESPONSE;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_CONFIG_BYTE;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_DEVICE_ADDRESS;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_ERROR_CODE;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_LORA_NAME;
import static com.alextim.lora.service.constants.LoraExtras.EXTRA_PAYLOAD;
import static com.alextim.lora.service.protocol.ErrorCode.findTitleByCode;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.R;
import com.alextim.lora.client.ble.BluetoothService;
import com.alextim.lora.service.message.BleMessages;
import com.alextim.lora.service.protocol.ConfigByteUtils;
import com.alextim.lora.ui.util.MessageCountChartView;
import com.alextim.lora.ui.util.MultiDeviceMessageChartView;
import com.alextim.lora.ui.util.StatusColor;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataFragment extends Fragment {
    private static final String TAG = "DataFragment";

    private static final long STATS_UPDATE_INTERVAL_MS = 1_000;
    private static final long RESPONSE_TIMEOUT_MS = 5_000;
    private static final long RECENT_STATS_WINDOW_MS = 10_000;

    private static final int CHART_WINDOW_SECONDS = 60;

    private static final long TIMESTAMP_RETENTION_MS = (CHART_WINDOW_SECONDS + 2) * 1000L;

    private MainActivity parentActivity;
    private BluetoothService bluetoothService;
    private boolean serviceBound = false;

    private Spinner activeDeviceSpinner;
    private ArrayAdapter<String> deviceSpinnerAdapter;
    private String currentActiveDeviceAddress = null;

    private TextView statusText;
    private ImageView statusIndicator;
    private TextView responseCodeLabel;

    private Spinner deviceTypeSpinner;
    private Spinner echoModeSpinner;
    private EditText packageLengthEditText;
    private Button readConfigButton;
    private Button writeConfigButton;

    // Остальные элементы
    private Button resetStatsButton;

    private LinearLayout devicesInfoContainer;
    private TextView devicesInfoPlaceholder;

    private MultiDeviceMessageChartView allDevicesMessageChart;
    private TextView allDevicesChartPlaceholder;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable statsUpdateTask;

    private final Map<String, DeviceData> deviceDataMap = new ConcurrentHashMap<>();

    private static class DeviceData {
        boolean isTransmitter = false;
        boolean isEchoMode = false;
        int packetLength = 10;

        long sentCount = 0;
        long receivedCount = 0;

        final Deque<Long> sentTimestamps = new ArrayDeque<>();
        final Deque<Long> receivedTimestamps = new ArrayDeque<>();

        Integer lastRssi = null;
        String lastMessageText = null;

        String loraName = null;

        Runnable timeoutRunnable;
        String pendingCommandType = "";
        long commandStartTime = 0;

        void recordSent() {
            sentCount++;
            sentTimestamps.addLast(System.currentTimeMillis());
            pruneOld(sentTimestamps);
        }

        void recordReceived() {
            receivedCount++;
            receivedTimestamps.addLast(System.currentTimeMillis());
            pruneOld(receivedTimestamps);
        }

        int recentSentCount() {
            pruneOld(sentTimestamps);
            return countWithinWindow(sentTimestamps, RECENT_STATS_WINDOW_MS);
        }

        int recentReceivedCount() {
            pruneOld(receivedTimestamps);
            return countWithinWindow(receivedTimestamps, RECENT_STATS_WINDOW_MS);
        }

        private static int countWithinWindow(Deque<Long> timestamps, long windowMs) {
            long cutoff = System.currentTimeMillis() - windowMs;
            int count = 0;
            for (Long ts : timestamps) {
                if (ts >= cutoff) {
                    count++;
                }
            }
            return count;
        }

        private static int[] bucketize(Deque<Long> timestamps) {
            long lastCompleteSecond = System.currentTimeMillis() / 1000L - 1;
            int[] buckets = new int[CHART_WINDOW_SECONDS];
            for (Long ts : timestamps) {
                long tsSecond = ts / 1000L;
                long secondsAgo = lastCompleteSecond - tsSecond;
                if (secondsAgo < 0 || secondsAgo >= CHART_WINDOW_SECONDS) {
                    continue;
                }
                int bucketFromEnd = (int) secondsAgo;
                int index = CHART_WINDOW_SECONDS - 1 - bucketFromEnd;
                if (index >= 0 && index < CHART_WINDOW_SECONDS) {
                    buckets[index]++;
                }
            }
            return buckets;
        }

        int[] sentHistoryBuckets() {
            pruneOld(sentTimestamps);
            return bucketize(sentTimestamps);
        }

        int[] receivedHistoryBuckets() {
            pruneOld(receivedTimestamps);
            return bucketize(receivedTimestamps);
        }

        private static void pruneOld(Deque<Long> timestamps) {
            long cutoff = System.currentTimeMillis() - TIMESTAMP_RETENTION_MS;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }
        }
    }

    private DeviceData activeDeviceData() {
        return currentActiveDeviceAddress != null
                ? deviceDataMap.computeIfAbsent(currentActiveDeviceAddress, k -> new DeviceData())
                : null;
    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String receivedDeviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
            Log.d(TAG, "onReceive action " + action + " from device: " + receivedDeviceAddress);

            if (receivedDeviceAddress == null) {
                Log.w(TAG, "Received message without device address: " + action);
                return;
            }

            DeviceData deviceData = deviceDataMap.computeIfAbsent(receivedDeviceAddress, k -> new DeviceData());
            boolean isActive = receivedDeviceAddress.equals(currentActiveDeviceAddress);

            if (ACTION_RECEIVE_DATA_EVENT.equals(action)) {
                handleReceiveDataEvent(intent, deviceData, isActive);
            } else if (ACTION_GENERATE_DATA_EVENT.equals(action)) {
                handleGenerateDataEvent(deviceData, isActive);
            } else if (ACTION_GET_DEVICE_CONFIG_RESPONSE.equals(action)) {
                handleGetDeviceConfigResponse(intent, deviceData, isActive);
            } else if (ACTION_SET_DEVICE_CONFIG_RESPONSE.equals(action)) {
                handleSetDeviceConfigResponse(intent, deviceData, isActive);
            } else if (ACTION_GET_CONFIG_RESPONSE.equals(action)) {
                handleGetConfigResponse(intent, deviceData);
            }
        }
    };

    private void handleReceiveDataEvent(Intent intent, DeviceData deviceData, boolean isActive) {
        byte[] arr = intent.getByteArrayExtra(EXTRA_PAYLOAD);
        if (arr == null || arr.length == 0) {
            Log.w(TAG, "handleReceiveDataEvent: empty payload, no RSSI byte to read.");
            return;
        }

        int lastByteUnsigned = arr[arr.length - 1] & 0xFF;
        int rssi = -(256 - lastByteUnsigned);
        deviceData.lastRssi = rssi;
        deviceData.recordReceived();

        String hexPayload = bytesToHex(arr);
        String time = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Calendar.getInstance().getTime());
        deviceData.lastMessageText = hexPayload + " в " + time;

        updateDevicesInfoDisplay();
    }

    private void handleGenerateDataEvent(DeviceData deviceData, boolean isActive) {
        deviceData.recordSent();

        updateDevicesInfoDisplay();
    }

    private void handleSetDeviceConfigResponse(Intent intent, DeviceData deviceData, boolean isActive) {
        clearPendingCommand(deviceData);

        byte errorCode = intent.getByteExtra(EXTRA_ERROR_CODE, (byte) -1);

        if (isActive) {
            updateResponseCodeLabel(errorCode);
            if (errorCode == 0) {
                setStatus("Конфигурация записана", StatusColor.COLOR_SUCCESS);
            } else {
                setStatus("Ошибка записи конфигурации", StatusColor.COLOR_ERROR);
            }
        }
    }

    private void handleGetDeviceConfigResponse(Intent intent, DeviceData deviceData, boolean isActive) {
        clearPendingCommand(deviceData);

        byte configByte = intent.getByteExtra(EXTRA_CONFIG_BYTE, (byte) -1);
        byte errorCode = intent.getByteExtra(EXTRA_ERROR_CODE, (byte) -1);

        ConfigByteUtils.ParsedConfig parsed = ConfigByteUtils.parseConfigByte(configByte);
        deviceData.isTransmitter = parsed.deviceType;
        deviceData.isEchoMode = parsed.echoEnabled;
        deviceData.packetLength = parsed.packetLength;

        if (!parsed.transmitMode) {
            Log.w(TAG, "handleGetDeviceConfigResponse: device reported Software transmit mode, " +
                    "but only Hardware mode is supported by this UI.");
        }

        updateDevicesInfoDisplay();

        if (isActive) {
            updateUIWithConfig(parsed);
            updateResponseCodeLabel(errorCode);
            if (errorCode == 0) {
                setStatus("Конфигурация прочитана", StatusColor.COLOR_SUCCESS);
            } else {
                setStatus("Ошибка чтения конфигурации", StatusColor.COLOR_ERROR);
            }
        }
    }

    private void handleGetConfigResponse(Intent intent, DeviceData deviceData) {
        String loraName = intent.getStringExtra(EXTRA_LORA_NAME);
        if (loraName != null) {
            deviceData.loraName = loraName;
            updateDevicesInfoDisplay();
        }
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

        updateDeviceSpinner();
        updateDevicesInfoDisplay();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_DATA_EVENT);
        filter.addAction(ACTION_GENERATE_DATA_EVENT);
        filter.addAction(ACTION_GET_DEVICE_CONFIG_RESPONSE);
        filter.addAction(ACTION_SET_DEVICE_CONFIG_RESPONSE);
        filter.addAction(ACTION_GET_CONFIG_RESPONSE);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, filter);

        startStatsUpdate();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        initViews(view);
        setupDeviceSpinner();
        setupConfigSpinners();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        activeDeviceSpinner = view.findViewById(R.id.activeDeviceSpinner);
        statusText = view.findViewById(R.id.statusText);
        statusIndicator = view.findViewById(R.id.statusIndicator);
        responseCodeLabel = view.findViewById(R.id.responseCodeLabel);

        deviceTypeSpinner = view.findViewById(R.id.deviceTypeSpinner);
        echoModeSpinner = view.findViewById(R.id.echoModeSpinner);
        packageLengthEditText = view.findViewById(R.id.packageLengthEditText);
        readConfigButton = view.findViewById(R.id.readConfigButton);
        writeConfigButton = view.findViewById(R.id.writeConfigButton);

        resetStatsButton = view.findViewById(R.id.resetStatsButton);

        allDevicesMessageChart = view.findViewById(R.id.allDevicesMessageChart);
        allDevicesChartPlaceholder = view.findViewById(R.id.allDevicesChartPlaceholder);

        devicesInfoContainer = view.findViewById(R.id.devicesInfoContainer);
        devicesInfoPlaceholder = view.findViewById(R.id.devicesInfoPlaceholder);
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

                DeviceData deviceData = activeDeviceData();
                if (deviceData != null) {
                    refreshUiFromDeviceData(deviceData);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentActiveDeviceAddress = null;
                setStatus("Устройство не выбрано", StatusColor.COLOR_ERROR);
            }
        });
    }

    private void updateDeviceSpinner() {
        if (bluetoothService != null) {
            List<String> deviceAddresses = bluetoothService.getConnectedDeviceAddresses();
            deviceSpinnerAdapter.clear();
            deviceSpinnerAdapter.addAll(deviceAddresses);
            deviceSpinnerAdapter.notifyDataSetChanged();

            if (currentActiveDeviceAddress != null && !deviceAddresses.contains(currentActiveDeviceAddress)) {
                currentActiveDeviceAddress = null;
                activeDeviceSpinner.setSelection(-1);
                setStatus("Выбранное устройство отключено", StatusColor.COLOR_ERROR);
            }

            if (currentActiveDeviceAddress == null && !deviceAddresses.isEmpty()) {
                activeDeviceSpinner.setSelection(0);
            }
        } else {
            deviceSpinnerAdapter.clear();
            deviceSpinnerAdapter.notifyDataSetChanged();
            currentActiveDeviceAddress = null;
            setStatus("Сервис не подключен", StatusColor.COLOR_ERROR);
        }
    }

    private void refreshUiFromDeviceData(DeviceData deviceData) {
        setStatus("Готово", StatusColor.COLOR_IDLE);
        responseCodeLabel.setText("Код: --");

        deviceTypeSpinner.setSelection(deviceData.isTransmitter ? 1 : 0);
        echoModeSpinner.setSelection(deviceData.isEchoMode ? 1 : 0);
        packageLengthEditText.setText(String.valueOf(deviceData.packetLength));

        updateDevicesInfoDisplay();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupClickListeners() {
        readConfigButton.setOnClickListener(v -> {
            if (isActiveDeviceReady()) {
                bluetoothService.sendMessage(new BleMessages.GetDeviceConfigurationCommand(), currentActiveDeviceAddress);

                bluetoothService.sendMessage(new BleMessages.GetConfigurationCommand(), currentActiveDeviceAddress);
                setStatus("Чтение конфигурации...", StatusColor.COLOR_SENDING);
                setPendingCommand(activeDeviceData(), "CMD_GET_DEVICE_CONFIGURATION");
            } else {
                showNotReadyError();
            }
        });

        writeConfigButton.setOnClickListener(v -> {
            DeviceData deviceData = activeDeviceData();
            if (isActiveDeviceReady()) {
                deviceData.isTransmitter = deviceTypeSpinner.getSelectedItemPosition() == 1;
                deviceData.isEchoMode = echoModeSpinner.getSelectedItemPosition() == 1;

                String lengthStr = packageLengthEditText.getText().toString();
                if (TextUtils.isEmpty(lengthStr)) {
                    Toast.makeText(getContext(), "Длина пакета не может быть пустой", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int length = Integer.parseInt(lengthStr);
                    if (length < 0 || length > 31) {
                        Toast.makeText(getContext(), "Длина пакета должна быть от 0 до 31", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    deviceData.packetLength = length;

                    // Transmit mode всегда Hardware - на устройстве нет Software режима в этом UI.
                    bluetoothService.sendMessage(new BleMessages.SetDeviceConfigurationCommand(
                            deviceData.isTransmitter, true, deviceData.isEchoMode, length), currentActiveDeviceAddress);

                    setStatus("Запись конфигурации...", StatusColor.COLOR_SENDING);
                    setPendingCommand(deviceData, "CMD_SET_DEVICE_CONFIGURATION");
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Неверный формат длины пакета", Toast.LENGTH_SHORT).show();
                }
            } else {
                showNotReadyError();
            }
        });

        resetStatsButton.setOnClickListener(v -> resetStatistics());
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
        setStatus(msg, StatusColor.COLOR_ERROR);
    }

    private void setupConfigSpinners() {
        ArrayAdapter<String> deviceTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"Приемник", "Передатчик"});
        deviceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceTypeSpinner.setAdapter(deviceTypeAdapter);

        ArrayAdapter<String> echoModeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"Выкл", "Вкл"});
        echoModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        echoModeSpinner.setAdapter(echoModeAdapter);
    }

    private void updateUIWithConfig(ConfigByteUtils.ParsedConfig config) {
        deviceTypeSpinner.setSelection(config.deviceType ? 1 : 0);
        echoModeSpinner.setSelection(config.echoEnabled ? 1 : 0);
        packageLengthEditText.setText(String.valueOf(config.packetLength));
    }

    private void startStatsUpdate() {
        statsUpdateTask = new Runnable() {
            @Override
            public void run() {
                if (!serviceBound && parentActivity != null && parentActivity.serviceBound) {
                    bluetoothService = parentActivity.bluetoothService;
                    serviceBound = true;
                    updateDeviceSpinner();
                } else if (serviceBound && (parentActivity == null || !parentActivity.serviceBound)) {
                    bluetoothService = null;
                    serviceBound = false;
                }

                updateDevicesInfoDisplay();
                handler.postDelayed(this, STATS_UPDATE_INTERVAL_MS);
            }
        };
        handler.post(statsUpdateTask);
    }

    private void stopStatsUpdate() {
        if (statsUpdateTask != null) {
            handler.removeCallbacks(statsUpdateTask);
            statsUpdateTask = null;
        }
    }

    private void resetStatistics() {
        if (deviceDataMap.isEmpty()) {
            Toast.makeText(getContext(), "Нет устройств для сброса", Toast.LENGTH_SHORT).show();
            return;
        }

        for (DeviceData deviceData : deviceDataMap.values()) {
            deviceData.sentCount = 0;
            deviceData.receivedCount = 0;
            deviceData.sentTimestamps.clear();
            deviceData.receivedTimestamps.clear();
            deviceData.lastRssi = null;
            deviceData.lastMessageText = null;
        }

        updateDevicesInfoDisplay();
    }

    private void updateResponseCodeLabel(byte errorCode) {
        responseCodeLabel.setText(findTitleByCode(errorCode));
    }

    private void updateDevicesInfoDisplay() {
        Log.d(TAG, "updateDevicesInfoDisplay called");

        if (devicesInfoContainer == null || devicesInfoPlaceholder == null) {
            Log.w(TAG, "updateDevicesInfoDisplay: views not initialized (devicesInfoContainer: "
                    + (devicesInfoContainer != null) + ", devicesInfoPlaceholder: "
                    + (devicesInfoPlaceholder != null) + ")");
            return;
        }

        devicesInfoContainer.removeAllViews();
        Log.d(TAG, "Cleared devicesInfoContainer");

        if (bluetoothService == null) {
            Log.w(TAG, "updateDevicesInfoDisplay: bluetoothService is null");
            devicesInfoPlaceholder.setVisibility(View.VISIBLE);
            updateAllDevicesChart(new ArrayList<>());
            return;
        }

        List<String> connectedAddresses = bluetoothService.getConnectedDeviceAddresses();
        Log.d(TAG, "Connected addresses: " + connectedAddresses);

        if (connectedAddresses.isEmpty()) {
            Log.d(TAG, "updateDevicesInfoDisplay: no connected devices");
            devicesInfoPlaceholder.setVisibility(View.VISIBLE);
            updateAllDevicesChart(new ArrayList<>());
            return;
        }

        devicesInfoPlaceholder.setVisibility(View.GONE);
        Log.d(TAG, "Hid placeholder, showing device list");

        List<String> sortedAddresses = new ArrayList<>(connectedAddresses);
        sortedAddresses.sort(String::compareTo);
        Log.d(TAG, "Sorted addresses: " + sortedAddresses);

        updateAllDevicesChart(sortedAddresses);

        for (String address : sortedAddresses) {
            DeviceData data = deviceDataMap.computeIfAbsent(address, k -> new DeviceData());
            Log.d(TAG, "Processing device " + address +
                    ": isTransmitter=" + data.isTransmitter +
                    ", isEchoMode=" + data.isEchoMode +
                    ", sentCount=" + data.sentCount +
                    ", receivedCount=" + data.receivedCount +
                    ", lastRssi=" + data.lastRssi);

            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_device_info, devicesInfoContainer, false);

            TextView nameAddressLabel = itemView.findViewById(R.id.deviceNameAddressLabel);
            TextView statusLabel = itemView.findViewById(R.id.deviceStatusLabel);
            TextView loraNameLabel = itemView.findViewById(R.id.deviceLoraNameLabel);
            MessageCountChartView sentChart = itemView.findViewById(R.id.deviceSentChart);
            MessageCountChartView receivedChart = itemView.findViewById(R.id.deviceReceivedChart);
            TextView statsLabel = itemView.findViewById(R.id.deviceStatsLabel);
            TextView recentStatsLabel = itemView.findViewById(R.id.deviceRecentStatsLabel);
            TextView rssiLabel = itemView.findViewById(R.id.deviceRssiLabel);
            TextView lastMessageLabel = itemView.findViewById(R.id.deviceLastMessageLabel);

            if (nameAddressLabel != null) nameAddressLabel.setText(address);
            if (statusLabel != null)
                statusLabel.setText("Статус: Подключено" + (address.equals(currentActiveDeviceAddress) ? " (Активно)" : ""));
            if (loraNameLabel != null)
                loraNameLabel.setText("LoRa: " + (data.loraName != null ? data.loraName : "--"));
            if (sentChart != null) {
                sentChart.setLineColor(StatusColor.CHART_SENT_COLOR);
                sentChart.setData(data.sentHistoryBuckets());
            }
            if (receivedChart != null) {
                receivedChart.setLineColor(StatusColor.CHART_RECEIVED_COLOR);
                receivedChart.setData(data.receivedHistoryBuckets());
            }

            String role = data.isTransmitter ? "Передатчик" : "Приемник";
            String mode = data.isEchoMode ? "Эхо" : "Без эхо";

            int recentSent = data.recentSentCount();
            int recentReceived = data.recentReceivedCount();

            Log.d(TAG, "Device " + address + " - recentSent: " + recentSent + ", recentReceived: " + recentReceived);

            String statsText;
            String recentStatsText;

            if (data.isTransmitter && data.isEchoMode) {
                Log.d(TAG, "Device " + address + " is Transmitter in Echo Mode");
                double successRate = data.sentCount > 0
                        ? (data.receivedCount * 100.0 / data.sentCount) : 0.0;
                statsText = String.format(Locale.getDefault(),
                        "%s (%s) - Отправлено: %d | Возвращено: %d | Успешность: %.2f%%",
                        role, mode, data.sentCount, data.receivedCount, successRate);

                Log.d(TAG, "Device " + address + " success rate: " + successRate + "%");

                double recentSuccessRate = recentSent > 0
                        ? (recentReceived * 100.0 / recentSent) : 0.0;
                recentStatsText = String.format(Locale.getDefault(),
                        "Последние 10с - Отправлено: %d | Возвращено: %d | Успешность: %.2f%%",
                        recentSent, recentReceived, recentSuccessRate);

                Log.d(TAG, "Device " + address + " recent success rate: " + recentSuccessRate + "%");
            } else {
                Log.d(TAG, "Device " + address + " is not Transmitter in Echo Mode - sentCount: " +
                        data.sentCount + ", receivedCount: " + data.receivedCount);
                statsText = String.format(Locale.getDefault(),
                        "%s (%s) - Отправлено: %d | Получено: %d", role, mode, data.sentCount, data.receivedCount);

                recentStatsText = String.format(Locale.getDefault(),
                        "Последние 10с - Отправлено: %d | Получено: %d", recentSent, recentReceived);
            }

            if (statsLabel != null) {
                statsLabel.setText(statsText);
                Log.d(TAG, "Set stats text for " + address + ": " + statsText);
            }
            if (recentStatsLabel != null) {
                recentStatsLabel.setText(recentStatsText);
                Log.d(TAG, "Set recent stats text for " + address + ": " + recentStatsText);
            }

            if (rssiLabel != null) {
                String rssiText = data.lastRssi != null
                        ? String.format(Locale.getDefault(), "RSSI: %d дБм", data.lastRssi)
                        : "RSSI: --";
                rssiLabel.setText(rssiText);
                Log.d(TAG, "Set RSSI text for " + address + ": " + rssiText);
            }

            if (lastMessageLabel != null) {
                String messageText = data.lastMessageText != null
                        ? "Последнее сообщение: " + data.lastMessageText
                        : "Последнее сообщение: --";
                lastMessageLabel.setText(messageText);
                Log.d(TAG, "Set message text for " + address + ": " + messageText);
            }

            devicesInfoContainer.addView(itemView);
            Log.d(TAG, "Added view for device: " + address);
        }

        Log.d(TAG, "updateDevicesInfoDisplay completed, total items added: " + sortedAddresses.size());
    }

    private void updateAllDevicesChart(List<String> sortedAddresses) {
        if (allDevicesMessageChart == null || allDevicesChartPlaceholder == null) {
            return;
        }

        if (sortedAddresses.isEmpty()) {
            allDevicesChartPlaceholder.setVisibility(View.VISIBLE);
            allDevicesMessageChart.setVisibility(View.GONE);
            return;
        }

        allDevicesChartPlaceholder.setVisibility(View.GONE);
        allDevicesMessageChart.setVisibility(View.VISIBLE);

        long[] sentCounts = new long[sortedAddresses.size()];
        long[] receivedCounts = new long[sortedAddresses.size()];
        List<String> loraNames = new ArrayList<>(sortedAddresses.size());
        for (int i = 0; i < sortedAddresses.size(); i++) {
            DeviceData data = deviceDataMap.computeIfAbsent(sortedAddresses.get(i), k -> new DeviceData());
            sentCounts[i] = data.sentCount;
            receivedCounts[i] = data.receivedCount;
            loraNames.add(data.loraName);
        }

        allDevicesMessageChart.setData(sortedAddresses, loraNames, sentCounts, receivedCounts);
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

    private void setPendingCommand(DeviceData deviceData, String commandType) {
        if (deviceData == null) return;

        clearPendingCommand(deviceData);

        deviceData.pendingCommandType = commandType;
        deviceData.commandStartTime = System.currentTimeMillis();

        Runnable timeoutRunnable = () -> {
            if (!deviceData.pendingCommandType.isEmpty()) {
                long elapsedTime = System.currentTimeMillis() - deviceData.commandStartTime;
                Log.d(TAG, "Timeout for command: " + deviceData.pendingCommandType + " after " + elapsedTime + "ms");

                if (deviceData == activeDeviceData()) {
                    setStatus("Тайм-аут: " + deviceData.pendingCommandType, StatusColor.COLOR_TIMEOUT);
                }
                deviceData.pendingCommandType = "";
            }
        };
        deviceData.timeoutRunnable = timeoutRunnable;

        timeoutHandler.postDelayed(timeoutRunnable, RESPONSE_TIMEOUT_MS);
    }

    private void clearPendingCommand(DeviceData deviceData) {
        if (deviceData.timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(deviceData.timeoutRunnable);
            deviceData.timeoutRunnable = null;
        }
        deviceData.pendingCommandType = "";
        deviceData.commandStartTime = 0;
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);

        stopStatsUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
