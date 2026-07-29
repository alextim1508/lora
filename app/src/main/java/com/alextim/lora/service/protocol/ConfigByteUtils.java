package com.alextim.lora.service.protocol;

public class ConfigByteUtils {

    /**
     * Результат парсинга байта конфигурации.
     */
    public static class ParsedConfig {
        public final boolean deviceType;      // 0 - Slave (приемник), 1 - Master (передатчик)
        public final boolean transmitMode;   // 0 - Software (программный), 1 - Hardware (аппаратный)
        public final boolean echoEnabled;    // 0 - Off, 1 - On
        public final int packetLength;       // 0-31

        public ParsedConfig(boolean deviceType, boolean transmitMode, boolean echoEnabled, int packetLength) {
            this.deviceType = deviceType;
            this.transmitMode = transmitMode;
            this.echoEnabled = echoEnabled;
            this.packetLength = packetLength;
        }

        @Override
        public String toString() {
            return "ParsedConfig{" +
                    "deviceType=" + (deviceType ? "Master" : "Slave") +
                    ", transmitMode=" + (transmitMode ? "Hardware" : "Software") +
                    ", echoEnabled=" + echoEnabled +
                    ", packetLength=" + packetLength +
                    '}';
        }
    }

    /**
     * Формирует байт конфигурации для передачи в команде.
     *
     * @param deviceType Тип устройства: 0 - Slave (приемник), 1 - Master (передатчик).
     * @param transmitMode Режим передачи: 0 - Software (программный), 1 - Hardware (аппаратный).
     * @param echoEnabled Флаг включения режима эха.
     * @param packetLength Длина посылки (0-31).
     * @return Сформированный байт конфигурации.
     * @throws IllegalArgumentException если длина посылки вне допустимого диапазона.
     */
    public static byte buildConfigByte(boolean deviceType, boolean transmitMode, boolean echoEnabled, int packetLength) {
        if (packetLength < 0 || packetLength > 31) {
            throw new IllegalArgumentException("Packet length must be between 0 and 31. Got: " + packetLength);
        }

        byte configByte = 0;

        // Бит 0: deviceType
        if (deviceType) {
            configByte |= (1 << 0);
        }

        // Бит 1: transmitMode
        if (transmitMode) {
            configByte |= (1 << 1);
        }

        // Бит 2: echoMode
        if (echoEnabled) {
            configByte |= (1 << 2);
        }

        // Биты 3..7: packageLength (5 битов)
        byte lengthBits = (byte) (packetLength & 0x1F); // 0x1F = 0b11111, маска для 5 битов
        configByte |= (lengthBits << 3); // Сдвинуть на 3 позиции влево

        return configByte;
    }

    /**
     * Парсит байт конфигурации, извлеченный из команды или события.
     *
     * @param configByte Байт конфигурации для парсинга.
     * @return Объект ParsedConfig с извлеченными значениями.
     */
    public static ParsedConfig parseConfigByte(byte configByte) {
        // Бит 0: deviceType
        boolean deviceType = ((configByte >> 0) & 0x01) == 1;

        // Бит 1: transmitMode
        boolean transmitMode = ((configByte >> 1) & 0x01) == 1;

        // Бит 2: echoMode
        boolean echoEnabled = ((configByte >> 2) & 0x01) == 1;

        // Биты 3..7: packageLength (5 битов)
        int packetLength = (configByte >> 3) & 0x1F; // 0x1F = 0b11111, маска для 5 битов

        return new ParsedConfig(deviceType, transmitMode, echoEnabled, packetLength);
    }
}
