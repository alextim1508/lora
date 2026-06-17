package com.alextim.lora.service.message;


import java.util.HashMap;
import java.util.Map;

public class LoRaConfigurator {

    public enum LoRaModule {
        LORA_UNKNOWN(0),
        LORA_XL1278(1),
        LORA_E32(2),
        LORA_E22(3),
        LORA_E34(4);

        private final int code;

        LoRaModule(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static LoRaModule fromCode(int code) {
            for (LoRaModule module : values()) {
                if (module.code == code) {
                    return module;
                }
            }
            return LORA_UNKNOWN;
        }
    }

    public abstract static class ModuleConfig {
        public final Map<Integer, String> rateMap = new HashMap<>();
        public final Map<Integer, String> powerMap = new HashMap<>();
        public final Map<Integer, String> channelMap = new HashMap<>();

        public String getRateDescription(int rateCode) {
            return rateMap.getOrDefault(rateCode, "Unknown rate");
        }

        public String getPowerDescription(int powerCode) {
            return powerMap.getOrDefault(powerCode, "Unknown power");
        }

        public String getChannelDescription(int channelCode) {
            return channelMap.getOrDefault(channelCode, "Unknown channel");
        }

        public abstract int[] getAvailableRates();

        public abstract int[] getAvailablePowers();

        public abstract int[] getAvailableChannels();
    }

    public static class XL1278Config extends ModuleConfig {
        public XL1278Config() {
            rateMap.put(0, "4.8 кБод");
/*            rateMap.put(0, "0.3 кБод");
            rateMap.put(1, "0.6 кБод");
            rateMap.put(2, "1.2 кБод");
            rateMap.put(3, "2.4 кБод");
            rateMap.put(4, "4.8 кБод");
            rateMap.put(5, "9.6 кБод");
            rateMap.put(6, "19.2 кБод");
            rateMap.put(7, "28.4 кБод");*/

            powerMap.put(0, "11 dBm");
            powerMap.put(1, "14 dBm");
            powerMap.put(2, "17 dBm");
            powerMap.put(3, "20 dBm");

            for (int i = 0; i <= 115; i++) {
                channelMap.put(i, "Channel " + i);
            }
        }

        @Override
        public int[] getAvailableRates() {
            return new int[]{0};
        }

        @Override
        public int[] getAvailablePowers() {
            return new int[]{0, 1, 2, 3};
        }

        @Override
        public int[] getAvailableChannels() {
            int[] channels = new int[116];
            for (int i = 0; i < channels.length; i++) {
                channels[i] = i;
            }
            return channels;
        }
    }

    public static class E34Config extends ModuleConfig {
        public E34Config() {
            rateMap.put(0, "250k");
            rateMap.put(1, "1M");
            rateMap.put(2, "2M");
            rateMap.put(3, "2M");

            powerMap.put(0, "27 dBm");
            powerMap.put(1, "21 dBm");
            powerMap.put(2, "15 dBm");
            powerMap.put(3, "9 dBm");

            for (int i = 0; i < 12; i++) {
                channelMap.put(i, "Channel " + i);
            }
        }

        @Override
        public int[] getAvailableRates() {
            return new int[]{0, 1, 2, 3};
        }

        @Override
        public int[] getAvailablePowers() {
            return new int[]{0, 1, 2, 3};
        }

        @Override
        public int[] getAvailableChannels() {
            int[] channels = new int[12];
            for (int i = 0; i < channels.length; i++) {
                channels[i] = i;
            }
            return channels;
        }
    }

    public static class E22Config extends ModuleConfig {
        public E22Config() {
            rateMap.put(0, "2.4k");
            rateMap.put(1, "2.4k");
            rateMap.put(2, "2.4k");
            rateMap.put(3, "2.4k");
            rateMap.put(4, "4.8k");
            rateMap.put(5, "9.6k");
            rateMap.put(6, "15.6k");
            rateMap.put(7, "15.6k");

            powerMap.put(0, "30 dBm");
            powerMap.put(1, "27 dBm");
            powerMap.put(2, "24 dBm");
            powerMap.put(3, "21 dBm");

            for (int i = 0; i < 65; i++) {
                channelMap.put(i, "Channel " + i);
            }
        }

        @Override
        public int[] getAvailableRates() {
            return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
        }

        @Override
        public int[] getAvailablePowers() {
            return new int[]{0, 1, 2, 3};
        }

        @Override
        public int[] getAvailableChannels() {
            int[] channels = new int[65];
            for (int i = 0; i < channels.length; i++) {
                channels[i] = i;
            }
            return channels;
        }
    }

    public static class E32Config extends ModuleConfig {
        public E32Config() {
            rateMap.put(0, "2.4k");
            rateMap.put(1, "2.4k");
            rateMap.put(2, "2.4k");
            rateMap.put(3, "4.8k");
            rateMap.put(4, "9.6k");
            rateMap.put(5, "19.2k");
            rateMap.put(6, "19.2k");
            rateMap.put(7, "19.2k");

            powerMap.put(0, "30 dBm");
            powerMap.put(1, "27 dBm");
            powerMap.put(2, "24 dBm");
            powerMap.put(3, "21 dBm");

            for (int i = 0; i < 32; i++) {
                channelMap.put(i, "Channel " + i);
            }
        }

        @Override
        public int[] getAvailableRates() {
            return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
        }

        @Override
        public int[] getAvailablePowers() {
            return new int[]{0, 1, 2, 3};
        }

        @Override
        public int[] getAvailableChannels() {
            int[] channels = new int[32];
            for (int i = 0; i < channels.length; i++) {
                channels[i] = i;
            }
            return channels;
        }
    }
}