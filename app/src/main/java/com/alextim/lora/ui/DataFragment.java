package com.alextim.lora.ui;

import static com.alextim.lora.client.ble.BleMessage.bytesToHex;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.R;
import com.alextim.lora.client.ble.BluetoothService;
import com.alextim.lora.service.message.BleMessages.GetLoraRssiCommand;
import com.alextim.lora.service.message.BleMessages.SendDataCommand;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class DataFragment extends Fragment {
    private static final String TAG = "DataFragment";

    public enum TestMode {
        ECHO_MODE,
        DIRECT_MODE;
    }

    private BluetoothSetupActivity parentActivity;
    private BluetoothService bluetoothService;
    private boolean serviceBound = false;

    private CheckBox senderCheckBox;
    private Spinner testModeSpinner;
    private Button resetStatsButton;
    private ScrollView devicesInfoScrollView;
    private LinearLayout devicesInfoContainer;
    private TextView devicesInfoPlaceholder;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable periodicTask;
    private boolean isSender = false;
    private TestMode currentTestMode = TestMode.ECHO_MODE;

    private final Map<String, DeviceData> deviceDataMap = new ConcurrentHashMap<>();

    private static class DeviceData {
        String name = "Unknown Device";
        long sentCount = 0;
        int receivedFromSenderCount = 0;
        final Map<Integer, Long> sentMsgHashes = new HashMap<>();
        long receiverStartTime = System.currentTimeMillis();
        final Deque<Long> recentMsgTimes = new LinkedList<>();
        int noiseRssi = Integer.MIN_VALUE;
        int receiveDataRssi = Integer.MIN_VALUE;
        String lastReceivedMessage = "--";
        long lastMessageTimestamp = 0;
    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String receivedDeviceAddress = intent.getStringExtra("device_address");
            Log.d(TAG, "onReceive action " + action + " from device: " + receivedDeviceAddress);

            if (receivedDeviceAddress == null) {
                Log.w(TAG, "Received message without device address: " + action);
                return;
            }

            DeviceData deviceData = deviceDataMap.computeIfAbsent(receivedDeviceAddress, k -> new DeviceData());

            if ("ACTION_RECEIVE_DATA_EVENT".equals(action)) {
                byte[] arr = intent.getByteArrayExtra("payload");
                String hexPayload = bytesToHex(arr);

                if (isSender) {
                    int receivedMsgHash = Arrays.hashCode(arr);
                    deviceData.sentMsgHashes.remove(receivedMsgHash);
                } else {
                    if (currentTestMode == TestMode.ECHO_MODE) {
                        if (bluetoothService != null) {
                            bluetoothService.sendMessage(new SendDataCommand(arr), receivedDeviceAddress);
                        }
                    }

                    deviceData.recentMsgTimes.add(System.currentTimeMillis());
                }
                deviceData.receivedFromSenderCount++;

                String time = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Calendar.getInstance().getTime());
                deviceData.lastReceivedMessage = time + " " + hexPayload;
                deviceData.lastMessageTimestamp = System.currentTimeMillis();

            } else if ("ACTION_SEND_DATA_RESPONSE".equals(action)) {

            } else if ("ACTION_GET_LORA_RSSI_RESPONSE".equals(action)) {
                int noiseRssi = intent.getIntExtra("noiseRssi", Integer.MIN_VALUE);
                int receiveDataRssi = intent.getIntExtra("receiveDataRssi", Integer.MIN_VALUE);
                deviceData.noiseRssi = noiseRssi;
                deviceData.receiveDataRssi = receiveDataRssi;
            }

            updateDevicesInfoDisplay();
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BluetoothSetupActivity) {
            parentActivity = (BluetoothSetupActivity) context;
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
        filter.addAction("ACTION_RECEIVE_DATA_EVENT");
        filter.addAction("ACTION_SEND_DATA_RESPONSE");
        filter.addAction("ACTION_GET_LORA_RSSI_RESPONSE");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, filter);

        updateDevicesInfoDisplay();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        initViews(view);
        setupSpinners();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        senderCheckBox = view.findViewById(R.id.senderCheckBox);
        testModeSpinner = view.findViewById(R.id.testModeSpinner);
        resetStatsButton = view.findViewById(R.id.resetStatsButton);
        devicesInfoScrollView = view.findViewById(R.id.devicesInfoScrollView);
        devicesInfoContainer = view.findViewById(R.id.devicesInfoContainer);
        devicesInfoPlaceholder = view.findViewById(R.id.devicesInfoPlaceholder);
    }

    private void setupSpinners() {
        String[] modeOptions = {"Echo Mode", "Direct Mode"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, modeOptions);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        testModeSpinner.setAdapter(modeAdapter);

        testModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTestMode = TestMode.values()[position];
                if (isSender) {
                    stopTest();
                    startTest();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupClickListeners() {
        senderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startTest();
            } else {
                stopTest();
            }
        });

        resetStatsButton.setOnClickListener(v -> {
            resetAllStatistics();
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void startTest() {
        if (isSender || !serviceBound || bluetoothService == null)
            return;

        isSender = true;

        List<String> connectedDevices = bluetoothService.getConnectedDeviceAddresses();
        if (connectedDevices.isEmpty()) {
            Log.w(TAG, "No devices connected to send test data to.");
            isSender = false;
            senderCheckBox.setChecked(false);
            return;
        }
        String targetDeviceAddress = connectedDevices.get(0);
        Log.d(TAG, "Starting sender test to device: " + targetDeviceAddress);

        DeviceData deviceData = deviceDataMap.computeIfAbsent(targetDeviceAddress, k -> new DeviceData());

        periodicTask = new Runnable() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void run() {
                if (isSender && serviceBound && bluetoothService != null) {
                    byte[] arr = new byte[16];
                    new Random().nextBytes(arr);

                    bluetoothService.sendMessage(new SendDataCommand(arr), targetDeviceAddress);

                    deviceData.sentCount++;

                    if (currentTestMode == TestMode.ECHO_MODE) {
                        deviceData.sentMsgHashes.put(Arrays.hashCode(arr), System.currentTimeMillis());
                    }

                    handler.postDelayed(this, 1_000);
                }
            }
        };
        handler.post(periodicTask);
    }

    private void stopTest() {
        if (periodicTask != null) {
            handler.removeCallbacks(periodicTask);
            periodicTask = null;
        }

        isSender = false;
    }

    private void resetAllStatistics() {
        for (DeviceData data : deviceDataMap.values()) {
            data.sentCount = 0;
            data.receivedFromSenderCount = 0;
            data.sentMsgHashes.clear();
            data.recentMsgTimes.clear();
            data.receiverStartTime = System.currentTimeMillis();
            data.lastReceivedMessage = "--";
            data.lastMessageTimestamp = 0;
        }
        updateDevicesInfoDisplay();
        Toast.makeText(getContext(), "Statistics for all devices reset", Toast.LENGTH_SHORT).show();
    }


    private void updateDevicesInfoDisplay() {
        devicesInfoContainer.removeAllViews();

        if (deviceDataMap.isEmpty()) {
            devicesInfoPlaceholder.setVisibility(View.VISIBLE);
            return;
        }

        devicesInfoPlaceholder.setVisibility(View.GONE);

        List<String> sortedAddresses = new ArrayList<>(deviceDataMap.keySet());
        sortedAddresses.sort(String::compareTo);

        for (String address : sortedAddresses) {
            DeviceData data = deviceDataMap.get(address);

            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_device_info, devicesInfoContainer, false);

            TextView nameAddressLabel = itemView.findViewById(R.id.deviceNameAddressLabel);
            TextView statusLabel = itemView.findViewById(R.id.deviceStatusLabel);
            TextView statsLabel = itemView.findViewById(R.id.deviceStatsLabel);
            TextView rssiLabel = itemView.findViewById(R.id.deviceRssiLabel);
            TextView lastMessageLabel = itemView.findViewById(R.id.deviceLastMessageLabel);

            nameAddressLabel.setText(data.name + " (" + address + ")");
            statusLabel.setText("Status: Connected");
            statsLabel.setText(String.format("Rx: %d", data.receivedFromSenderCount));

            String rssiText = "RSSI: ";
            if (data.noiseRssi != Integer.MIN_VALUE) {
                rssiText += String.format("Noise: %d dBm", data.noiseRssi);
            } else {
                rssiText += "Noise: -- dBm";
            }
            rssiText += " | ";
            if (data.receiveDataRssi != Integer.MIN_VALUE) {
                rssiText += String.format("Last Msg: %d dBm", data.receiveDataRssi);
            } else {
                rssiText += "Last Msg: -- dBm";
            }
            rssiLabel.setText(rssiText);

            String lastMessageText = "Last Message: ";
            if (!"--".equals(data.lastReceivedMessage)) {
                lastMessageText += data.lastReceivedMessage;
            } else {
                lastMessageText += "--";
            }
            lastMessageLabel.setText(lastMessageText);

            devicesInfoContainer.addView(itemView);
        }

        devicesInfoScrollView.post(() -> devicesInfoScrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver);
    }

    @Override
    public void onDestroy() {
        stopTest();
        super.onDestroy();
    }
}