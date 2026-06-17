package com.alextim.lora.client.ble.constants;

public class BleActions {

    /**
     * Broadcast action: отправляется при изменении статуса подключения BLE.
     * Используется в основном для обновления UI в фрагментах (ParametersFragment, RealtimeMonitorFragment).
     * Содержит EXTRA_BLE_STATUS, EXTRA_BLE_DEVICE_NAME, EXTRA_BLE_DEVICE_ADDRESS.
     *
     * Пример использования:
     * - ParametersFragment: отображает текущий статус подключения
     * - BluetoothService: информирует UI о состоянии соединения
     */
    public static final String ACTION_BLE_STATUS_UPDATED = "com.alextim.lora.ACTION_BLE_STATUS_UPDATE";

    /**
     * Broadcast action: отправляется при изменении уровня сигнала BLE (RSSI).
     * Используется для отображения силы сигнала в пользовательском интерфейсе.
     * Содержит EXTRA_BLE_RSSI.
     *
     * Пример использования:
     * - RealtimeMonitorFragment: обновляет отображение BLE RSSI
     * - BluetoothService: периодически отправляет текущее значение RSSI
     */
    public static final String ACTION_BLE_RSSI_UPDATED = "com.alextim.lora.ACTION_BLE_RSSI_UPDATE";

    /**
     * Broadcast action: отправляется при обновлении статистики BLE-соединения.
     * Используется для отображения статистики подключения в разделе Параметров.
     * Содержит EXTRA_BLE_ERRORS, EXTRA_BLE_RECEIVED_COUNT, EXTRA_BLE_SENT_COUNT, EXTRA_BLE_CONNECTION_TIME.
     *
     * Пример использования:
     * - ParametersFragment: обновляет статистику подключения
     * - BluetoothService: отправляет статистику при изменении
     */
    public static final String ACTION_BLE_STATS_UPDATED = "com.alextim.lora.ACTION_BLE_STATS_UPDATE";
}
