package com.alextim.lora.service;

import static com.alextim.lora.service.protocol.BleMessageParser.parse;
import static com.alextim.lora.service.constants.LoraActions.*;
import static com.alextim.lora.service.constants.LoraExtras.*;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alextim.lora.client.ble.BleMessage;
import com.alextim.lora.service.message.BleMessages;
import com.alextim.lora.service.message.BleMessages.GetConfigurationResponse;
import com.alextim.lora.service.message.BleMessages.GetLoraRssiResponse;
import com.alextim.lora.service.message.BleMessages.GetVersionResponse;
import com.alextim.lora.service.message.BleMessages.ReceiveDataEvent;
import com.alextim.lora.service.message.BleMessages.SendDataResponse;
import com.alextim.lora.service.message.BleMessages.SetConfigurationResponse;
import com.alextim.lora.service.message.BleMessages.StatusEvent;

public class MessageProcessingService {

    private static final String TAG = "MessageProcessingService";

    public BleMessage parseMessage(BleMessage msg) {
        BleMessage parsedMessage;
        try {
            parsedMessage = parse(msg);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to parse raw message: " + e.getMessage(), e);
            return new BleMessages.ErrorMessage(msg.type, msg.param, msg.data, msg.timestamp, e);
        }
        return parsedMessage;
    }

    public void sendParsedMessageToUI(Context context, BleMessage parsed, String deviceAddress) {
        Intent intent = null;

        if (parsed instanceof GetConfigurationResponse) {
            intent = createGetConfigResponseIntent((GetConfigurationResponse) parsed);
        } else if (parsed instanceof SetConfigurationResponse) {
            intent = createSetConfigResponseIntent((SetConfigurationResponse) parsed);
        } else if (parsed instanceof SendDataResponse) {
            intent = createSendDataResponseIntent((SendDataResponse) parsed);
        } else if (parsed instanceof ReceiveDataEvent) {
            intent = createReceiveDataEventIntent((ReceiveDataEvent) parsed);
        } else if (parsed instanceof StatusEvent) {
            intent = createStatusEventIntent((StatusEvent) parsed);
        } else if (parsed instanceof GetVersionResponse) {
            intent = createGetVersionResponseIntent((GetVersionResponse) parsed);
        } else if (parsed instanceof GetLoraRssiResponse) {
            intent = createGetLoraRssiResponseIntent((GetLoraRssiResponse) parsed);
        } else if (parsed instanceof BleMessages.ErrorMessage) {
            Log.e(TAG, "Error parsing message from device " + deviceAddress + ": " + parsed);
            return;
        }

        if (intent != null) {
            intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private Intent createGetVersionResponseIntent(GetVersionResponse answer) {
        Intent intent = new Intent(ACTION_GET_VERSION_RESPONSE);
        intent.putExtra(EXTRA_VERSION, answer.version);
        intent.putExtra(EXTRA_ERROR_CODE, answer.param);
        return intent;
    }

    private Intent createGetConfigResponseIntent(GetConfigurationResponse answer) {
        Intent intent = new Intent(ACTION_GET_CONFIG_RESPONSE);
        intent.putExtra(EXTRA_LORA_TYPE, answer.loraType);
        intent.putExtra(EXTRA_POWER_INDEX, answer.loraPowerIndex);
        intent.putExtra(EXTRA_RATE_INDEX, answer.loraRateIndex);
        intent.putExtra(EXTRA_CHANNEL_INDEX, answer.loraChannelIndex);
        intent.putExtra(EXTRA_ERROR_CODE, answer.param);
        return intent;
    }

    private Intent createSetConfigResponseIntent(SetConfigurationResponse answer) {
        Intent intent = new Intent(ACTION_SET_CONFIG_RESPONSE);
        intent.putExtra(EXTRA_ERROR_CODE, answer.param);
        return intent;
    }

    private Intent createGetLoraRssiResponseIntent(GetLoraRssiResponse answer) {
        Intent intent = new Intent(ACTION_GET_LORA_RSSI_RESPONSE);
        intent.putExtra(EXTRA_NOISE_RSSI, answer.noiseRssi);
        intent.putExtra(EXTRA_RECEIVE_DATA_RSSI, answer.receiveDataRssi);
        intent.putExtra(EXTRA_ERROR_CODE, answer.param);
        return intent;
    }

    private Intent createSendDataResponseIntent(SendDataResponse answer) {
        Intent intent = new Intent(ACTION_SEND_DATA_RESPONSE);
        intent.putExtra(EXTRA_ERROR_CODE, answer.param);
        return intent;
    }

    private Intent createReceiveDataEventIntent(ReceiveDataEvent event) {
        Intent intent = new Intent(ACTION_RECEIVE_DATA_EVENT);
        intent.putExtra(EXTRA_PAYLOAD, event.payload);
        return intent;
    }

    private Intent createStatusEventIntent(StatusEvent event) {
        Intent intent = new Intent(ACTION_STATUS_EVENT);
        intent.putExtra(EXTRA_VOLTAGE, event.voltage);
        intent.putExtra(EXTRA_TEMPERATURE, event.temperature);
        return intent;
    }
}