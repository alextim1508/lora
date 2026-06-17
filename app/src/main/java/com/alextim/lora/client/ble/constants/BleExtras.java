package com.alextim.lora.client.ble.constants;

public class BleExtras {
    /**
     * Extra для ACTION_BLE_STATUS_UPDATE: текущий статус BLE-соединения.
     * Тип: int (значения из BleConnectionStatus)
     *
     * Пример использования:
     * - BluetoothService: устанавливает текущий статус
     * - ParametersFragment: отображает статус в UI
     */
    public static final String EXTRA_BLE_STATUS = "EXTRA_BLE_STATUS";

    /**
     * Extra для ACTION_BLE_STATUS_UPDATE: имя подключенного BLE-устройства.
     * Тип: String
     *
     * Пример использования:
     * - BluetoothService: устанавливает имя устройства при подключении
     * - ParametersFragment: отображает имя в UI
     */
    public static final String EXTRA_BLE_DEVICE_NAME = "EXTRA_BLE_DEVICE_NAME";

    /**
     * Extra для ACTION_BLE_STATUS_UPDATE: MAC-адрес подключенного BLE-устройства.
     * Тип: String
     *
     * Пример использования:
     * - BluetoothService: устанавливает адрес при подключении
     * - ParametersFragment: отображает адрес в UI
     */
    public static final String EXTRA_BLE_DEVICE_ADDRESS = "EXTRA_BLE_DEVICE_ADDRESS";

    /**
     * Extra для ACTION_BLE_RSSI_UPDATE: уровень сигнала BLE в dBm.
     * Тип: int
     *
     * Пример использования:
     * - BluetoothService: отправляет текущее значение RSSI
     * - RealtimeMonitorFragment: обновляет отображение BLE RSSI
     */
    public static final String EXTRA_BLE_RSSI = "EXTRA_BLE_RSSI";

    /**
     * Extra для ACTION_BLE_STATS_UPDATE: количество ошибок при передаче данных.
     * Тип: int
     *
     * Пример использования:
     * - BluetoothService: обновляет счетчик ошибок
     * - ParametersFragment: отображает статистику в UI
     */
    public static final String EXTRA_BLE_ERRORS = "EXTRA_BLE_ERRORS";

    /**
     * Extra для ACTION_BLE_STATS_UPDATE: количество успешно полученных пакетов.
     * Тип: int
     *
     * Пример использования:
     * - BluetoothService: обновляет счетчик полученных пакетов
     * - ParametersFragment: отображает статистику в UI
     */
    public static final String EXTRA_BLE_RECEIVED_COUNT = "EXTRA_BLE_RECEIVED_COUNT";

    /**
     * Extra для ACTION_BLE_STATS_UPDATE: количество успешно отправленных пакетов.
     * Тип: int
     *
     * Пример использования:
     * - BluetoothService: обновляет счетчик отправленных пакетов
     * - ParametersFragment: отображает статистику в UI
     */
    public static final String EXTRA_BLE_SENT_COUNT = "EXTRA_BLE_SENT_COUNT";

    /**
     * Extra для ACTION_BLE_STATS_UPDATE: время текущего подключения в миллисекундах.
     * Тип: long
     *
     * Пример использования:
     * - BluetoothService: обновляет время подключения
     * - ParametersFragment: отображает длительность подключения
     */
    public static final String EXTRA_BLE_CONNECTION_TIME = "EXTRA_BLE_CONNECTION_TIME";
}
