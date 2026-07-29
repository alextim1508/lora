package com.alextim.lora.service.lora;

import java.util.Map;

public class E32Config {

    public static class E32_433_T20_Config extends ModuleConfig {
        public E32_433_T20_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-433T20";
        }
    }

    public static class E32_900_T20_Config extends ModuleConfig {
        public E32_900_T20_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantC(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-900T20";
        }
    }

    //----------------------------------------------------------------------------------------------

    public static class E32_170_T30_Config extends ModuleConfig {
        public E32_170_T30_Config() {
            fullRateMapVariantB(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantA(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-170T30";
        }
    }

    public static class E32_433_T30_Config extends ModuleConfig {
        public E32_433_T30_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-433T30";
        }
    }

    public static class E32_900_T30_Config extends ModuleConfig {
        public E32_900_T30_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantC(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-900T30";
        }
    }

    //----------------------------------------------------------------------------------------------

    public static class E32_433_T33_Config extends ModuleConfig {
        public E32_433_T33_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantC(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-433T33";
        }
    }

    //----------------------------------------------------------------------------------------------

    public static class E32_433_T37_Config extends ModuleConfig {
        public E32_433_T37_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantD(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E32-433T37";
        }
    }

    //----------------------------------------------------------------------------------------------
    public static class E32_DEFAULT_Config extends ModuleConfig {
        public E32_DEFAULT_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantB(channelMap);
        }
        @Override
        public String getTitle() {
            return "E32-DEFAULT";
        }
    }

    //----------------------------------------------------------------------------------------------

    private static void fullRateMapVariantA(Map<Integer, String> rateMap) {
        rateMap.put(0, "2.4k");
        rateMap.put(1, "2.4k");
        rateMap.put(2, "2.4k");
        rateMap.put(3, "4.8k");
        rateMap.put(4, "9.6k");
        rateMap.put(5, "19.2k");
        rateMap.put(6, "19.2k");
        rateMap.put(7, "19.2k");
    }

    private static void fullRateMapVariantB(Map<Integer, String> rateMap) {
        rateMap.put(0, "0.3k");
        rateMap.put(1, "1.2k");
        rateMap.put(2, "2.4k");
        rateMap.put(3, "4.8k");
        rateMap.put(4, "9.6k");
        rateMap.put(5, "19.2k");
        rateMap.put(6, "19.2k");
        rateMap.put(7, "19.2k");
    }

    private static void fullPowerMapVariantA(Map<Integer, String> powerMap) {
        powerMap.put(0, "20 dBm");
        powerMap.put(1, "17 dBm");
        powerMap.put(2, "14 dBm");
        powerMap.put(3, "10 dBm");
    }

    private static void fullPowerMapVariantB(Map<Integer, String> powerMap) {
        powerMap.put(0, "30 dBm");
        powerMap.put(1, "27 dBm");
        powerMap.put(2, "24 dBm");
        powerMap.put(3, "21 dBm");
    }

    private static void fullPowerMapVariantC(Map<Integer, String> powerMap) {
        powerMap.put(0, "33 dBm");
        powerMap.put(1, "30 dBm");
        powerMap.put(2, "27 dBm");
        powerMap.put(3, "24 dBm");
    }

    private static void fullPowerMapVariantD(Map<Integer, String> powerMap) {
        powerMap.put(0, "37 dBm");
        powerMap.put(1, "37 dBm");
        powerMap.put(2, "37 dBm");
        powerMap.put(3, "37 dBm");
    }

    private static void fullChannelMapVariantA(Map<Integer, String> channelMap) {
        for (int i = 0; i < 55; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    private static void fullChannelMapVariantB(Map<Integer, String> channelMap) {
        for (int i = 0; i < 32; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    private static void fullChannelMapVariantC(Map<Integer, String> channelMap) {
        for (int i = 0; i < 70; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }
}
