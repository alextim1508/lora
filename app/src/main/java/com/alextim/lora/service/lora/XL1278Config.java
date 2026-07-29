package com.alextim.lora.service.lora;

public class XL1278Config extends ModuleConfig {
    public XL1278Config() {
        rateMap.put(0, "4.8 кБод");

        powerMap.put(0, "11 dBm");
        powerMap.put(1, "14 dBm");
        powerMap.put(2, "17 dBm");
        powerMap.put(3, "20 dBm");

        for (int i = 0; i < 116; i++) {
            channelMap.put(i, "Channel " + i);
        }
    }

    @Override
    public String getTitle() {
        return "XL1278";
    }
}
