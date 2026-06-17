package com.alextim.lora.client.ble.constants;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.alextim.lora.service.protocol.BleProtocolParser;

public class DeviceConnection {
    private final BluetoothGatt gatt;
    private final BluetoothDevice device;
    private final ConnectionStats connectionStats;
    private final BleProtocolParser protocolParser;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private volatile boolean connected = false;

    public DeviceConnection(BluetoothGatt gatt, BluetoothDevice device, ConnectionStats stats) {
        this.gatt = gatt;
        this.device = device;
        this.connectionStats = stats;
        this.protocolParser = new BleProtocolParser();
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

    public BleProtocolParser getProtocolParser() {
        return protocolParser;
    }
}