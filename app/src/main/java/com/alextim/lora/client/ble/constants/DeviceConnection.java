package com.alextim.lora.client.ble.constants;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class DeviceConnection {
    private final BluetoothGatt gatt;
    private final BluetoothDevice device;
    private final ConnectionStats connectionStats;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private volatile boolean connected = false;

    public DeviceConnection(BluetoothGatt gatt, ConnectionStats stats, BluetoothDevice device) {
        this.gatt = gatt;
        this.connectionStats = stats;
        this.device = device;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }


    public BluetoothDevice getDevice() {
        return device;
    }

    public void setNotifyCharacteristic(BluetoothGattCharacteristic notifyCharacteristic) {
        this.notifyCharacteristic = notifyCharacteristic;
    }

    public BluetoothGattCharacteristic getNotifyCharacteristic() {
        return notifyCharacteristic;
    }

    public void setWriteCharacteristic(BluetoothGattCharacteristic writeCharacteristic) {
        this.writeCharacteristic = writeCharacteristic;
    }

    public BluetoothGattCharacteristic getWriteCharacteristic() {
        return writeCharacteristic;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public ConnectionStats getConnectionStats() {
        return connectionStats;
    }
}