package com.alextim.lora.client.ble;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.alextim.lora.ui.BluetoothSetupActivity;


public abstract class BluetoothServiceBase extends Service {

    static final String TAG = "BluetoothService";
    static final String CHANNEL_NOTIFICATION_ID = "BleServiceChannel";
    static final int FOREGROUND_NOTIFICATION_ID = 1;

    static final int SERVICE_STOPPED_NOTIFICATION_ID = 4;

    @RequiresApi(api = Build.VERSION_CODES.O)
    void startForegroundService() {
        createNotificationChannels();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, BluetoothSetupActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle("Лора сервис запущен")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void createNotificationChannels() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_NOTIFICATION_ID,
                "Уведомления Bluetooth",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Стандартные уведомления о работе Bluetooth");

        notificationManager.createNotificationChannel(channel);
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void stopWithNotification() {
        showServiceStoppedNotification();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            stopForeground(true);
            stopSelf();
        }, 3000);
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showServiceStoppedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle("Шрамэл сервис остановлен")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true);

        Intent intent = new Intent(this, BluetoothSetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this).notify(SERVICE_STOPPED_NOTIFICATION_ID, builder.build());
    }
}