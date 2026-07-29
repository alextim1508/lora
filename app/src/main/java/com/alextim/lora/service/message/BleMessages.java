package com.alextim.lora.service.message;


import static com.alextim.lora.service.message.Commands.CMD_GET_DEVICE_CONFIGURATION;
import static com.alextim.lora.service.message.Commands.CMD_SET_CONFIGURATION;
import static com.alextim.lora.service.message.Commands.CMD_SET_DEVICE_CONFIGURATION;

import com.alextim.lora.client.ble.BleMessage;
import com.alextim.lora.service.protocol.ConfigByteUtils;
import com.alextim.lora.service.protocol.PacketTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BleMessages {

    public static class GetVersionCommand extends BleMessage {

        public String version;

        public GetVersionCommand() {
            super(PacketTypes.COMMANDS, Commands.CMD_GET_VERSION, (byte) 0, new byte[0], 0);
        }

        @Override
        public String toString() {
            return "CMD_GET_VERSION";
        }
    }

    public static class RestartCommand extends BleMessage {
        public RestartCommand() {
            super(PacketTypes.COMMANDS, Commands.CMD_RESTART, (byte) 0, new byte[0], 0);
        }

        @Override
        public String toString() {
            return "CMD_RESTART";
        }
    }

    public static class GetConfigurationCommand extends BleMessage {
        public GetConfigurationCommand() {
            super(PacketTypes.COMMANDS, Commands.CMD_GET_CONFIGURATION, (byte) 0, new byte[0], 0);
        }

        @Override
        public String toString() {
            return "CMD_GET_CONFIGURATION";
        }
    }

    public static class SetConfigurationCommand extends BleMessage {
        public static final int LORA_NAME_MAX_LENGTH_BYTES = 16;

        public final byte version;
        public final byte loraPowerIndex;
        public final byte loraRateIndex;
        public final byte loraChannelIndex;
        public final String loraName;

        public SetConfigurationCommand(String loraName, byte version, byte loraPowerIndex, byte loraRateIndex, byte loraChannelIndex) {
            super(PacketTypes.COMMANDS, CMD_SET_CONFIGURATION, calcLen(),
                    createDataBytes(loraName.getBytes(StandardCharsets.US_ASCII), version, loraPowerIndex, loraRateIndex, loraChannelIndex), 0);
            this.version = version;
            this.loraPowerIndex = loraPowerIndex;
            this.loraRateIndex = loraRateIndex;
            this.loraChannelIndex = loraChannelIndex;
            this.loraName = loraName;
        }

        private static byte calcLen() {
            return (byte) (1 /* version */ + 3 /* power, rate, channel */ + LORA_NAME_MAX_LENGTH_BYTES);
        }

        private static byte[] createDataBytes(byte[] loraNameBytes, byte version, byte loraPowerIndex, byte loraRateIndex, byte loraChannelIndex) {
            byte[] data = new byte[calcLen()];

            int idx = 0;
            data[idx++] = version;
            data[idx++] = loraPowerIndex;
            data[idx++] = loraRateIndex;
            data[idx++] = loraChannelIndex;

            System.arraycopy(loraNameBytes, 0, data, idx, Math.min(loraNameBytes.length, LORA_NAME_MAX_LENGTH_BYTES));
            return data;
        }

        @Override
        public String toString() {
            return "CMD_SET_CONFIGURATION{" +
                    "loraName='" + loraName + '\'' +
                    ", version=" + version +
                    ", loraPowerIndex=" + loraPowerIndex +
                    ", loraRateIndex=" + loraRateIndex +
                    ", loraChannelIndex=" + loraChannelIndex +
                    '}';
        }
    }

    public static class GetLoraRssiCommand extends BleMessage {
        public GetLoraRssiCommand() {
            super(PacketTypes.COMMANDS, Commands.CMD_GET_LORA_RSSI, (byte) 0, new byte[0], 0);

        }

        @Override
        public String toString() {
            return "CMD_GET_LORA_RSSI";
        }
    }

    public static class SendDataCommand extends BleMessage {
        public final byte[] payload;

        public SendDataCommand(byte[] payload) {
            super(PacketTypes.COMMANDS, Commands.CMD_SEND_DATA, (byte) (payload != null ? payload.length : 0), payload, 0);
            this.payload = payload != null ? payload : new byte[0];
        }

        @Override
        public String toString() {
            return "CMD_SEND_DATA";
        }
    }

    public static class SetDeviceConfigurationCommand extends BleMessage {

        public final byte configByte;

        public SetDeviceConfigurationCommand(byte configByte) {
            super(PacketTypes.COMMANDS, CMD_SET_DEVICE_CONFIGURATION, calcLen(), createDataBytes(configByte), 0);
            this.configByte = configByte;
        }

        public SetDeviceConfigurationCommand(boolean deviceType, boolean transmitMode, boolean echoEnabled, int packetLength) {
            this(ConfigByteUtils.buildConfigByte(deviceType, transmitMode, echoEnabled, packetLength));
        }

        private static byte calcLen() {
            return 1;
        }

        private static byte[] createDataBytes(byte configByte) {
            return new byte[]{configByte};
        }

        @Override
        public String toString() {
            return "CMD_SET_DEVICE_CONFIGURATION";
        }
    }

    public static class GetDeviceConfigurationCommand extends BleMessage {
        public GetDeviceConfigurationCommand() {
            super(PacketTypes.COMMANDS, CMD_GET_DEVICE_CONFIGURATION, (byte) 0, new byte[0], 0);
        }

        @Override
        public String toString() {
            return "CMD_GET_DEVICE_CONFIGURATION";
        }
    }

    // === EVENTS ===

    public static class StatusEvent extends BleMessage {

        public byte version;
        public int voltage;
        public int temperature;

        public StatusEvent(byte[] data, long timestamp) {
            super(PacketTypes.EVENTS, Events.EVENT_STATUS, (byte) (data != null ? data.length : 0), data, timestamp);

            version = data[0];

            ByteBuffer bb = ByteBuffer.wrap(new byte[]{
                    data[2],
                    data[3],
            });
            bb.order(ByteOrder.LITTLE_ENDIAN);
            voltage = Short.toUnsignedInt(bb.getShort());

            bb = ByteBuffer.wrap(new byte[]{
                    data[4],
                    data[5],
            });
            bb.order(ByteOrder.LITTLE_ENDIAN);
            temperature = Short.toUnsignedInt(bb.getShort());
        }

        @Override
        public String toString() {
            return "ENT_STATUS";
        }
    }

    public static class ReceiveDataEvent extends BleMessage {

        public final byte[] payload;

        public ReceiveDataEvent(byte[] data, long timestamp) {
            super(PacketTypes.EVENTS, Events.EVENT_RECEIVE_DATA, (byte) (data != null ? data.length : 0), data, timestamp);
            this.payload = data != null ? data : new byte[0];
        }

        @Override
        public String toString() {
            return "ENT_RECEIVE_DATA";
        }
    }

    public static class GenerateDataEvent extends BleMessage {

        public GenerateDataEvent(byte[] data, long timestamp) {
            super(PacketTypes.EVENTS, Events.EVENT_GENERATE_DATA, (byte) (data != null ? data.length : 0), data, timestamp);
        }

        @Override
        public String toString() {
            return "ENT_GENERATE_DATA";
        }
    }

    // === RESPONSES (ответы на команды, тип = код команды) ===

    public static class GetVersionResponse extends BleMessage {

        public final String version;

        public GetVersionResponse(byte[] data, byte errorCode, long timestamp) {
            super(Commands.CMD_GET_VERSION, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
            this.version = data != null ? new String(data) : "unknown";
        }

        @Override
        public String toString() {
            return "CMD_GET_VERSION_RESPONSE";
        }
    }

    public static class GetConfigurationResponse extends BleMessage {

        public static final int LORA_NAME_START_INDEX = 4;
        public static final int LORA_NAME_LENGTH = 16;

        public final byte version;
        public final byte loraPowerIndex;
        public final byte loraRateIndex;
        public final byte loraChannelIndex;
        public final String loraName;

        public GetConfigurationResponse(byte[] data, byte errorCode, long timestamp) {
            super(Commands.CMD_GET_CONFIGURATION, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
            version = data[0];
            loraPowerIndex = data[1];
            loraRateIndex = data[2];
            loraChannelIndex = data[3];

            byte[] nameBytes = new byte[LORA_NAME_LENGTH];
            System.arraycopy(data, LORA_NAME_START_INDEX, nameBytes, 0, LORA_NAME_LENGTH);
            String rawName = new String(nameBytes, StandardCharsets.US_ASCII);
            loraName = rawName.replace("\0", "").trim();
        }

        @Override
        public String toString() {
            return "GetConfigurationResponse{" +
                    "version=" + version +
                    ", loraPowerIndex=" + loraPowerIndex +
                    ", loraRateIndex=" + loraRateIndex +
                    ", loraChannelIndex=" + loraChannelIndex +
                    ", loraName='" + loraName + '\'' +
                    '}';
        }
    }

    public static class SetConfigurationResponse extends BleMessage {

        public SetConfigurationResponse(byte[] data, byte errorCode, long timestamp) {
            super(CMD_SET_CONFIGURATION, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
        }

        @Override
        public String toString() {
            return "CMD_SET_CONFIGURATION_RESPONSE";
        }
    }

    public static class GetLoraRssiResponse extends BleMessage {

        public final int noiseRssi;
        public final int receiveDataRssi;

        public GetLoraRssiResponse(byte[] data, byte errorCode, long timestamp) {
            super(Commands.CMD_GET_LORA_RSSI, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);

            if (data != null && data.length >= 8) {
                ByteBuffer bb = ByteBuffer.wrap(new byte[]{data[0], data[1], data[2], data[3]});
                bb.order(ByteOrder.LITTLE_ENDIAN);
                this.noiseRssi = bb.getInt();

                bb = ByteBuffer.wrap(new byte[]{data[4], data[5], data[6], data[7]});
                bb.order(ByteOrder.LITTLE_ENDIAN);
                this.receiveDataRssi = bb.getInt();
            } else {
                this.noiseRssi = Integer.MIN_VALUE;
                this.receiveDataRssi = Integer.MIN_VALUE;
            }
        }

        @Override
        public String toString() {
            return String.format("CMD_GET_LORA_RSSI_RESPONSE - Noise: %d, Receive: %d", noiseRssi, receiveDataRssi);
        }
    }

    public static class SendDataResponse extends BleMessage {

        public SendDataResponse(byte[] data, byte errorCode, long timestamp) {
            super(Commands.CMD_SEND_DATA, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
        }

        @Override
        public String toString() {
            return "CMD_SEND_DATA_RESPONSE";
        }
    }

    public static class SetDeviceConfigurationResponse extends BleMessage {

        public SetDeviceConfigurationResponse(byte[] data, byte errorCode, long timestamp) {
            super(CMD_SET_DEVICE_CONFIGURATION, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
        }

        @Override
        public String toString() {
            return "CMD_SET_DEVICE_CONFIGURATION_RESPONSE";
        }
    }

    public static class GetDeviceConfigurationResponse extends BleMessage {

        public final byte configByte;

        public GetDeviceConfigurationResponse(byte[] data, byte errorCode, long timestamp) {
            super(CMD_GET_DEVICE_CONFIGURATION, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
            if (data != null && data.length >= 1) {
                this.configByte = data[0];
            } else {
                this.configByte = 0;
            }
        }

        @Override
        public String toString() {
            return "CMD_GET_DEVICE_CONFIGURATION_RESPONSE";
        }
    }

    // === UNKNOWN ===
    public static class UnknownMessage extends BleMessage {
        public UnknownMessage(byte type, byte param, byte[] data, long timestamp) {
            super(type, param, (byte) (data != null ? data.length : 0), data, timestamp);
        }
    }

    public static class ErrorMessage extends BleMessage {

        private final RuntimeException exception;

        public ErrorMessage(byte type, byte param, byte[] data, long timestamp, RuntimeException exception) {
            super(type, param, (byte) (data != null ? data.length : 0), data, timestamp);
            this.exception = exception;
        }
    }
}
