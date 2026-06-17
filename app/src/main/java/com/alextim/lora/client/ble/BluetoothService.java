package com.alextim.lora.client.ble;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;

import static com.alextim.lora.client.ble.BleMessage.bytesToHex;
import static com.alextim.lora.client.ble.constants.BleActions.ACTION_BLE_RSSI_UPDATED;
import static com.alextim.lora.client.ble.constants.BleActions.ACTION_BLE_STATS_UPDATED;
import static com.alextim.lora.client.ble.constants.BleActions.ACTION_BLE_STATUS_UPDATED;
import static com.alextim.lora.client.ble.constants.BleConnectionStatus.CONNECTED;
import static com.alextim.lora.client.ble.constants.BleConnectionStatus.DISCONNECTED;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_CONNECTION_TIME;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_DEVICE_ADDRESS;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_DEVICE_NAME;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_ERRORS;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_RECEIVED_COUNT;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_RSSI;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_SENT_COUNT;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_STATUS;
import static com.alextim.lora.client.ble.constants.ServiceUUIDs.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;
import static com.alextim.lora.client.ble.constants.ServiceUUIDs.UART_NOTIFY_CHART_UUID;
import static com.alextim.lora.client.ble.constants.ServiceUUIDs.UART_SERVICE_UUID;
import static com.alextim.lora.client.ble.constants.ServiceUUIDs.UART_WRITE_CHAT_UUID;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.client.ble.constants.BleConnectionStatus;
import com.alextim.lora.client.ble.constants.ConnectionPersistence;
import com.alextim.lora.client.ble.constants.ConnectionStats;
import com.alextim.lora.client.ble.constants.DeviceConnection;
import com.alextim.lora.service.FileLogger;
import com.alextim.lora.service.MessageProcessingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class BluetoothService extends BluetoothServiceBase {

    private BluetoothAdapter bluetoothAdapter;

    private final Map<String, DeviceConnection> activeConnections = new ConcurrentHashMap<>();
    private final Set<BluetoothDevice> scannedDevices = Collections.synchronizedSet(new LinkedHashSet<>());
    private ConnectionPersistence connectionPersistence;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private MessageProcessingService messageProcessingService;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        FileLogger.d(TAG, "Bluetooth Adapter is OFF.");
                        disconnectAllDevices();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        FileLogger.d(TAG, "Bluetooth Adapter is ON.");

                        // if (connectionPersistence.hasAnyLastDevice() && !connectionPersistence.isUserInitiatedDisconnectForAll()) {
                        //     Set<String> lastAddresses = connectionPersistence.getLastDeviceAddresses();
                        //     for(String addr : lastAddresses) {
                        //         if(!connectionPersistence.isUserInitiatedDisconnect(addr)) {
                        //             connectToDevice(addr);
                        //         }
                        //     }
                        // }
                        break;
                }
            }
        }
    };

    @RequiresPermission(allOf = {Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.POST_NOTIFICATIONS})
    private final BiConsumer<byte[], String> deviceAwareConsumer = (data, deviceAddress) -> {
        BleMessage bleMessage = new BleMessage(data);
        BleMessage parsed = messageProcessingService.parseMessage(bleMessage);
        logMessageProcessing(bleMessage, parsed, deviceAddress);
        messageProcessingService.sendParsedMessageToUI(this, parsed, deviceAddress);
    };

    private void logMessageProcessing(BleMessage bleMessage, BleMessage parsed, String deviceAddress) {
        String FRAME_LINE = "══════════════════════════════════════════════════════════════════════════════";
        FileLogger.i(TAG, "╔" + FRAME_LINE);
        FileLogger.i(TAG, "║ [BLE] RECEIVED from [" + deviceAddress + "]: " + (bleMessage != null ? bleMessage.toString() : "null"));
        FileLogger.i(TAG, "║ [PARSED] " + (parsed != null ? parsed.toString() : "null"));
        FileLogger.i(TAG, "╚" + FRAME_LINE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onCreate() {
        super.onCreate();

        FileLogger.init(this, "ble_service_log");
        FileLogger.d(TAG, "BluetoothService.onCreate() started");

        this.connectionPersistence = new ConnectionPersistence(this);

        messageProcessingService = new MessageProcessingService();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        startForegroundService();
        FileLogger.d(TAG, "BluetoothService created");

        registerReceivers();

        // if (connectionPersistence.hasAnyLastDevice() && !connectionPersistence.isUserInitiatedDisconnectForAll()) {
        //     Set<String> lastAddresses = connectionPersistence.getLastDeviceAddresses();
        //     for(String addr : lastAddresses) {
        //         if(!connectionPersistence.isUserInitiatedDisconnect(addr)) {
        //             connectToDevice(addr);
        //         }
        //     }
        // }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onDestroy() {
        FileLogger.d(TAG, "BluetoothService destroyed");

        unregisterReceivers();

        disconnectAllDevices();

        super.onDestroy();
    }

    private void registerReceivers() {
        IntentFilter systemFilter = new IntentFilter();
        systemFilter.addAction(ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, systemFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterReceivers() {
        try {
            unregisterReceiver(broadcastReceiver);
            FileLogger.d(TAG, "Unregistered system broadcast receiver");
        } catch (IllegalArgumentException e) {
            FileLogger.e(TAG, "System broadcast receiver was not registered", e);
        }

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            FileLogger.d(TAG, "Unregistered local broadcast receiver");
        } catch (IllegalArgumentException e) {
            FileLogger.e(TAG, "Local broadcast receiver was not registered", e);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void startScan() {
        FileLogger.d(TAG, "Starting BLE scan...");
        synchronized (scannedDevices) {
            scannedDevices.clear();
        }

        ScanCallback scanCallback = new ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null &&
                        !scannedDevices.contains(device) &&
                        device.getName().toUpperCase().startsWith("SHARM")) { //todo

                    synchronized (scannedDevices) {
                        scannedDevices.add(device);
                        FileLogger.d(TAG, "Found device: " + device.getAddress() + " (" + device.getName() + ") RSSI: " + result.getRssi());
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                FileLogger.e(TAG, "Scan failed with error code: " + errorCode);
            }
        };

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(scanCallback);

            new Thread(() -> {
                try {
                    Thread.sleep(4_000);
                    bluetoothLeScanner.stopScan(scanCallback);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT})
    public void connectToMultipleDevices(List<String> deviceAddresses) {
        if (deviceAddresses == null || deviceAddresses.isEmpty()) {
            FileLogger.w(TAG, "connectToMultipleDevices: List is null or empty.");
            return;
        }

        if (bluetoothAdapter == null) {
            FileLogger.e(TAG, "connectToMultipleDevices: BluetoothAdapter is null.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            FileLogger.w(TAG, "connectToMultipleDevices: Bluetooth is not enabled.");
            return;
        }

        FileLogger.d(TAG, "Attempting to connect to " + deviceAddresses.size() + " devices simultaneously.");

        for (String address : deviceAddresses) {
            if (!activeConnections.containsKey(address)) {
                connectToDevice(address);
            } else {
                FileLogger.d(TAG, "Already connected to device: " + address);
                broadcastStatusUpdateForDevice(address, CONNECTED);
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connectToDevice(String deviceAddress) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                FileLogger.w(TAG, "Bluetooth is not enabled, cannot connect to device: " + deviceAddress);
                broadcastStatusUpdateForDevice(deviceAddress, DISCONNECTED);
                return;
            }
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void connectToDevice(BluetoothDevice device) {
        String deviceAddress = device.getAddress();
        FileLogger.d(TAG, "Connecting to device: " + device.getName() + " (" + deviceAddress + ")");

        BluetoothGatt gatt = device.connectGatt(this, true, createDeviceSpecificCallback(deviceAddress));

        DeviceConnection conn = new DeviceConnection(gatt,  new ConnectionStats(), device);

        activeConnections.put(deviceAddress, conn);

        FileLogger.d(TAG, "Started connecting to device: " + device.getName() + " (" + deviceAddress + ")");
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void disconnectAllDevices() {
        FileLogger.d(TAG, "Disconnecting from all devices.");
        synchronized (activeConnections) {
            for (Map.Entry<String, DeviceConnection> entry : activeConnections.entrySet()) {
                String deviceAddress = entry.getKey();
                FileLogger.d(TAG, "Disconnecting from device: " + deviceAddress);
                entry.getValue().getGatt().disconnect();
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void disconnectDevice(String deviceAddress) {
        DeviceConnection conn = activeConnections.get(deviceAddress);
        if (conn != null) {
            FileLogger.d(TAG, "Disconnecting from device: " + deviceAddress);
            conn.getGatt().disconnect();
        } else {
            FileLogger.d(TAG, "Attempted to disconnect from non-connected device: " + deviceAddress);
        }
    }

    public void setUserInitiatedDisconnect(String address, boolean res) {
        connectionPersistence.setUserInitiatedDisconnect(address, res);
    }

    private BluetoothGattCallback createDeviceSpecificCallback(String deviceAddress) {
        return new BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                DeviceConnection deviceConnection = activeConnections.get(deviceAddress);
                if (deviceConnection == null || deviceConnection.getGatt() != gatt) {
                    FileLogger.w(TAG, "onConnectionStateChange for unexpected gatt/device: " + deviceAddress);
                    return;
                }

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            FileLogger.d(TAG, "Connected to device: " + deviceAddress + ". Discovering services...");
                            deviceConnection.setConnected(true);

                            connectionPersistence.setUserInitiatedDisconnect(deviceAddress, false);
                            connectionPersistence.saveLastDevice(deviceConnection.getDevice());

                            deviceConnection.getConnectionStats().setConnectionStartTime(System.currentTimeMillis());

                            broadcastStatusUpdateForDevice(deviceAddress, CONNECTED);

                            gatt.requestMtu(156);
                            break;

                        case BluetoothProfile.STATE_DISCONNECTED:
                            FileLogger.d(TAG, "Disconnected from device: " + deviceAddress);
                            deviceConnection.setConnected(false);

                            DeviceConnection removedConn = activeConnections.remove(deviceAddress);

                            broadcastStatusUpdateForDevice(deviceAddress, DISCONNECTED);

                            if (removedConn != null) {
                                removedConn.getGatt().close();
                                connectionPersistence.removeLastDevice(deviceAddress);
                            }
                            break;
                    }
                } else {
                    FileLogger.w(TAG, "onConnectionStateChange ERROR for device: " + deviceAddress + ". Status: " + status + " New State: " + newState);
                    deviceConnection.setConnected(false);

                    DeviceConnection removedConn = activeConnections.remove(deviceAddress);

                    broadcastStatusUpdateForDevice(deviceAddress, DISCONNECTED);

                    if (removedConn != null) {
                        removedConn.getGatt().close();
                        connectionPersistence.removeLastDevice(deviceAddress);
                    }
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                DeviceConnection conn = activeConnections.get(deviceAddress);
                if (conn == null || conn.getGatt() != gatt) {
                    FileLogger.w(TAG, "onServicesDiscovered for unexpected gatt/device: " + deviceAddress);
                    return;
                }

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "onServicesDiscovered for device " + deviceAddress + ": status=" + status);
                    Log.d(TAG, "Available services for " + deviceAddress + ":");
                    for (BluetoothGattService service : gatt.getServices()) {
                        Log.d(TAG, "  Service: " + service.getUuid());
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            Log.d(TAG, "    Characteristic: " + characteristic.getUuid() +
                                    ", Properties: " + characteristic.getProperties());
                        }
                    }

                    BluetoothGattService service = gatt.getService(UART_SERVICE_UUID);
                    if (service != null) {
                        Log.d(TAG, "Found UART service for device " + deviceAddress + ": " + UART_SERVICE_UUID);
                        BluetoothGattCharacteristic writeChar = service.getCharacteristic(UART_WRITE_CHAT_UUID);
                        Log.d(TAG, "Write characteristic (for sending to device " + deviceAddress + "): " + (writeChar != null));
                        BluetoothGattCharacteristic notifyChar = service.getCharacteristic(UART_NOTIFY_CHART_UUID);
                        Log.d(TAG, "Notify characteristic (for receiving from device " + deviceAddress + "): " + (notifyChar != null));

                        if (notifyChar != null) {
                            conn.setNotifyCharacteristic(notifyChar);
                            Log.d(TAG, "Notify characteristic properties: " + notifyChar.getProperties());
                            boolean supportsNotify = (notifyChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                            Log.d(TAG, "Notify characteristic supports notify: " + supportsNotify);
                            if (supportsNotify) {
                                boolean setNotificationSuccess = gatt.setCharacteristicNotification(notifyChar, true);
                                Log.d(TAG, "setCharacteristicNotification returned: " + setNotificationSuccess + " for device " + deviceAddress);

                                BluetoothGattDescriptor descriptor = notifyChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                                if (descriptor != null) {
                                    Log.d(TAG, "Found CCCD descriptor for device " + deviceAddress);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    boolean writeDescriptorSuccess = gatt.writeDescriptor(descriptor);
                                    Log.d(TAG, "writeDescriptor returned: " + writeDescriptorSuccess + " for device " + deviceAddress);
                                    if (writeDescriptorSuccess) {
                                        Log.d(TAG, "Notifications enabled successfully for device: " + deviceAddress);
                                    } else {
                                        Log.e(TAG, "Failed to write descriptor for notifications for device: " + deviceAddress);
                                    }
                                } else {
                                    Log.e(TAG, "CCCD descriptor is null for device: " + deviceAddress);
                                }
                            } else {
                                Log.w(TAG, "Notify characteristic does NOT support NOTIFY for device: " + deviceAddress);
                            }
                        } else {
                            Log.e(TAG, "Notify characteristic is null for device: " + deviceAddress);
                        }

                        if (writeChar != null) {
                            conn.setWriteCharacteristic(writeChar);
                            Log.d(TAG, "Write characteristic ready for writing for device: " + deviceAddress);
                        } else {
                            Log.e(TAG, "Write characteristic is null for device: " + deviceAddress);
                        }

                        Log.d(TAG, "UART ready for communication with device: " + deviceAddress);

                    } else {
                        Log.e(TAG, "UART service not found for device: " + deviceAddress + "! Available services:");
                        for (BluetoothGattService s : gatt.getServices()) {
                            Log.e(TAG, "  " + s.getUuid());
                        }
                        conn.getConnectionStats().incrementErrorsCounter();
                    }
                } else {
                    Log.e(TAG, "Service discovery failed for device: " + deviceAddress + ", status=" + status);
                    conn.getConnectionStats().incrementErrorsCounter();
                }
                broadcastConnectionStatsUpdateForDevice(deviceAddress);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                DeviceConnection conn = activeConnections.get(deviceAddress);
                if (conn == null || conn.getGatt() != gatt || conn.getNotifyCharacteristic() == null || !characteristic.getUuid().equals(conn.getNotifyCharacteristic().getUuid())) {
                    FileLogger.w(TAG, "onCharacteristicChanged for unexpected gatt/device/char: " + deviceAddress);
                    return;
                }

                FileLogger.d(TAG, "onCharacteristicChanged called for device: " + deviceAddress + bytesToHex(characteristic.getValue()));
/*                byte[] data = characteristic.getValue();
                if (data.length != 0) {
                    BleProtocolParser.addData(data);
                    BleProtocolParser.handle(deviceAwareConsumer, deviceAddress);
                }*/
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                DeviceConnection conn = activeConnections.get(deviceAddress);
                if (conn == null || conn.getGatt() != gatt || conn.getWriteCharacteristic() == null || !characteristic.getUuid().equals(conn.getWriteCharacteristic().getUuid())) {
                    FileLogger.w(TAG, "onCharacteristicWrite for unexpected gatt/device/char: " + deviceAddress);
                    return;
                }

                Log.d(TAG, "onCharacteristicWrite for device " + deviceAddress + ": status=" + status + ", characteristic=" + characteristic.getUuid());
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Write successful for device: " + deviceAddress);
                    conn.getConnectionStats().incrementSentMessagesCounter();
                } else {
                    Log.e(TAG, "Write failed for device " + deviceAddress + ", status=" + status);
                    conn.getConnectionStats().incrementErrorsCounter();
                }
                broadcastConnectionStatsUpdateForDevice(deviceAddress);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                DeviceConnection conn = activeConnections.get(deviceAddress);
                if (conn == null || conn.getGatt() != gatt) {
                    FileLogger.w(TAG, "onReadRemoteRssi for unexpected gatt/device: " + deviceAddress);
                    return;
                }

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    conn.getConnectionStats().setCurrentRssi(rssi);
                    broadcastRssiUpdateForDevice(deviceAddress);
                    FileLogger.d(TAG, "RSSI (connected to " + deviceAddress + "): " + rssi);
                } else {
                    FileLogger.e(TAG, "RSSI read failed for device " + deviceAddress + ": " + status);
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                DeviceConnection conn = activeConnections.get(deviceAddress);
                if (conn == null || conn.getGatt() != gatt) {
                    FileLogger.w(TAG, "onMtuChanged for unexpected gatt/device: " + deviceAddress);
                    return;
                }

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    FileLogger.d(TAG, "MTU change success for device " + deviceAddress + ", new MTU: " + mtu);
                    gatt.discoverServices();

                } else {
                    FileLogger.e(TAG, "MTU change failure for device " + deviceAddress + ", status: " + status);
                }
            }
        };
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public boolean sendMessage(BleMessage message, String deviceAddress) {
        DeviceConnection conn = activeConnections.get(deviceAddress);
        if (conn == null || conn.getGatt() == null || conn.getWriteCharacteristic() == null || message == null) {
            Log.e(TAG, "GATT, writeCharacteristic, or message is null for device: " + deviceAddress);
            return false;
        }
        Log.d(TAG, "Sending message to device " + deviceAddress + ": " + message.getClass().getSimpleName());
        Log.d(TAG, "Sending bytes: " + bytesToHex(message.rawMessage));
        conn.getWriteCharacteristic().setValue(message.rawMessage);
        conn.getWriteCharacteristic().setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean success = conn.getGatt().writeCharacteristic(conn.getWriteCharacteristic());
        if (success) {
            Log.d(TAG, "Write request sent successfully (no response expected) to device: " + deviceAddress);
        } else {
            Log.e(TAG, "Failed to send write request to device: " + deviceAddress);
            conn.getConnectionStats().incrementErrorsCounter();
        }
        broadcastConnectionStatsUpdateForDevice(deviceAddress);
        return success;
    }

    public boolean isEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public boolean isConnectedToDevice(String deviceAddress) {
        DeviceConnection conn = activeConnections.get(deviceAddress);
        return conn != null && conn.isConnected() && conn.getGatt() != null;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void broadcastStatusUpdateForDevice(String deviceAddress, BleConnectionStatus status) {
        Intent intent = new Intent(ACTION_BLE_STATUS_UPDATED);
        intent.putExtra(EXTRA_BLE_STATUS, status.name());
        intent.putExtra(EXTRA_BLE_DEVICE_ADDRESS, deviceAddress);

        String deviceName = getDeviceName(deviceAddress);
        intent.putExtra(EXTRA_BLE_DEVICE_NAME, deviceName != null ? deviceName : deviceAddress);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastRssiUpdateForDevice(String deviceAddress) {
        DeviceConnection conn = activeConnections.get(deviceAddress);
        if (conn != null) {
            Intent intent = new Intent(ACTION_BLE_RSSI_UPDATED);
            intent.putExtra(EXTRA_BLE_DEVICE_ADDRESS, deviceAddress);
            intent.putExtra(EXTRA_BLE_RSSI, conn.getConnectionStats().getCurrentRssi());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void broadcastConnectionStatsUpdateForDevice(String deviceAddress) {
        DeviceConnection conn = activeConnections.get(deviceAddress);
        if (conn != null) {
            Intent intent = new Intent(ACTION_BLE_STATS_UPDATED);
            intent.putExtra(EXTRA_BLE_DEVICE_ADDRESS, deviceAddress);
            intent.putExtra(EXTRA_BLE_SENT_COUNT, conn.getConnectionStats().getSentMessagesCounter());
            intent.putExtra(EXTRA_BLE_RECEIVED_COUNT, conn.getConnectionStats().getReceivedMessageCounter());
            intent.putExtra(EXTRA_BLE_ERRORS, conn.getConnectionStats().getErrorsCounter());
            intent.putExtra(EXTRA_BLE_CONNECTION_TIME, conn.getConnectionStats().getConnectionDurationSec());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    public List<BluetoothDevice> getScannedDevices() {
        synchronized (scannedDevices) {
            return new ArrayList<>(scannedDevices);
        }
    }

    public List<String> getConnectedDeviceAddresses() {
        return new ArrayList<>(activeConnections.keySet());
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public List<String> getConnectedDeviceNames() {
        List<String> names = new ArrayList<>();
        for (String addr : activeConnections.keySet()) {
            String name = getDeviceName(addr);
            names.add(name != null ? name : addr);
        }
        return names;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private String getDeviceName(String address) {
        DeviceConnection conn = activeConnections.get(address);
        if (conn != null && conn.getDevice() != null) {
            String name = conn.getDevice().getName();
            if (name != null)
                return name;
        }

        synchronized (scannedDevices) {
            for (BluetoothDevice device : scannedDevices) {
                if (device.getAddress().equals(address)) {
                    return device.getName();
                }
            }
        }

        return connectionPersistence.getLastDeviceName(address);
    }

    public BleConnectionStatus getConnectionStatusForDevice(String deviceAddress) {
        return isConnectedToDevice(deviceAddress) ? CONNECTED : DISCONNECTED;
    }
}