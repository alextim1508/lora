package com.alextim.lora.client.ble.constants;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.RequiresPermission;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPersistence {

    private static final String PREF_LORA_BLE_SERVICE = "com.alextim.shramel.PREF_SHRAMEL_BLE_SERVICE";
    private static final String LAST_DEVICE_ADDRESSES_KEY = "last_device_addresses_set";
    private static final String LAST_DEVICE_NAME_PREFIX = "last_device_name_";
    private static final String USER_INITIATED_DISCONNECT_PREFIX = "user_initiated_disconnect_";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private final Map<String, String> cachedDeviceNames = new ConcurrentHashMap<>();
    private final Map<String, Boolean> cachedUserInitiatedDisconnect = new ConcurrentHashMap<>();

    public ConnectionPersistence(Context context) {
        this.prefs = context.getSharedPreferences(PREF_LORA_BLE_SERVICE, Context.MODE_PRIVATE);
        this.editor = prefs.edit();

        loadCache();
    }

    private void loadCache() {
        Set<String> addresses = prefs.getStringSet(LAST_DEVICE_ADDRESSES_KEY, new HashSet<>());
        for (String address : addresses) {
            String name = prefs.getString(LAST_DEVICE_NAME_PREFIX + address, null);
            if (name != null) {
                cachedDeviceNames.put(address, name);
            }
            boolean isUserInitiated = prefs.getBoolean(USER_INITIATED_DISCONNECT_PREFIX + address, false);
            cachedUserInitiatedDisconnect.put(address, isUserInitiated);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void saveLastDevice(BluetoothDevice device) {
        if (device != null && device.getAddress() != null) {
            String address = device.getAddress();
            String name = device.getName();

            if (name != null) {
                cachedDeviceNames.put(address, name);
            }

            Set<String> addresses = prefs.getStringSet(LAST_DEVICE_ADDRESSES_KEY, new HashSet<>());

            Set<String> updatedAddresses = new HashSet<>(addresses);
            updatedAddresses.add(address);


            editor.putStringSet(LAST_DEVICE_ADDRESSES_KEY, updatedAddresses);

            editor.putString(LAST_DEVICE_NAME_PREFIX + address, name);
            editor.apply();

            cachedUserInitiatedDisconnect.put(address, false);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void removeLastDevice(String deviceAddress) {
        if (deviceAddress != null) {
            // Обновляем кэш
            cachedDeviceNames.remove(deviceAddress);
            cachedUserInitiatedDisconnect.remove(deviceAddress);

            // Получаем текущий набор адресов
            Set<String> addresses = prefs.getStringSet(LAST_DEVICE_ADDRESSES_KEY, new HashSet<>());
            // Удаляем адрес
            Set<String> updatedAddresses = new HashSet<>(addresses);
            updatedAddresses.remove(deviceAddress);

            // Удаляем имя устройства
            editor.remove(LAST_DEVICE_NAME_PREFIX + deviceAddress);
            // Удаляем флаг отключения
            editor.remove(USER_INITIATED_DISCONNECT_PREFIX + deviceAddress);

            // Сохраняем обновленный набор адресов
            if (updatedAddresses.isEmpty()) {
                editor.remove(LAST_DEVICE_ADDRESSES_KEY);
            } else {
                editor.putStringSet(LAST_DEVICE_ADDRESSES_KEY, updatedAddresses);
            }
            editor.apply();
        }
    }


    public Set<String> getLastDeviceAddresses() {
        return prefs.getStringSet(LAST_DEVICE_ADDRESSES_KEY, new HashSet<>());
    }

    public String getLastDeviceName(String deviceAddress) {
        if (deviceAddress != null) {
            // Сначала проверяем кэш
            String cachedName = cachedDeviceNames.get(deviceAddress);
            if (cachedName != null) {
                return cachedName;
            }
            // Если в кэше нет, читаем из SharedPreferences
            String name = prefs.getString(LAST_DEVICE_NAME_PREFIX + deviceAddress, null);
            if (name != null) {
                cachedDeviceNames.put(deviceAddress, name); // Обновляем кэш
            }
            return name;
        }
        return null;
    }

    public boolean isUserInitiatedDisconnect(String deviceAddress) {
        if (deviceAddress != null) {
            Boolean cachedFlag = cachedUserInitiatedDisconnect.get(deviceAddress);
            if (cachedFlag != null) {
                return cachedFlag;
            }

            boolean flag = prefs.getBoolean(USER_INITIATED_DISCONNECT_PREFIX + deviceAddress, false);
            cachedUserInitiatedDisconnect.put(deviceAddress, flag);
            return flag;
        }
        return false;
    }

    public void setUserInitiatedDisconnect(String deviceAddress, boolean userInitiatedDisconnect) {
        if (deviceAddress != null) {
            cachedUserInitiatedDisconnect.put(deviceAddress, userInitiatedDisconnect);
            editor.putBoolean(USER_INITIATED_DISCONNECT_PREFIX + deviceAddress, userInitiatedDisconnect);
            editor.apply();
        }
    }

    public void setUserInitiatedDisconnectForAll(boolean userInitiatedDisconnect) {
        Set<String> addresses = getLastDeviceAddresses();
        for (String address : addresses) {
            setUserInitiatedDisconnect(address, userInitiatedDisconnect);
        }
    }

    public boolean isUserInitiatedDisconnectForAll() {
        Set<String> addresses = getLastDeviceAddresses();
        for (String address : addresses) {
            if (!isUserInitiatedDisconnect(address)) {
                return false;
            }
        }
        return !addresses.isEmpty();
    }
}