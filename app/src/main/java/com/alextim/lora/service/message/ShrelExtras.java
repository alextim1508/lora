package com.alextim.lora.service.message;

public class ShrelExtras {

    // --- Для событий (Notify) ---
    /**
     * Extra для ACTION_SHREL_BD_SHARM_DATA_EVENT: сериализованный объект MeasDataBDSharm.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект MeasDataBDSharm
     * - RealtimeMonitorFragment: десериализует и обрабатывает данные от ШАРМ БД
     */
    public static final String EXTRA_SHREL_BD_MEAS_DATA_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_BD_MEAS_DATA_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_BD_SHARM_INFO_EVENT: сериализованный объект MeasInfoBDSharm.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект MeasInfoBDSharm
     * - RealtimeMonitorFragment: десериализует и обрабатывает информацию о ШАРМ БД
     */
    public static final String EXTRA_SHREL_BD_MEAS_INFO_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_BD_MEAS_INFO_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_BRISHREL_DATA_EVENT: сериализованный объект MeasDataBRIShrel.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект MeasDataBRIShrel
     * - RealtimeMonitorFragment: десериализует и обрабатывает данные от ШРЭЛ БРИ
     */
    public static final String EXTRA_SHREL_BRI_MEAS_DATA_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_BRI_MEAS_DATA_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_BRISHREL_INFO_EVENT: сериализованный объект MeasInfoBRIShrel.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект MeasInfoBRIShrel
     * - RealtimeMonitorFragment: десериализует и обрабатывает информацию о ШРЭЛ БРИ
     */
    public static final String EXTRA_SHREL_BRI_MEAS_INFO_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_BRI_MEAS_INFO_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_PU_SHREL_DATA_EVENT: сериализованный объект PUDataShrel.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект PUDataShrel
     * - RealtimeMonitorFragment: десериализует и обрабатывает данные от ШРЭЛ ПУ
     */
    public static final String EXTRA_SHREL_PU_MEAS_DATA_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_PU_MEAS_DATA_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_PU_SHREL_INFO_EVENT: сериализованный объект PUInfoShrel.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект PUInfoShrel
     * - RealtimeMonitorFragment: десериализует и обрабатывает информацию о ШРЭЛ ПУ
     */
    public static final String EXTRA_SHREL_PU_MEAS_INFO_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_PU_MEAS_INFO_OBJ_BYTES";


    // --- Для ответов на команды Read (GET) ---
    // General Commands
    /**
     * Extra для ACTION_SHREL_GENERAL_GET_VERSION_RESPONSE: строка версии устройства.
     * Тип: String
     *
     * Пример использования:
     * - MessageProcessingService: устанавливает строку версии
     * - ParametersFragment: отображает версию в UI
     */
    public static final String EXTRA_SHREL_VERSION_STRING = "com.alextim.bluetest.EXTRA_SHREL_VERSION_STRING";

    /**
     * Extra для ACTION_SHREL_GENERAL_GET_CHARACTERISTICS_RESPONSE: сериализованный объект характеристик устройства.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект характеристик
     * - ParametersFragment: десериализует и отображает характеристики
     */
    public static final String EXTRA_SHREL_DEVICE_CHARACTERISTICS_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_DEVICE_CHARACTERISTICS_OBJ_BYTES";


    // Settings
    /**
     * Extra для ACTION_SHREL_GET_MEAS_SETTINGS_RESPONSE: сериализованный объект настроек измерений.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект настроек
     * - ParametersFragment: десериализует и отображает настройки
     */
    public static final String EXTRA_SHREL_MEAS_SETTINGS_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_MEAS_SETTINGS_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_USER_SETTINGS_BD_SHARM_RESPONSE: сериализованный объект пользовательских настроек ШАРМ БД.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект настроек ШАРМ БД
     * - ParametersFragment: десериализует и отображает настройки ШАРМ БД
     */
    public static final String EXTRA_SHREL_USER_SETTINGS_BD_SHARM_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_USER_SETTINGS_BD_SHARM_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_USER_SETTINGS_BRI_SHREL_RESPONSE: сериализованный объект пользовательских настроек ШРЭЛ БРИ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект настроек ШРЭЛ БРИ
     * - ParametersFragment: десериализует и отображает настройки ШРЭЛ БРИ
     */
    public static final String EXTRA_SHREL_USER_SETTINGS_BRI_SHREL_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_USER_SETTINGS_BRI_SHREL_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_USER_SETTINGS_PU_SHREL_RESPONSE: сериализованный объект пользовательских настроек ШРЭЛ ПУ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект настроек ШРЭЛ ПУ
     * - ParametersFragment: десериализует и отображает настройки ШРЭЛ ПУ
     */
    public static final String EXTRA_SHREL_USER_SETTINGS_PU_SHREL_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_USER_SETTINGS_PU_SHREL_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_UHF_SETTINGS_RESPONSE: сериализованный объект настроек УКВ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект настроек УКВ
     * - ParametersFragment: десериализует и отображает настройки УКВ
     */
    public static final String EXTRA_SHREL_UHF_SETTINGS_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_UHF_SETTINGS_OBJ_BYTES";


    // Params
    /**
     * Extra для ACTION_SHREL_GET_MEAS_PARAMS_BD_SHARM_RESPONSE: сериализованный объект параметров измерений ШАРМ БД.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект параметров ШАРМ БД
     * - ParametersFragment: десериализует и отображает параметры ШАРМ БД
     */
    public static final String EXTRA_SHREL_MEAS_PARAMS_BD_SHARM_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_MEAS_PARAMS_BD_SHARM_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_MEAS_PARAMS_BRI_SHREL_RESPONSE: сериализованный объект параметров измерений ШРЭЛ БРИ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект параметров ШРЭЛ БРИ
     * - ParametersFragment: десериализует и отображает параметры ШРЭЛ БРИ
     */
    public static final String EXTRA_SHREL_MEAS_PARAMS_BRI_SHREL_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_MEAS_PARAMS_BRI_SHREL_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_MEAS_PARAMS_PU_SHREL_RESPONSE: сериализованный объект параметров измерений ШРЭЛ ПУ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект параметров ШРЭЛ ПУ
     * - ParametersFragment: десериализует и отображает параметры ШРЭЛ ПУ
     */
    public static final String EXTRA_SHREL_MEAS_PARAMS_PU_SHREL_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_MEAS_PARAMS_PU_SHREL_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_USER_PARAMS_BD_SHARM_RESPONSE: сериализованный объект пользовательских параметров ШАРМ БД.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект параметров ШАРМ БД
     * - ParametersFragment: десериализует и отображает параметры ШАРМ БД
     */
    public static final String EXTRA_SHREL_USER_PARAMS_BD_SHARM_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_USER_PARAMS_BD_SHARM_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_USER_PARAMS_BRI_SHREL_RESPONSE: сериализованный объект пользовательских параметров ШРЭЛ БРИ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект параметров ШРЭЛ БРИ
     * - ParametersFragment: десериализует и отображает параметры ШРЭЛ БРИ
     */
    public static final String EXTRA_SHREL_USER_PARAMS_BRI_SHREL_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_USER_PARAMS_BRI_SHREL_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GET_USER_PARAMS_PU_SHREL_RESPONSE: сериализованный объект пользовательских параметров ШРЭЛ ПУ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект параметров ШРЭЛ ПУ
     * - ParametersFragment: десериализует и отображает параметры ШРЭЛ ПУ
     */
    public static final String EXTRA_SHREL_USER_PARAMS_PU_SHREL_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_USER_PARAMS_PU_SHREL_OBJ_BYTES";


    // --- Для ответов на команды Write (SET) ---
    /**
     * Extra для ответов на SET-команды: код ошибки при выполнении команды.
     * Тип: int
     *
     * Пример использования:
     * - MessageProcessingService: устанавливает код ошибки
     * - RealtimeMonitorFragment: проверяет успешность выполнения команды
     */
    public static final String EXTRA_SHREL_SET_COMMAND_ERROR_CODE = "com.alextim.bluetest.EXTRA_SHREL_SET_COMMAND_ERROR_CODE";

    /**
     * Extra для всех типов ответов: код характеристики, от которой пришло сообщение.
     * Тип: int
     *
     * Пример использования:
     * - MessageProcessingService: устанавливает код характеристики
     * - Все обработчики: определяют источник сообщения
     */
    public static final String EXTRA_SHREL_CHAR_CODE = "com.alextim.bluetest.EXTRA_SHREL_CHAR_CODE";

    /**
     * Extra для ACTION_SHREL_GENERAL_GET_SHARM_BD_HARD_SETTINGS_RESPONSE: сериализованный объект аппаратных настроек ШАРМ БД.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект аппаратных настроек ШАРМ БД
     * - ParametersFragment: десериализует и отображает аппаратные настройки ШАРМ БД
     */
    public static final String EXTRA_SHREL_SHARM_BD_HARD_SETTINGS_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_SHARM_BD_HARD_SETTINGS_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GENERAL_GET_SHREL_BRI_HARD_SETTINGS_RESPONSE: сериализованный объект аппаратных настроек ШРЭЛ БРИ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект аппаратных настроек ШРЭЛ БРИ
     * - ParametersFragment: десериализует и отображает аппаратные настройки ШРЭЛ БРИ
     */
    public static final String EXTRA_SHREL_SHREL_BRI_HARD_SETTINGS_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_SHREL_BRI_HARD_SETTINGS_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_GENERAL_GET_SHREL_PU_HARD_SETTINGS_RESPONSE: сериализованный объект аппаратных настроек ШРЭЛ ПУ.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект аппаратных настроек ШРЭЛ ПУ
     * - ParametersFragment: десериализует и отображает аппаратные настройки ШРЭЛ ПУ
     */
    public static final String EXTRA_SHREL_SHREL_PU_HARD_SETTINGS_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_SHREL_PU_HARD_SETTINGS_OBJ_BYTES";

    // --- Для ошибок ---
    /**
     * Extra для ACTION_SHREL_ERROR_MESSAGE и ACTION_SHREL_UNKNOWN_MESSAGE:
     * необработанное сообщение в шестнадцатеричном формате.
     * Тип: String
     *
     * Пример использования:
     * - MessageProcessingService: устанавливает hex-представление сообщения
     * - RealtimeMonitorFragment: логирует необработанное сообщение
     */
    public static final String EXTRA_SHREL_RAW_MESSAGE_HEX = "com.alextim.bluetest.EXTRA_SHREL_RAW_MESSAGE_HEX";

    /**
     * Extra для ACTION_SHREL_ERROR_MESSAGE: текст сообщения об ошибке.
     * Тип: String
     *
     * Пример использования:
     * - MessageProcessingService: устанавливает текст ошибки
     * - RealtimeMonitorFragment: логирует ошибку
     */
    public static final String EXTRA_SHREL_EXCEPTION_MESSAGE = "com.alextim.bluetest.EXTRA_SHREL_EXCEPTION_MESSAGE";

    /**
     * Extra для ACTION_SHREL_ERROR_MESSAGE: сериализованный объект сообщения об ошибке.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект ошибки
     * - RealtimeMonitorFragment: десериализует и обрабатывает ошибку
     */
    public static final String EXTRA_SHREL_ERROR_MESSAGE_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_ERROR_MESSAGE_OBJ_BYTES";

    /**
     * Extra для ACTION_SHREL_UNKNOWN_MESSAGE: сериализованный объект неизвестного сообщения.
     * Тип: byte[]
     *
     * Пример использования:
     * - MessageProcessingService: сериализует объект неизвестного сообщения
     * - RealtimeMonitorFragment: десериализует и логирует сообщение
     */
    public static final String EXTRA_SHREL_UNKNOWN_MESSAGE_OBJ_BYTES = "com.alextim.bluetest.EXTRA_SHREL_UNKNOWN_MESSAGE_OBJ_BYTES";


    // --- Общие (для всех типов сообщений) ---
    /**
     * Extra для всех типов сообщений: код характеристики источника сообщения.
     * Тип: int
     *
     * Пример использования:
     * - MessageProcessingService: устанавливает код характеристики
     * - Все обработчики: определяют источник сообщения
     */
    public static final String EXTRA_SHREL_SOURCE_CHAR_CODE = "com.alextim.bluetest.EXTRA_SHREL_SOURCE_CHAR_CODE";

    public static final String EXTRA_SHREL_PROCESSING_MESSAGE_ERROR_COUNT = "com.alextim.bluetest.EXTRA_SHREL_HANDLE_MESSAGE_ERROR_COUNT";
}