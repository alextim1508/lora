package com.alextim.lora.service.protocol;

public class DetectorCodes {
    public static final byte START_PACKAGE_BYTE = (byte) 0x79;

    public static final int START_INDEX = 0;
    public static final int TIMESTAMP_INDEX = 1;
    public static final int TYPE_INDEX = 5;
    public static final int PARAM_INDEX = 6;
    public static final int LEN_INDEX = 7;
    public static final int DATA_KS_INDEX_CUSTOM = 8;
    public static final int HEADER_KS_INDEX = 9;
    public static final int DATA_INDEX_CUSTOM = 10;

    public static final int HEADER_SIZE_WITHOUT_DATA = 10;

    public static final int CONTROL_SUM_BASE_HEADER = 0x57;
    public static final int CONTROL_SUM_BASE_DATA = 0x57;
}