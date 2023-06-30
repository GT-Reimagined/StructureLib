package com.gtnewhorizon.structurelib;


import com.gtnewhorizon.structurelib.util.PlatformUtils;
import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import com.gtnewhorizon.structurelib.net.AlignmentMessage;
import com.gtnewhorizon.structurelib.net.ErrorHintParticleMessage;
import com.gtnewhorizon.structurelib.net.SetChannelDataMessage;
import com.gtnewhorizon.structurelib.net.UpdateHintParticleMessage;
import com.gtnewhorizon.structurelib.util.XSTR;
import trinsdar.networkapi.api.PacketRegistration;


/**
 * This class does not contain a stable API. Refrain from using this class.
 */
public class StructureLib {

    private static final String STRUCTURECOMPAT_MODID = "structurecompat";
    public static boolean DEBUG_MODE;
    public static boolean PANIC_MODE = Boolean.getBoolean("structurelib.panic");
    public static final Logger LOGGER = LogManager.getLogger("StructureLib");

    public static final NetworkChannel CHANNEL = new NetworkChannel(StructureLibAPI.MOD_ID, 0,"main");

    public static final ResourceLocation ALIGNMENT_QUERY = new ResourceLocation(StructureLibAPI.MOD_ID, "alignment_query");
    public static final ResourceLocation ALIGNMENT_DATA = new ResourceLocation(StructureLibAPI.MOD_ID, "alignment_data");
    public static final ResourceLocation UPDATE_HINT_PARTICLE = new ResourceLocation(StructureLibAPI.MOD_ID, "update_hint_particle");
    public static final ResourceLocation ERROR_HINT_PARTICLE = new ResourceLocation(StructureLibAPI.MOD_ID, "error_hint_particle");
    public static final ResourceLocation SET_CHANNEL_DATA = new ResourceLocation(StructureLibAPI.MOD_ID, "set_channel_data");

    static CommonProxy proxy;

    public static void init() {
        PacketRegistration.registerPacket(AlignmentMessage.AlignmentQuery.class, ALIGNMENT_QUERY, AlignmentMessage.AlignmentQuery::decode, PacketRegistration.NetworkDirection.PLAY_TO_SERVER);
        PacketRegistration.registerPacket(AlignmentMessage.AlignmentData.class, ALIGNMENT_DATA, AlignmentMessage.AlignmentData::decode, PacketRegistration.NetworkDirection.PLAY_TO_CLIENT);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, UPDATE_HINT_PARTICLE, UpdateHintParticleMessage.HANDLER, UpdateHintParticleMessage.class);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, ERROR_HINT_PARTICLE, ErrorHintParticleMessage.HANDLER, ErrorHintParticleMessage.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, SET_CHANNEL_DATA, SetChannelDataMessage.HANDLER, SetChannelDataMessage.class);

        try {
            DEBUG_MODE = Boolean.parseBoolean(System.getProperty("structurelib.debug"));
        } catch (IllegalArgumentException | NullPointerException e) {
            // turn on debug by default in dev mode
            // this will be overridden if above property is present and set to false
            //DEBUG_MODE = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        }
    }

    public static final XSTR RANDOM = new XSTR();
    static StructureLib INSTANCE;

    static Object COMPAT;
    protected static CreativeModeTab creativeTab;

    public void preInit() {
        proxy = PlatformUtils.isServer() ? new CommonProxy() : new ClientProxy();
        proxy.preInit();
        init();
        /*if (Loader.isModLoaded(STRUCTURECOMPAT_MODID)) {
            COMPAT = Loader.instance().getIndexedModList().get(STRUCTURECOMPAT_MODID).getMod();
        }*/
    }

    public static CreativeModeTab getCreativeTab() {
        return creativeTab;
    }

    public static void addClientSideChatMessages(String... messages) {
        proxy.addClientSideChatMessages(messages);
    }

    public static Player getCurrentPlayer() {
        return proxy.getCurrentPlayer();
    }

    public static boolean isCurrentPlayer(Player player) {
        return proxy.isCurrentPlayer(player);
    }

    public static long getOverworldTime() {
        return proxy.getOverworldTime();
    }

    public static StructureLib instance() {
        return INSTANCE;
    }

    public CommonProxy proxy() {
        return proxy;
    }
}
