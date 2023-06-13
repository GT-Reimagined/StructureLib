package com.gtnewhorizon.structurelib;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class StructureLibConfig {

    public static final Common COMMON = new Common();

    public static final CommonConfig COMMON_CONFIG;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT = new Client();

    public static final ClientConfig CLIENT_CONFIG;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {

        final Pair<CommonConfig, ForgeConfigSpec> COMMON_PAIR = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_CONFIG = COMMON_PAIR.getLeft();
        COMMON_SPEC = COMMON_PAIR.getRight();
        final Pair<ClientConfig, ForgeConfigSpec> CLIENT_PAIR = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_CONFIG = CLIENT_PAIR.getLeft();
        CLIENT_SPEC = CLIENT_PAIR.getRight();

    }

    public static void onModConfigEvent(final ModConfig e) {
        if (e.getModId().equals(StructureLibAPI.MOD_ID)){
            if (e.getSpec() == COMMON_SPEC) bakeCommonConfig();
            else if (e.getSpec() == CLIENT_SPEC) bakeClientConfig();
        }
    }


    public static class Client{
        public int HINT_LIFESPAN, HINT_TRANSPARENCY, MAX_COEXISTING;
        public boolean REMOVE_COLLIDING;
    }

    public static class Common {
        public int AUTO_PLACE_BUDGET, AUTO_PLACE_INTERVAL;
    }


    public static class CommonConfig {

        public final ForgeConfigSpec.IntValue AUTO_PLACE_BUDGET, AUTO_PLACE_INTERVAL;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("hologram");
            AUTO_PLACE_BUDGET = builder.comment("Max number of elements can be placed in one round of auto place.",
                "As expected, server side settings will overrides client settings.",
                "Certain larger multi might increase these values beyond this configured value.")
                .translation("structurelib.config.auto_place_budget")
                .defineInRange("AUTO_PLACE_BUDGET", 25, 1, 200);
            AUTO_PLACE_INTERVAL = builder.comment("Unit: millisecond. Minimal interval between two auto place round.",
                "As expected, server side settings will overrides client settings.",
                "Note this relates to the wall clock, not in game ticks.",
                "Value smaller than default is likely to be perceived as no minimal interval whatsoever.")
                .translation("structurelib.config.auto_place_interval")
                .defineInRange("AUTO_PLACE_INTERVAL", 300, 0, 20000);
            builder.pop();
        }

    }

    public static class ClientConfig {

        public final ForgeConfigSpec.IntValue HINT_LIFESPAN, HINT_TRANSPARENCY, MAX_COEXISTING;
        public final ForgeConfigSpec.BooleanValue REMOVE_COLLIDING;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("hologram");
            HINT_LIFESPAN = builder.comment("Ticks before a hologram disappears.")
                .defineInRange("HINT_LIFESPAN", 400, 1, 20000);
            HINT_TRANSPARENCY = builder.comment("Alpha value of hologram particles. Higher the value, the more \"ghostly\" the hologram will appear to be.")
                .translation("structurelib.config.hint_transparency")
                .defineInRange("HINT_TRANSPARENCY", 192, 1, 255);
            MAX_COEXISTING = builder.comment("An attempt will be made to prune old holograms when a new hologram is about to be projected")
                .translation("structurelib.config.max_coexisting")
                .defineInRange("MAX_COEXISTING", 1, 1, 100);
            REMOVE_COLLIDING = builder.comment("An attempt will be made to remove an existing hologram if it collides with a new hologram")
                .translation("structurelib.config.remove_colliding")
                .define("REMOVE_COLLIDING", true);
            builder.pop();
        }

    }

    private static void bakeCommonConfig() {
        COMMON.AUTO_PLACE_BUDGET = COMMON_CONFIG.AUTO_PLACE_BUDGET.get();
        COMMON.AUTO_PLACE_INTERVAL = COMMON_CONFIG.AUTO_PLACE_INTERVAL.get();
    }

    private static void bakeClientConfig() {
        CLIENT.HINT_LIFESPAN = CLIENT_CONFIG.HINT_LIFESPAN.get();
        CLIENT.HINT_TRANSPARENCY = CLIENT_CONFIG.HINT_TRANSPARENCY.get();
        CLIENT.MAX_COEXISTING = CLIENT_CONFIG.MAX_COEXISTING.get();
        CLIENT.REMOVE_COLLIDING = CLIENT_CONFIG.REMOVE_COLLIDING.get();
    }
}
