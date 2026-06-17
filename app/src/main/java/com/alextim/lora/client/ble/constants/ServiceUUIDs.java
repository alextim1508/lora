package com.alextim.lora.client.ble.constants;

import java.util.UUID;

/**
 * Класс, содержащий константы для UUID сервисов, характеристик и дескрипторов
 */
public class ServiceUUIDs {

    /*UUID сервиса UART*/
    public static final UUID UART_SERVICE_UUID = UUID.fromString("0000fe40-cc7a-482a-984a-7f2ed5b3e58f");

    /* UUID характеристики для отправки данных в устройство (виртуальный "TX" на стороне устройства)*/
    public static final UUID UART_WRITE_CHAT_UUID = UUID.fromString("0000fe41-8e22-4541-9d4c-21edae82ed19");

    /*UUID характеристики для получения данных от устройства (виртуальный "RX" на стороне устройства, с поддержкой notify)*/
    public static final UUID UART_NOTIFY_CHART_UUID = UUID.fromString("0000fe42-8e22-4541-9d4c-21edae82ed19");

   /* UUID дескриптора Client Characteristic Configuration Descriptor (CCCD) - используется для включения/отключения уведомлений*/
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34fb");
}
