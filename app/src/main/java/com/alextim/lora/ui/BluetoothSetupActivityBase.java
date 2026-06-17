package com.alextim.lora.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.alextim.lora.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public abstract class BluetoothSetupActivityBase extends AppCompatActivity {
    static final String TAG = "BluetoothSetupActivity";
    static final int REQUEST_BLUETOOTH_PERMISSIONS = 1002;
    static final int REQUEST_MANAGE_ALL_FILES_ACCESS = 1004;

    TabLayout tabLayout;
    ViewPager2 viewPager;
    MyPagerAdapter pagerAdapter;
    TextView connectionStatus;


    abstract void initializeComponents();

    void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        connectionStatus = findViewById(R.id.connectionStatus);
    }

    void setupViewPager() {
        pagerAdapter = new MyPagerAdapter(this, getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Монитор");
                    break;
                case 1:
                    tab.setText("Соединение");
                    break;
                case 2:
                    tab.setText("Управление");
                    break;
            }
        }).attach();
    }


    private static class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(Context context, FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new DataFragment();
                case 1:
                    return new ConnectionFragment();
                case 2:
                    return new ManagementFragment();
                default:
                    return new ConnectionFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MANAGE_ALL_FILES_ACCESS) {
            boolean granted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
            if (granted) {
                Log.d(TAG, "All files access permission granted via settings.");
                recreate();
            } else {
                Log.w(TAG, "All files access permission NOT granted via settings.");
                Toast.makeText(this, "All files access permission is required to save DB files to Downloads folder.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "Bluetooth/Location/Notifications permissions granted");
                recreate(); // Перезапуск активити для корректной инициализации
            } else {
                Toast.makeText(this, "Permissions required for Bluetooth and notifications operation", Toast.LENGTH_LONG).show();
            }
        }
    }

    void requestPermissions() {
        List<String> permissionsList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_MANAGE_ALL_FILES_ACCESS);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsList.isEmpty()) {
            String[] permissions = permissionsList.toArray(new String[0]);
            requestPermissions(permissions, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            // Все разрешения уже есть — инициализировать компоненты
            initializeComponents();
        }
    }

    boolean checkPermissions() {
        boolean basicPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            basicPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            basicPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        boolean notificationPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        boolean storagePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storagePermission = Environment.isExternalStorageManager();
        } else {
            storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        Log.d(TAG, "Basic permissions: " + basicPermissions + ", Notification permission: " + notificationPermission + ", Storage permission: " + storagePermission);
        return basicPermissions && notificationPermission && storagePermission;
    }

}
