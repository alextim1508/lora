package com.alextim.lora.service.message;


import static com.alextim.lora.service.message.Commands.CMD_SET_CONFIGURATION;

import com.alextim.lora.client.ble.BleMessage;
import com.alextim.lora.service.protocol.PacketTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

        public final byte loraType;
        public final byte loraPowerIndex;
        public final byte loraRateIndex;
        public final byte loraChannelIndex;

        public SetConfigurationCommand(byte loraType, byte loraPowerIndex, byte loraRateIndex, byte loraChannelIndex) {
            super(PacketTypes.COMMANDS, CMD_SET_CONFIGURATION, calcLen(), createDataBytes(loraType, loraPowerIndex, loraRateIndex, loraChannelIndex), 0);
            this.loraType = loraType;
            this.loraPowerIndex = loraPowerIndex;
            this.loraRateIndex = loraRateIndex;
            this.loraChannelIndex = loraChannelIndex;
        }

        private static byte calcLen() {
            return 5;
        }

        private static byte[] createDataBytes(byte loraType, byte loraPowerIndex, byte loraRateIndex, byte loraChannelIndex) {
            return new byte[]{
                    1, loraType, loraPowerIndex, loraRateIndex, loraChannelIndex};
        }

        @Override
        public String toString() {
            return "CMD_SET_CONFIGURATION";
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

        public final byte version;
        public final byte loraType;
        public final byte loraPowerIndex;
        public final byte loraRateIndex;
        public final byte loraChannelIndex;

        public GetConfigurationResponse(byte[] data, byte errorCode, long timestamp) {
            super(Commands.CMD_GET_CONFIGURATION, errorCode, (byte) (data != null ? data.length : 0), data, timestamp);
            version = data[0];
            loraType = data[1];
            loraPowerIndex = data[2];
            loraRateIndex = data[3];
            loraChannelIndex = data[4];
        }

        @Override
        public String toString() {
            return "CMD_GET_CONFIGURATION_RESPONSE";
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
