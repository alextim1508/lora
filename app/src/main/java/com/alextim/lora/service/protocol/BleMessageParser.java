package com.alextim.lora.service.protocol;


import com.alextim.lora.client.ble.BleMessage;
import com.alextim.lora.service.message.BleMessages;
import com.alextim.lora.service.message.Commands;
import com.alextim.lora.service.message.Events;

public class BleMessageParser {

    public static BleMessage parse(BleMessage bleMessage) {
        if (bleMessage == null) {
            return null;
        }

        byte type = bleMessage.type;
        byte param = bleMessage.param;
        byte[] data = bleMessage.data;
        long timestamp = bleMessage.timestamp;

        if (type == PacketTypes.EVENTS) {
            if (param == Events.EVENT_STATUS) {
                return new BleMessages.StatusEvent(data, timestamp);
            } else if (param == Events.EVENT_RECEIVE_DATA) {
                return new BleMessages.ReceiveDataEvent(data, timestamp);
            }
        } else if (type == PacketTypes.COMMANDS) {
            if (param == Commands.CMD_GET_VERSION) {
                return new BleMessages.GetVersionCommand();
            } else if (param == Commands.CMD_RESTART) {
                return new BleMessages.RestartCommand();
            } else if (param == Commands.CMD_GET_CONFIGURATION) {
                return new BleMessages.GetConfigurationCommand();
            } else if (param == Commands.CMD_SET_CONFIGURATION) {
                return new BleMessages.SetConfigurationCommand(
                        bleMessage.data[1],
                        bleMessage.data[2],
                        bleMessage.data[3],
                        bleMessage.data[4]);
            } else if (param == Commands.CMD_GET_LORA_RSSI) {
                return new BleMessages.GetLoraRssiCommand();
            } else if (param == Commands.CMD_SEND_DATA) {
                return new BleMessages.SendDataCommand(data);
            }
        } else {
            if (type == Commands.CMD_GET_VERSION) {
                return new BleMessages.GetVersionResponse(data, bleMessage.param, timestamp);
            } else if (type == Commands.CMD_GET_CONFIGURATION) {
                return new BleMessages.GetConfigurationResponse(data, bleMessage.param, timestamp);
            } else if (type == Commands.CMD_SET_CONFIGURATION) {
                return new BleMessages.SetConfigurationResponse(data, bleMessage.param, timestamp);
            } else if (type == Commands.CMD_GET_LORA_RSSI) {
                return new BleMessages.GetLoraRssiResponse(data, bleMessage.param, timestamp);
            } else if (type == Commands.CMD_SEND_DATA) {
                return new BleMessages.SendDataResponse(data, bleMessage.param, timestamp);
            }
        }

        return new BleMessages.UnknownMessage(type, param, data, timestamp);
    }
}