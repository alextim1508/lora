package com.alextim.lora.service.lora;

import java.util.Map;

public class E22Config {

    public static class E22_170_T30_Config extends ModuleConfig {
        public E22_170_T30_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantA(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-170T30";
        }
    }

    public static class E22_230_T30_Config extends ModuleConfig {
        public E22_230_T30_Config() {
            fullRateMapVariantB(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-230T30";
        }
    }

    public static class E22_400_T30_Config extends ModuleConfig {
        public E22_400_T30_Config() {
            fullRateMapVariantC(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantC(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-400T30";
        }
    }

    public static class E22_900_T30_Config extends ModuleConfig {
        public E22_900_T30_Config() {
            fullRateMapVariantC(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantD(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-900T30";
        }
    }

    //----------------------------------------------------------------------------------------------
    public static class E22_170_T33_Config extends ModuleConfig {
        public E22_170_T33_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantA(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-170T33";
        }
    }

    public static class E22_230_T33_Config extends ModuleConfig {
        public E22_230_T33_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-230T33";
        }
    }

    public static class E22_400_T33_Config extends ModuleConfig {
        public E22_400_T33_Config() {
            fullRateMapVariantC(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantC(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-400T33";
        }
    }

    public static class E22_900_T33_Config extends ModuleConfig {
        public E22_900_T33_Config() {
            fullRateMapVariantC(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantD(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-900T33";
        }
    }

    //----------------------------------------------------------------------------------------------

    public static class E22_230_T37_Config extends ModuleConfig {
        public E22_230_T37_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantC(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-230T37";
        }
    }

    public static class E22_400_T37_Config extends ModuleConfig {
        public E22_400_T37_Config() {
            fullRateMapVariantC(rateMap);
            fullPowerMapVariantC(powerMap);
            fullChannelMapVariantC(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-400T37";
        }
    }

    //----------------------------------------------------------------------------------------------

    public static class E22_DEFAULT_Config extends ModuleConfig {
        public E22_DEFAULT_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        @Override
        public String getTitle() {
            return "E22-DEFAULT";
        }
    }

    //----------------------------------------------------------------------------------------------

    private static void fullRateMapVariantA(Map<Integer, String> rateMap) {
        rateMap.put(0, "2.4k");
        rateMap.put(1, "2.4k");
        rateMap.put(2, "2.4k");
        rateMap.put(3, "2.4k");
        rateMap.put(4, "4.8k");
        rateMap.put(5, "9.6k");
        rateMap.put(6, "15.6k");
        rateMap.put(7, "15.6k");
    }

    private static void fullRateMapVariantB(Map<Integer, String> rateMap) {
        rateMap.put(0, "0.3k");
        rateMap.put(1, "1.2k");
        rateMap.put(2, "2.4k");
        rateMap.put(3, "4.8k");
        rateMap.put(4, "9.6k");
        rateMap.put(5, "19.2k");
        rateMap.put(6, "38.4k");
        rateMap.put(7, "62.5k");
    }

    private static void fullRateMapVariantC(Map<Integer, String> rateMap) {
        rateMap.put(0, "2.4k");
        rateMap.put(1, "2.4k");
        rateMap.put(2, "2.4k");
        rateMap.put(3, "4.8k");
        rateMap.put(4, "9.6k");
        rateMap.put(5, "19.2k");
        rateMap.put(6, "38.4k");
        rateMap.put(7, "62.5k");
    }

    private static void fullPowerMapVariantA(Map<Integer, String> powerMap) {
        powerMap.put(0, "30 dBm");
        powerMap.put(1, "27 dBm");
        powerMap.put(2, "24 dBm");
        powerMap.put(3, "21 dBm");
    }

    private static void fullPowerMapVariantB(Map<Integer, String> powerMap) {
        powerMap.put(0, "33 dBm");
        powerMap.put(1, "30 dBm");
        powerMap.put(2, "27 dBm");
        powerMap.put(3, "24 dBm");
    }

    private static void fullPowerMapVariantC(Map<Integer, String> powerMap) {
        powerMap.put(0, "37 dBm");
        powerMap.put(1, "37 dBm");
        powerMap.put(2, "37 dBm");
        powerMap.put(3, "37 dBm");
    }

    private static void fullChannelMapVariantA(Map<Integer, String> channelMap) {
        for (int i = 0; i < 93; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    private static void fullChannelMapVariantB(Map<Integer, String> channelMap) {
        for (int i = 0; i < 65; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    private static void fullChannelMapVariantC(Map<Integer, String> channelMap) {
        for (int i = 0; i < 84; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    private static void fullChannelMapVariantD(Map<Integer, String> channelMap) {
        for (int i = 0; i < 81; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }
}
