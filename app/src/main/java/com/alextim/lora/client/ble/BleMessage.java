package com.alextim.lora.client.ble;

import static com.alextim.lora.service.protocol.BleProtocolParser.setControlSums;
import static com.alextim.lora.service.protocol.DetectorCodes.*;
import static com.alextim.lora.service.protocol.DetectorCodes.LEN_INDEX;
import static com.alextim.lora.service.protocol.DetectorCodes.PARAM_INDEX;
import static com.alextim.lora.service.protocol.DetectorCodes.START_INDEX;
import static com.alextim.lora.service.protocol.DetectorCodes.START_PACKAGE_BYTE;
import static com.alextim.lora.service.protocol.DetectorCodes.TYPE_INDEX;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
N  Len Name                Value
1  1   Байт синхронизации  0x79
2  4   Метка времени       0xEA DE 00 00 ← little-endian: 0x0000DEEA = 57066 мс
3  1   Тип                 0x00 ← 0 для событий (EVENTS), 0xFF для команд (COMMANDS), остальные - ответ на команду (Тип=коду команды)
4  1   Параметр            0x01 ← Зависит от Типа: для событий - код события, для команд - код команды, для ответов - код ошибки (0 - ощибки нет, команда выполнена)
5  1   Длина данных        0x04 ← 4 байта
6  1   KS данных           0xA9 ← сумма данных + 0x57
7  1   KS заголовка        0xCD ← сумма заголовка + 0x57
8  4   Данные              0x4F 03 00 00 ← 4 байта данных
*/
public class BleMessage {
    public final byte type;
    public final byte param;
    public final byte len;
    public final byte[] data;
    public final long timestamp;
    public final byte[] rawMessage;

    public BleMessage(byte[] rawMessage) {

        this.rawMessage = rawMessage;

        if (rawMessage == null || rawMessage.length < HEADER_SIZE_WITHOUT_DATA) {
            throw new IllegalArgumentException("Invalid message length: " + (rawMessage != null ? rawMessage.length : "null"));
        }

        byte startByte = rawMessage[START_INDEX];
        if (startByte != START_PACKAGE_BYTE) {
            throw new IllegalArgumentException("Invalid start byte: " + startByte);
        }

        this.timestamp = ByteBuffer.wrap(rawMessage, TIMESTAMP_INDEX, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;

        this.type = rawMessage[TYPE_INDEX];
        this.param = rawMessage[PARAM_INDEX];
        this.len = rawMessage[LEN_INDEX];

        if (rawMessage.length != HEADER_SIZE_WITHOUT_DATA + (this.len & 0xFF)) {
            throw new IllegalArgumentException("Message length mismatch");
        }

        this.data = new byte[this.len & 0xFF];
        System.arraycopy(rawMessage, DATA_INDEX_CUSTOM, this.data, 0, this.data.length);
    }

    public BleMessage(byte type, byte param, byte len, byte[] data, long timestamp) {
        this.type = type;
        this.param = param;
        this.len = len;
        this.data = data != null ? data : new byte[0];
        this.timestamp = timestamp;

        this.rawMessage = toByteArray();
    }

    public byte[] toByteArray() {
        byte[] message = new byte[HEADER_SIZE_WITHOUT_DATA + this.data.length];

        message[START_INDEX] = START_PACKAGE_BYTE;

        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) this.timestamp);
        System.arraycopy(bb.array(), 0, message, TIMESTAMP_INDEX, 4);

        message[TYPE_INDEX] = this.type;

        message[PARAM_INDEX] = this.param;

        message[LEN_INDEX] = this.len;

        System.arraycopy(this.data, 0, message, DATA_INDEX_CUSTOM, this.data.length);

        setControlSums(message);

        return message;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "BleMessage{" +
                "type=" + type +
                ", param=" + param +
                ", len=" + len +
                ", data=" + bytesToHex(data) +
                ", timestamp=" + timestamp +
                "}";
    }
}
