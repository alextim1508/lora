package com.alextim.lora.service.lora;

import java.util.ArrayList;
import java.util.List;

public class LoraConfig {

    private final List<String> titles = new ArrayList<>();

    private final List<ModuleConfig> moduleConfigs = new ArrayList<>();

    public LoraConfig() {
        // E32 configs
        addConfig(new E32Config.E32_433_T20_Config());
        addConfig(new E32Config.E32_900_T20_Config());
        addConfig(new E32Config.E32_170_T30_Config());
        addConfig(new E32Config.E32_433_T30_Config());
        addConfig(new E32Config.E32_900_T30_Config());
        addConfig(new E32Config.E32_433_T33_Config());
        addConfig(new E32Config.E32_433_T37_Config());
        addConfig(new E32Config.E32_DEFAULT_Config());

        // E22 configs
        addConfig(new E22Config.E22_170_T30_Config());
        addConfig(new E22Config.E22_230_T30_Config());
        addConfig(new E22Config.E22_400_T30_Config());
        addConfig(new E22Config.E22_900_T30_Config());
        addConfig(new E22Config.E22_170_T33_Config());
        addConfig(new E22Config.E22_230_T33_Config());
        addConfig(new E22Config.E22_400_T33_Config());
        addConfig(new E22Config.E22_900_T33_Config());
        addConfig(new E22Config.E22_230_T37_Config());
        addConfig(new E22Config.E22_400_T37_Config());
        addConfig(new E22Config.E22_DEFAULT_Config());

        // E34 configs
        addConfig(new E34Config.E34_2G4_27D_Config());
        addConfig(new E34Config.E34_2G4_20D_Config());
        addConfig(new E34Config.E34_2G4_27H_Config());
        addConfig(new E34Config.E34_2G4_20H_Config());
        addConfig(new E34Config.E34_DEFALT_Config());

        // XL1278 config
        addConfig(new XL1278Config());
    }

    private void addConfig(ModuleConfig config) {
        titles.add(config.getTitle());
        moduleConfigs.add(config);
    }

    public ModuleConfig findByTitle(String title) {
        for (int i= 0; i < titles.size(); i++) {
            if(title.startsWith(titles.get(i))) {
                return moduleConfigs.get(i);
            }
        }

        String defaultConfigTitle = title.split("-")[0] + "-DEFAULT";
        for (int i= 0; i < titles.size(); i++) {
            if(defaultConfigTitle.equals(titles.get(i))) {
                return moduleConfigs.get(i);
            }
        }

        return null;
    }
}
