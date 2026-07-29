package com.alextim.lora.service.lora;

import java.util.Map;

public class E34Config {

    public static class E34_2G4_27D_Config extends ModuleConfig {
        public E34_2G4_27D_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantA(channelMap);
        }

        public String getTitle() {
            return "E34-2G427D";
        }
    }

    public static class E34_2G4_20D_Config extends ModuleConfig {
        public E34_2G4_20D_Config() {
            fullRateMapVariantA(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantA(channelMap);
        }

        public String getTitle() {
            return "E34-2G420D";
        }
    }


    public static class E34_2G4_27H_Config extends ModuleConfig {
        public E34_2G4_27H_Config() {
            fullRateMapVariantB(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        public String getTitle() {
            return "E34-2G427H";
        }
    }

    public static class E34_2G4_20H_Config extends ModuleConfig {
        public E34_2G4_20H_Config() {
            fullRateMapVariantB(rateMap);
            fullPowerMapVariantB(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        public String getTitle() {
            return "E34-2G420H";
        }
    }

    public static class E34_DEFALT_Config extends ModuleConfig {
        public E34_DEFALT_Config() {
            fullRateMapVariantB(rateMap);
            fullPowerMapVariantA(powerMap);
            fullChannelMapVariantB(channelMap);
        }

        public String getTitle() {
            return "E34-DEFAULT";
        }
    }

    private static void fullRateMapVariantA(Map<Integer, String> rateMap) {
        rateMap.put(0, "Auto");
    }

    private static void fullRateMapVariantB(Map<Integer, String> rateMap) {
        rateMap.put(0, "250k");
        rateMap.put(1, "1M");
        rateMap.put(2, "2M");
        rateMap.put(3, "2M");
    }

    private static void fullPowerMapVariantA(Map<Integer, String> powerMap) {
        powerMap.put(0, "27 dBm");
        powerMap.put(1, "21 dBm");
        powerMap.put(2, "15 dBm");
        powerMap.put(3, "9 dBm");
    }

    private static void fullPowerMapVariantB(Map<Integer, String> powerMap) {
        powerMap.put(0, "20 dBm");
        powerMap.put(1, "17 dBm");
        powerMap.put(2, "14 dBm");
        powerMap.put(3, "10 dBm");
    }


    private static void fullChannelMapVariantA(Map<Integer, String> channelMap) {
        for (int i = 0; i < 16; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    private static void fullChannelMapVariantB(Map<Integer, String> channelMap) {
        for (int i = 0; i < 12; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }
}
