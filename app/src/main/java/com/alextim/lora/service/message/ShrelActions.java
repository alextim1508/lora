package com.alextim.lora.service.message;

public class ShrelActions {

    // --- СОБЫТИЯ (Notify от устройств) ---
    /**
     * Broadcast action: событие с данными измерений от ШАРМ БД.
     * Отправляется при получении данных измерений от ШАРМ БД.
     * Содержит сериализованный объект MeasDataBDSharm в EXTRA_SHREL_BD_MEAS_DATA_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки данных от ШАРМ БД
     * - RealtimeMonitorFragment: обновляет график и текущее значение
     */
    public static final String ACTION_SHREL_BD_SHARM_DATA_EVENT = "com.alextim.bluetest.ACTION_SHREL_BD_SHARM_DATA_EVENT";

    /**
     * Broadcast action: информационное событие от ШАРМ БД.
     * Отправляется при получении служебной информации от ШАРМ БД (напряжение, температура и т.д.).
     * Содержит сериализованный объект MeasInfoBDSharm в EXTRA_SHREL_BD_MEAS_INFO_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки информации от ШАРМ БД
     * - RealtimeMonitorFragment: обновляет отображение информации о ШАРМ БД
     */
    public static final String ACTION_SHREL_BD_SHARM_INFO_EVENT = "com.alextim.bluetest.ACTION_SHREL_BD_SHARM_INFO_EVENT";

    /**
     * Broadcast action: событие с данными измерений от ШРЭЛ БРИ.
     * Отправляется при получении данных измерений от ШРЭЛ БРИ.
     * Содержит сериализованный объект MeasDataBRIShrel в EXTRA_SHREL_BRI_MEAS_DATA_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки данных от ШРЭЛ БРИ
     * - RealtimeMonitorFragment: обновляет график и текущее значение
     */
    public static final String ACTION_SHREL_BRISHREL_DATA_EVENT = "com.alextim.bluetest.ACTION_SHREL_BRISHREL_DATA_EVENT";

    /**
     * Broadcast action: информационное событие от ШРЭЛ БРИ.
     * Отправляется при получении служебной информации от ШРЭЛ БРИ (напряжение, температура и т.д.).
     * Содержит сериализованный объект MeasInfoBRIShrel в EXTRA_SHREL_BRI_MEAS_INFO_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки информации от ШРЭЛ БРИ
     * - RealtimeMonitorFragment: обновляет отображение информации о ШРЭЛ БРИ
     */
    public static final String ACTION_SHREL_BRISHREL_INFO_EVENT = "com.alextim.bluetest.ACTION_SHREL_BRISHREL_INFO_EVENT";

    /**
     * Broadcast action: событие с данными измерений от ШРЭЛ ПУ.
     * Отправляется при получении данных измерений от ШРЭЛ ПУ (включая данные от ШРЭЛ БРИ через ПУ).
     * Содержит сериализованный объект PUDataShrel в EXTRA_SHREL_PU_MEAS_DATA_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки данных от ШРЭЛ ПУ
     * - RealtimeMonitorFragment: обновляет график и текущее значение
     */
    public static final String ACTION_SHREL_PU_SHREL_DATA_EVENT = "com.alextim.bluetest.ACTION_SHREL_PU_SHREL_DATA_EVENT";

    /**
     * Broadcast action: информационное событие от ШРЭЛ ПУ.
     * Отправляется при получении служебной информации от ШРЭЛ ПУ.
     * Содержит сериализованный объект PUInfoShrel в EXTRA_SHREL_PU_MEAS_INFO_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки информации от ШРЭЛ ПУ
     * - RealtimeMonitorFragment: обновляет отображение информации о ШРЭЛ ПУ
     */
    public static final String ACTION_SHREL_PU_SHREL_INFO_EVENT = "com.alextim.bluetest.ACTION_SHREL_PU_SHREL_INFO_EVENT";


    // --- ОТВЕТЫ НА КОМАНДЫ Read ---
    // General Commands
    /**
     * Broadcast action: ответ на команду получения версии устройства.
     * Содержит EXTRA_SHREL_VERSION_STRING.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает версию устройства
     */
    public static final String ACTION_SHREL_GENERAL_GET_VERSION_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GENERAL_GET_VERSION_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения характеристик устройства.
     * Содержит EXTRA_SHREL_DEVICE_CHARACTERISTICS_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает характеристики устройства
     */
    public static final String ACTION_SHREL_GENERAL_GET_CHARACTERISTICS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GENERAL_GET_CHARACTERISTICS_RESPONSE";


    // Settings (Read responses)
    /**
     * Broadcast action: ответ на команду получения настроек измерений.
     * Содержит EXTRA_SHREL_MEAS_SETTINGS_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает настройки измерений
     */
    public static final String ACTION_SHREL_GET_MEAS_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_MEAS_SETTINGS_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки настроек измерений.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки настроек
     */
    public static final String ACTION_SHREL_SET_MEAS_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_MEAS_SETTINGS_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения пользовательских настроек ШАРМ БД.
     * Содержит EXTRA_SHREL_USER_SETTINGS_BD_SHARM_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает пользовательские настройки ШАРМ БД
     */
    public static final String ACTION_SHREL_GET_USER_SETTINGS_BD_SHARM_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_USER_SETTINGS_BD_SHARM_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки пользовательских настроек ШАРМ БД.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки настроек
     */
    public static final String ACTION_SHREL_SET_USER_SETTINGS_BD_SHARM_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_USER_SETTINGS_BD_SHARM_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения пользовательских настроек ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_USER_SETTINGS_BRI_SHREL_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает пользовательские настройки ШРЭЛ БРИ
     */
    public static final String ACTION_SHREL_GET_USER_SETTINGS_BRI_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_USER_SETTINGS_BRI_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки пользовательских настроек ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки настроек
     */
    public static final String ACTION_SHREL_SET_USER_SETTINGS_BRI_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_USER_SETTINGS_BRI_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения пользовательских настроек ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_USER_SETTINGS_PU_SHREL_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает пользовательские настройки ШРЭЛ ПУ
     */
    public static final String ACTION_SHREL_GET_USER_SETTINGS_PU_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_USER_SETTINGS_PU_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки пользовательских настроек ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки настроек
     */
    public static final String ACTION_SHREL_SET_USER_SETTINGS_PU_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_USER_SETTINGS_PU_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения настроек УКВ.
     * Содержит EXTRA_SHREL_UHF_SETTINGS_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает настройки УКВ
     */
    public static final String ACTION_SHREL_GET_UHF_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_UHF_SETTINGS_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки настроек УКВ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки настроек
     */
    public static final String ACTION_SHREL_SET_UHF_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_UHF_SETTINGS_RESPONSE";


    // Params (Read responses)
    /**
     * Broadcast action: ответ на команду получения параметров измерений ШАРМ БД.
     * Содержит EXTRA_SHREL_MEAS_PARAMS_BD_SHARM_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает параметры измерений ШАРМ БД
     */
    public static final String ACTION_SHREL_GET_MEAS_PARAMS_BD_SHARM_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_MEAS_PARAMS_BD_SHARM_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки параметров измерений ШАРМ БД.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки параметров
     */
    public static final String ACTION_SHREL_SET_MEAS_PARAMS_BD_SHARM_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_MEAS_PARAMS_BD_SHARM_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения пользовательских параметров ШАРМ БД.
     * Содержит EXTRA_SHREL_USER_PARAMS_BD_SHARM_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает пользовательские параметры ШАРМ БД
     */
    public static final String ACTION_SHREL_GET_USER_PARAMS_BD_SHARM_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_USER_PARAMS_BD_SHARM_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки пользовательских параметров ШАРМ БД.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки параметров
     */
    public static final String ACTION_SHREL_SET_USER_PARAMS_BD_SHARM_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_USER_PARAMS_BD_SHARM_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения параметров измерений ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_MEAS_PARAMS_BRI_SHREL_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает параметры измерений ШРЭЛ БРИ
     */
    public static final String ACTION_SHREL_GET_MEAS_PARAMS_BRI_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_MEAS_PARAMS_BRI_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки параметров измерений ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки параметров
     */
    public static final String ACTION_SHREL_SET_MEAS_PARAMS_BRI_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_MEAS_PARAMS_BRI_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения пользовательских параметров ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_USER_PARAMS_BRI_SHREL_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает пользовательские параметры ШРЭЛ БРИ
     */
    public static final String ACTION_SHREL_GET_USER_PARAMS_BRI_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_USER_PARAMS_BRI_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки пользовательских параметров ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки параметров
     */
    public static final String ACTION_SHREL_SET_USER_PARAMS_BRI_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_USER_PARAMS_BRI_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения параметров измерений ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_MEAS_PARAMS_PU_SHREL_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает параметры измерений ШРЭЛ ПУ
     */
    public static final String ACTION_SHREL_GET_MEAS_PARAMS_PU_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_MEAS_PARAMS_PU_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки параметров измерений ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки параметров
     */
    public static final String ACTION_SHREL_SET_MEAS_PARAMS_PU_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_MEAS_PARAMS_PU_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду получения пользовательских параметров ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_USER_PARAMS_PU_SHREL_OBJ_BYTES.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - ParametersFragment: отображает пользовательские параметры ШРЭЛ ПУ
     */
    public static final String ACTION_SHREL_GET_USER_PARAMS_PU_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_USER_PARAMS_PU_SHREL_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки пользовательских параметров ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки параметров
     */
    public static final String ACTION_SHREL_SET_USER_PARAMS_PU_SHREL_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_USER_PARAMS_PU_SHREL_RESPONSE";


    /**
     * Broadcast action: ответ на команду установки аппаратных настроек ШАРМ БД.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки аппаратных настроек
     */
    public static final String ACTION_SHREL_SET_SHARM_BD_HARD_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_SHARM_BD_HARD_SETTINGS_RESPONSE";
    public static final String ACTION_SHREL_GET_SHARM_BD_HARD_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_SHARM_BD_HARD_SETTINGS_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки аппаратных настроек ШРЭЛ БРИ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки аппаратных настроек
     */
    public static final String ACTION_SHREL_SET_SHREL_BRI_HARD_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_SHREL_BRI_HARD_SETTINGS_RESPONSE";
    public static final String ACTION_SHREL_GET_SHREL_BRI_HARD_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_SHREL_BRI_HARD_SETTINGS_RESPONSE";

    /**
     * Broadcast action: ответ на команду установки аппаратных настроек ШРЭЛ ПУ.
     * Содержит EXTRA_SHREL_SET_COMMAND_ERROR_CODE.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет после обработки ответа
     * - RealtimeMonitorFragment: обрабатывает результат установки аппаратных настроек
     */
    public static final String ACTION_SHREL_SET_SHREL_PU_HARD_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_SET_SHREL_PU_HARD_SETTINGS_RESPONSE";
    public static final String ACTION_SHREL_GET_SHREL_PU_HARD_SETTINGS_RESPONSE = "com.alextim.bluetest.ACTION_SHREL_GET_SHREL_PU_HARD_SETTINGS_RESPONSE";

    // --- ОШИБКИ / НЕИЗВЕСТНЫЕ ---
    /**
     * Broadcast action: сообщение об ошибке при обработке данных.
     * Отправляется при возникновении ошибки в MessageProcessingService.
     * Содержит EXTRA_SHREL_EXCEPTION_MESSAGE и EXTRA_SHREL_RAW_MESSAGE_HEX.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет при обнаружении ошибки
     * - RealtimeMonitorFragment: логирует ошибку
     */
    public static final String ACTION_SHREL_ERROR_MESSAGE = "com.alextim.bluetest.ACTION_SHREL_ERROR_MESSAGE";

    /**
     * Broadcast action: неизвестное сообщение от устройства.
     * Отправляется при получении сообщения, которое не может быть обработано.
     * Содержит EXTRA_SHREL_RAW_MESSAGE_HEX.
     *
     * Пример использования:
     * - MessageProcessingService: отправляет при получении неизвестного сообщения
     * - RealtimeMonitorFragment: логирует неизвестное сообщение
     */
    public static final String ACTION_SHREL_UNKNOWN_MESSAGE = "com.alextim.bluetest.ACTION_SHREL_UNKNOWN_MESSAGE";

    public static final String ACTION_SHREL_HANDLE_STATS_UPDATE = "com.alextim.bluetest.ACTION_SHREL_HANDLE_STATS_UPDATE";
}