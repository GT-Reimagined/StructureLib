package com.gtnewhorizon.structurelib;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.utils.AutomationType;
import com.gtnewhorizon.structurelib.util.PlatformUtils;

public class StructureLibConfig {

    public static ConfigEntry.IntValue AUTO_PLACE_BUDGET;
    public static ConfigEntry.IntValue AUTO_PLACE_INTERVAL;

    public static ConfigEntry.IntValue HINT_LIFESPAN;
    public static ConfigEntry.IntValue HINT_TRANSPARENCY;
    public static ConfigEntry.IntValue MAX_COEXISTING;
    public static ConfigEntry.BoolValue REMOVE_COLLIDING;

    public static ConfigHandler CONFIG_COMMON;
    public static ConfigHandler CONFIG_CLIENT;

    public static void init(){
        Config config = new Config("structurelib");
        ConfigSection section = config.add("general");
        AUTO_PLACE_BUDGET = section.addInt("auto_place_budget", 25,
            "Max number of elements can be placed in one round of auto place.",
            "As expected, server side settings will overrides client settings.",
            "Certain larger multi might increase these values beyond this configured value.").setRange(1, 200);
        AUTO_PLACE_INTERVAL = section.addInt("auto_place_interval", 300,
                "Unit: millisecond. Minimal interval between two auto place round.",
                "As expected, server side settings will overrides client settings.",
                "Note this relates to the wall clock, not in game ticks.",
                "Value smaller than default is likely to be perceived as no minimal interval whatsoever.").setRange(0, 20000);
        CONFIG_COMMON = PlatformUtils.INSTANCE.createConfig(StructureLibAPI.MOD_ID, config);
        CONFIG_COMMON.register();
        if (PlatformUtils.INSTANCE.isClient()){
            Config client = new Config("structurelib-client");
            ConfigSection clientSection = client.add("general");
            HINT_LIFESPAN = clientSection.addInt("hint_lifespan", 400,
                "Ticks before a hologram disappears.").setRange(1, 20000);
            HINT_TRANSPARENCY = clientSection.addInt("hint_transparency", 192,
                "Alpha value of hologram particles. Higher the value, the more \"ghostly\" the hologram will appear to be.").setRange(1, 255);
            MAX_COEXISTING = clientSection.addInt("max_coexisting", 1,
                "An attempt will be made to prune old holograms when a new hologram is about to be projected").setRange(1, 100);
            REMOVE_COLLIDING = clientSection.addBool("remove_colliding", true,
                "An attempt will be made to remove an existing hologram if it collides with a new hologram");
            CONFIG_CLIENT = PlatformUtils.INSTANCE.createConfig(StructureLibAPI.MOD_ID, client, ConfigSettings.withConfigType(ConfigType.CLIENT).withAutomations(AutomationType.AUTO_LOAD));
            CONFIG_CLIENT.register();
        }
    }

}
