package com.alextim.lora.service.protocol;


import static com.alextim.lora.client.ble.BleMessage.bytesToHex;
import static com.alextim.lora.service.protocol.DetectorCodes.CONTROL_SUM_BASE_DATA;
import static com.alextim.lora.service.protocol.DetectorCodes.CONTROL_SUM_BASE_HEADER;
import static com.alextim.lora.service.protocol.DetectorCodes.DATA_INDEX_CUSTOM;
import static com.alextim.lora.service.protocol.DetectorCodes.DATA_KS_INDEX_CUSTOM;
import static com.alextim.lora.service.protocol.DetectorCodes.HEADER_KS_INDEX;
import static com.alextim.lora.service.protocol.DetectorCodes.HEADER_SIZE_WITHOUT_DATA;
import static com.alextim.lora.service.protocol.DetectorCodes.LEN_INDEX;
import static com.alextim.lora.service.protocol.DetectorCodes.START_PACKAGE_BYTE;

import android.util.Log;

import java.util.function.BiConsumer;

public class BleProtocolParser {

    private static final String TAG = "BleProtocolParser";
    private final byte[] rcvBuf = new byte[256];
    private int rcvInd = 0;

    public void addData(byte[] data) {
        for (byte b : data) {
            if (rcvInd >= rcvBuf.length) {
                Log.e(TAG, "Buffer overflow!");
                rcvInd = 0;
                break;
            }
            rcvBuf[rcvInd++] = b;
        }
    }

    public void handle(BiConsumer<byte[], String> consumer, String deviceAddress) {
        int start = 0;
        while (start < rcvInd) {
            while (start < rcvInd) {
                if (rcvBuf[start] == START_PACKAGE_BYTE) {
                    break;
                } else {
                    start++;
                }
            }

            if (start != 0) {
                System.arraycopy(rcvBuf, start, rcvBuf, 0, rcvInd - start);
                rcvInd = rcvInd - start;
                start = 0;
            }

            if (rcvInd < HEADER_SIZE_WITHOUT_DATA) {
                return;
            }

            byte len = rcvBuf[LEN_INDEX];

            if (rcvInd < HEADER_SIZE_WITHOUT_DATA + (len & 0xFF)) {
                return;
            }

            try {
                controlHeaderCheck(rcvBuf, 0, HEADER_KS_INDEX, HEADER_KS_INDEX); // KSH
                controlDataCheck(rcvBuf, DATA_INDEX_CUSTOM, DATA_INDEX_CUSTOM + (len & 0xFF), DATA_KS_INDEX_CUSTOM); // KSD
            } catch (Exception e) {
                Log.w(TAG, "Checksum failed at index " + start + ", skipping byte.", e);
                start++;
                continue;
            }

            byte[] message = new byte[HEADER_SIZE_WITHOUT_DATA + (len & 0xFF)];
            System.arraycopy(rcvBuf, 0, message, 0, message.length);

            consumer.accept(message, deviceAddress);

            start = HEADER_SIZE_WITHOUT_DATA + (len & 0xFF);
        }

        rcvInd -= start;
    }

    private static void controlHeaderCheck(byte[] arr, int from, int toExclude, int ksIndex) {
        int sum = CONTROL_SUM_BASE_HEADER;
        for (int i = from; i < toExclude; i++) {
            sum += Byte.toUnsignedInt(arr[i]);
        }
        sum &= 0xFF;

        if (sum != Byte.toUnsignedInt(arr[ksIndex])) {
            String err = String.format("Invalid header checksum: %x != calculated %x, arr: %s, from: %d toExclude: %d",
                    Byte.toUnsignedInt(arr[ksIndex]), sum, bytesToHex(arr), from, toExclude);
            Log.w(TAG, err);
            throw new RuntimeException(err);
        }
    }

    private static void controlDataCheck(byte[] arr, int from, int toExclude, int ksIndex) {
        if (from == toExclude)
            return;

        int sum = CONTROL_SUM_BASE_DATA;
        if (from < toExclude) {
            for (int i = from; i < toExclude; i++) {
                sum += Byte.toUnsignedInt(arr[i]);
            }
        }
        sum &= 0xFF;

        if (sum != Byte.toUnsignedInt(arr[ksIndex])) {
            String err = String.format("Invalid data checksum: %x != calculated %x, arr: %s, from: %d toExclude: %d",
                    Byte.toUnsignedInt(arr[ksIndex]), sum, bytesToHex(arr), from, toExclude);
            Log.w(TAG, err);
            throw new RuntimeException(err);
        }
    }

    public static void setControlSums(byte[] arr) {
        int dataKs = CONTROL_SUM_BASE_DATA;
        for (int i = DATA_INDEX_CUSTOM; i < DATA_INDEX_CUSTOM + (arr[LEN_INDEX] & 0xFF); i++) {
            dataKs += Byte.toUnsignedInt(arr[i]);
        }
        dataKs &= 0xFF;
        arr[DATA_KS_INDEX_CUSTOM] = (byte) dataKs;

        int headerKs = CONTROL_SUM_BASE_HEADER;
        for (int i = 0; i < HEADER_KS_INDEX; i++) {
            headerKs += Byte.toUnsignedInt(arr[i]);
        }
        headerKs &= 0xFF;
        arr[HEADER_KS_INDEX] = (byte) headerKs;
    }

}