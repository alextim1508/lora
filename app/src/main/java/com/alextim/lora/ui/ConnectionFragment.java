package com.alextim.lora.ui;

import static com.alextim.lora.client.ble.constants.BleActions.ACTION_BLE_STATUS_UPDATED;
import static com.alextim.lora.client.ble.constants.BleConnectionStatus.CONNECTED;
import static com.alextim.lora.client.ble.constants.BleConnectionStatus.DISCONNECTED;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_DEVICE_ADDRESS;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_DEVICE_NAME;
import static com.alextim.lora.client.ble.constants.BleExtras.EXTRA_BLE_STATUS;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
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
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectionFragment extends ConfigFragment {

    private static final String TAG = "ConnectionFragment";

    private ProgressBar scanProgressBar;
    private Button refreshButton;
    private Button disconnectAllButton;
    private ListView connectedDevicesListView;

    private ConnectedDevicesAdapter connectedDevicesAdapter;
    private final List<DeviceInfo> connectedDevicesList = new ArrayList<>();

    private final BroadcastReceiver connectionStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_BLE_STATUS_UPDATED.equals(action)) {
                String statusString = intent.getStringExtra(EXTRA_BLE_STATUS);
                String deviceName = intent.getStringExtra(EXTRA_BLE_DEVICE_NAME);
                String deviceAddress = intent.getStringExtra(EXTRA_BLE_DEVICE_ADDRESS);

                if (deviceAddress != null) {
                    if (CONNECTED.name().equals(statusString)) {
                        addConnectedDevice(deviceAddress, deviceName);
                    } else if (DISCONNECTED.name().equals(statusString)) {
                        removeConnectedDevice(deviceAddress);
                    }
                }
            }
        }
    };


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(ACTION_BLE_STATUS_UPDATED);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(connectionStatusReceiver, filter);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connection, container, false); // Используем новую разметку

        initViews(view);
        setupAdapters();


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(connectionStatusReceiver);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    void setupListeners() {
        refreshButton.setOnClickListener(v -> {
            if (bluetoothService != null) {
                showDeviceSelectionDialog(scanProgressBar);
            } else {
                Toast.makeText(getContext(), "Service not bound", Toast.LENGTH_SHORT).show();
            }
        });

        disconnectAllButton.setOnClickListener(v -> {
            if (bluetoothService != null) {
                bluetoothService.disconnectAllDevices();
            } else {
                Toast.makeText(getContext(), "Service not bound", Toast.LENGTH_SHORT).show();
            }
        });

    }

    void initViews(View view) {
        scanProgressBar = view.findViewById(R.id.scanProgressBar);
        refreshButton = view.findViewById(R.id.refreshButton);
        disconnectAllButton = view.findViewById(R.id.disconnectAllButton);
        connectedDevicesListView = view.findViewById(R.id.connectedDevicesListView);
    }

    @Override
    void registerBroadcastReceiver() {
    }

    @Override
    void unregisterBroadcastReceiver() {
    }

    @Override
    void updateUIFromService() {
    }

    @Override
    void updateUIFromSettings() {
    }

    @Override
    void saveCurrentState(Context context) {
    }

    @Override
    void restoreState(Context context) {
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupAdapters() {
        connectedDevicesAdapter = new ConnectedDevicesAdapter(requireContext(), connectedDevicesList, (address) -> {
            if (bluetoothService != null) {
                bluetoothService.disconnectDevice(address);
                bluetoothService.setUserInitiatedDisconnect(address, true);
            } else {
                Toast.makeText(getContext(), "Service not bound", Toast.LENGTH_SHORT).show();
            }
        });
        connectedDevicesListView.setAdapter(connectedDevicesAdapter);
    }


    private void addConnectedDevice(String address, String name) {
        DeviceInfo newDevice = new DeviceInfo(address, name);
        if (!connectedDevicesList.contains(newDevice)) {
            connectedDevicesList.add(newDevice);
            Log.d(TAG, "Added to connected list: " + address);

            requireActivity().runOnUiThread(() -> {
                connectedDevicesAdapter.notifyDataSetChanged();
            });
        }
    }

    private void removeConnectedDevice(String address) {
        DeviceInfo deviceToRemove = new DeviceInfo(address, null);
        connectedDevicesList.remove(deviceToRemove);
        Log.d(TAG, "Removed from connected list: " + address);

        requireActivity().runOnUiThread(() -> {
            connectedDevicesAdapter.notifyDataSetChanged();
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    public void showDeviceSelectionDialog(ProgressBar progressBar) {
        if (bluetoothService != null) {
            if (!isBleEnabled()) {
                Toast.makeText(getContext(), "Bluetooth не включен", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            bluetoothService.startScan();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressBar.setVisibility(View.GONE);

                List<BluetoothDevice> devices = bluetoothService.getScannedDevices();
                if (!devices.isEmpty()) {
                    showMultiDeviceSelectionDialog(devices);
                } else {
                    Toast.makeText(getContext(), "Устроства не найдены", Toast.LENGTH_SHORT).show();
                }
            }, 5_000);
        } else {

            Toast.makeText(getContext(), "Service not bound", Toast.LENGTH_SHORT).show();

        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void showMultiDeviceSelectionDialog(List<BluetoothDevice> devices) {
        String[] deviceNames = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            BluetoothDevice device = devices.get(i);
            String name = device.getName() != null ? device.getName() : "Unknown Device";
            deviceNames[i] = name + " (" + device.getAddress() + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_multi_select_devices, null);
        builder.setView(dialogView);

        ListView listView = dialogView.findViewById(R.id.dialogDevicesListView);
        Button okButton = dialogView.findViewById(R.id.dialogOkButton);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);

        MultiDeviceSelectionAdapter adapter = new MultiDeviceSelectionAdapter(getContext(), devices);
        listView.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(v -> {
            Set<String> selectedAddresses = adapter.getSelectedAddresses();
            Log.d(TAG, "OK clicked, selected " + selectedAddresses.size() + " devices.");

            if (!selectedAddresses.isEmpty()) {
                List<String> selectedAddressList = new ArrayList<>(selectedAddresses);
                if (bluetoothService != null) {
                    bluetoothService.connectToMultipleDevices(selectedAddressList);
                } else {
                    Toast.makeText(getContext(), "Service not bound", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "No devices selected.");
                Toast.makeText(getContext(), "No devices selected", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "Cancel clicked.");
            dialog.dismiss();
        });

        dialog.show();
    }

    private static class DeviceInfo {
        String address;
        String name;

        DeviceInfo(String address, String name) {
            this.address = address;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeviceInfo)) return false;
            DeviceInfo deviceInfo = (DeviceInfo) o;
            return address.equals(deviceInfo.address);
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }
    }

    private static class ConnectedDevicesAdapter extends ArrayAdapter<DeviceInfo> {
        private final OnDeviceDisconnectListener disconnectListener;

        interface OnDeviceDisconnectListener {
            void onDisconnect(String deviceAddress);
        }

        public ConnectedDevicesAdapter(@NonNull Context context, @NonNull List<DeviceInfo> objects, OnDeviceDisconnectListener listener) {
            super(context, 0, objects);
            this.disconnectListener = listener;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            DeviceInfo device = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_connected_device, parent, false); // Новая разметка для элемента списка
            }

            TextView nameView = convertView.findViewById(R.id.deviceNameTextView);
            TextView addressView = convertView.findViewById(R.id.deviceAddressTextView);
            Button disconnectButton = convertView.findViewById(R.id.disconnectButton);

            nameView.setText(device.name != null ? device.name : "Unknown Device (" + device.address + ")");
            addressView.setText(device.address);

            disconnectButton.setOnClickListener(v -> {
                if (disconnectListener != null && device.address != null) {
                    disconnectListener.onDisconnect(device.address);
                }
            });

            return convertView;
        }
    }

    private static class MultiDeviceSelectionAdapter extends BaseAdapter {
        private final Context context;
        private final List<BluetoothDevice> devices;
        private final Set<String> selectedAddresses = new HashSet<>();

        public MultiDeviceSelectionAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_device_checkbox, parent, false);
                holder = new ViewHolder();
                holder.nameTextView = convertView.findViewById(R.id.deviceNameTextView);
                holder.addressTextView = convertView.findViewById(R.id.deviceAddressTextView);
                holder.checkBox = convertView.findViewById(R.id.deviceCheckBox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = devices.get(position);
            String name = device.getName() != null ? device.getName() : "Unknown Device";
            String address = device.getAddress();

            holder.nameTextView.setText(name);
            holder.addressTextView.setText(address);
            holder.checkBox.setChecked(selectedAddresses.contains(address));

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAddresses.add(address);
                } else {
                    selectedAddresses.remove(address);
                }
            });

            return convertView;
        }

        public Set<String> getSelectedAddresses() {
            return new HashSet<>(selectedAddresses);
        }

        static class ViewHolder {
            TextView nameTextView;
            TextView addressTextView;
            CheckBox checkBox;
        }
    }

    public boolean isBleEnabled() {
        return bluetoothService != null && bluetoothService.isEnabled();
    }
}