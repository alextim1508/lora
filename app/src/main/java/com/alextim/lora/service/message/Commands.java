package com.alextim.lora.service.message;

public class Commands {
    public static final byte CMD_GET_VERSION = (byte) 0x01;
    public static final byte CMD_RESTART = (byte) 0x02;

    public static final byte CMD_GET_CONFIGURATION = (byte) 0x20;
    public static final byte CMD_SET_CONFIGURATION = (byte) 0x30;
    public static final byte CMD_SEND_DATA = (byte) 0x40;
    public static final byte CMD_GET_LORA_RSSI = (byte) 0x50;
}
