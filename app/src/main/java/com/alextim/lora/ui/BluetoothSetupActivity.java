package com.alextim.lora.ui;

import static com.alextim.lora.client.ble.constants.BleActions.ACTION_BLE_STATUS_UPDATED;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_DEVICE_ADDRESS;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_DEVICE_NAME;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_STATUS;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.R;
import com.alextim.lora.client.ble.BluetoothService;
import com.alextim.lora.client.ble.BluetoothService.LocalBinder;

public class BluetoothSetupActivity extends BluetoothSetupActivityBase {

    BluetoothService bluetoothService;
    boolean serviceBound = false;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "BroadcastReceiver onReceive " + action);

            if (ACTION_BLE_STATUS_UPDATED.equals(action)) {
                String statusString = intent.getStringExtra(EXTRA_BLE_STATUS);
                String deviceName = intent.getStringExtra(EXTRA_BLE_DEVICE_NAME);
                String deviceAddress = intent.getStringExtra(EXTRA_BLE_DEVICE_ADDRESS);

                Log.d(TAG, "BroadcastReceiver onReceive status " + statusString + " for device " + deviceName + " (" + deviceAddress + ")");

                runOnUiThread(() -> updateStatusBasedOnAllDevices());
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            bluetoothService = binder.getService();
            serviceBound = true;
            Log.d(TAG, "Service connected");

            runOnUiThread(() -> updateStatusBasedOnAllDevices());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            serviceBound = false;
            Log.d(TAG, "Service disconnected");

            runOnUiThread(() -> updateStatusBasedOnAllDevices());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_setup);

        initViews();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            initializeComponents();
        }
    }

    void initializeComponents() {
        setupViewPager();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BLE_STATUS_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        Intent serviceIntent = new Intent(this, BluetoothService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        setupBackButtonHandler();
    }

    private void updateStatusBasedOnAllDevices() {
        if (bluetoothService != null) {
            int connectedCount = bluetoothService.getConnectedDeviceAddresses().size();
            if (connectedCount == 0) {
                connectionStatus.setText("Отключено");
                connectionStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                String statusText = "Подключено к " + connectedCount + (connectedCount > 1 ? " устройствам" : " устройству");
                connectionStatus.setText(statusText);
                connectionStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        } else {
            connectionStatus.setText("Отключено");
            connectionStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

    @Override
    protected void onDestroy() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver was not registered or already unregistered", e);
        }

        if (serviceBound) {
            try {
                unbindService(serviceConnection);
                serviceBound = false;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Service was not bound or already unbound", e);
            }
        }

        super.onDestroy();
    }

    private void setupBackButtonHandler() {
        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            @Override
            public void handleOnBackPressed() {
                if (isTaskRoot()) {
                    new AlertDialog.Builder(BluetoothSetupActivity.this)
                            .setTitle("Выход из приложения")
                            .setMessage(
                                    "Вы действительно хотите выйти?\n\n" +
                                            "При выходе Bluetooth-соединение будет закрыто и данные с устройства перестанут поступать.\n\n" +
                                            "Вы можете свернуть приложение, оно продолжит работу в фоне."
                            )
                            .setPositiveButton("Выйти", (dialog, which) -> {
                                stopBluetoothService();
                                BluetoothSetupActivity.this.finish();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                } else {
                    setEnabled(false);
                    BluetoothSetupActivity.this.onBackPressed();
                }
            }
        });
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void stopBluetoothService() {
        if (bluetoothService != null && serviceBound) {
            bluetoothService.stopWithNotification();

            unbindService(serviceConnection);
            serviceBound = false;
            bluetoothService = null;
        } else {
            Intent serviceIntent = new Intent(this, BluetoothService.class);
            stopService(serviceIntent);
        }

        Log.d("MainActivity", "Запрос на остановку Bluetooth-сервиса с уведомлением отправлен");
    }
}