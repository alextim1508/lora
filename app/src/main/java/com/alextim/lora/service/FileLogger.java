package com.alextim.lora.service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileLogger {

    private static final String TAG = "FileLogger";
    private static String logFilePath;
    private static final Object lock = new Object();

    public static void init(Context context, String baseFileName) {
        if (baseFileName == null || baseFileName.trim().isEmpty()) {
            baseFileName = "app_log";
        }

        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null) {
            File logDir = new File(documentsDir, "Lora_logs");
            if (!logDir.exists()) {
                boolean dirCreated = logDir.mkdirs();
                if (!dirCreated) {
                    Log.e(TAG, "Не удалось создать директорию для логов: " + logDir.getAbsolutePath());
                    logFilePath = null;
                    return;
                } else {
                    Log.d(TAG, "Директория для логов создана: " + logDir.getAbsolutePath());
                }
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileNameWithTimestamp = baseFileName + "_" + timestamp + ".txt";
            logFilePath = new File(logDir, fileNameWithTimestamp).getAbsolutePath();
            Log.i(TAG, "FileLogger инициализирован. Новый файл лога: " + logFilePath);

            try {
                File logFile = new File(logFilePath);
                if (logFile.exists()) {
                    logFile.delete();
                    Log.w(TAG, "Файл с таким именем уже существовал (странно), удален: " + logFilePath);
                }
                boolean created = logFile.createNewFile();
                if (!created) {
                    Log.e(TAG, "Не удалось создать файл лога: " + logFilePath);
                    logFilePath = null;
                }
            } catch (IOException e) {
                Log.e(TAG, "Ошибка при создании файла лога: " + logFilePath, e);
                logFilePath = null;
            }
        } else {
            Log.e(TAG, "Не удалось получить директорию Documents для лога");
        }
    }


    public static void logToFile(String level, String tag, String message) {
        if (logFilePath == null) {
            return;
        }

        synchronized (lock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                writer.write(String.format("[%s] [%s] %s: %s%n", timestamp, level, tag, message));
            } catch (IOException e) {
                System.err.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date()) + "] " +
                        "Ошибка записи в файл лога: " + e.getMessage());
            }
        }
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
        logToFile("DEBUG", tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
        logToFile("INFO", tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
        logToFile("WARN", tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
        logToFile("ERROR", tag, msg);
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
        logToFile("VERBOSE", tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
        logToFile("ERROR", tag, msg + (tr != null ? " " + Log.getStackTraceString(tr) : ""));
    }
}