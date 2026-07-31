package com.alextim.lora.service.lora;

public class XL1278Config extends ModuleConfig {
    public XL1278Config() {
        rateMap.put(0, "4.8 кБод");

        powerMap.put(0, "11 дБм");
        powerMap.put(1, "14 дБм");
        powerMap.put(2, "17 дБм");
        powerMap.put(3, "20 дБм");

        for (int i = 0; i < 116; i++) {
            channelMap.put(i, "Канал " + i);
        }
    }

    @Override
    public String getTitle() {
        return "XL1278";
    }
}
