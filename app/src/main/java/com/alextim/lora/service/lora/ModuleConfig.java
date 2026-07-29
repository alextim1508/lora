package com.alextim.lora.service.lora;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public abstract class ModuleConfig {
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

    public int[] getAvailableRates() {
        return rateMap.keySet().stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
    }

    public int[] getAvailablePowers() {
        return powerMap.keySet().stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
    }

    public int[] getAvailableChannels() {
        return channelMap.keySet().stream()
                .mapToInt(Integer::intValue)
                .sorted()
                .toArray();
    }

    public abstract String getTitle();

    @NonNull
    @Override
    public String toString() {
        return getTitle();
    }
}