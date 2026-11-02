package com.gtnewhorizon.structurelib;


import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
import com.gtnewhorizon.structurelib.net.StructureLibNetworking;
import com.teamresourceful.resourcefullib.common.network.Network;
import com.teamresourceful.resourcefullib.common.network.base.Networking;
import com.teamresourceful.resourcefullib.common.network.neoforge.NetworkImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.structurelib.net.AlignmentMessage;
import com.gtnewhorizon.structurelib.net.ErrorHintParticleMessage;
import com.gtnewhorizon.structurelib.net.SetChannelDataMessage;
import com.gtnewhorizon.structurelib.net.UpdateHintParticleMessage;
import com.gtnewhorizon.structurelib.util.XSTR;


/**
 * This class does not contain a stable API. Refrain from using this class.
 */
@Mod(StructureLibAPI.MOD_ID)
public class StructureLib {

    private static final String STRUCTURECOMPAT_MODID = "structurecompat";
    public static boolean DEBUG_MODE;
    public static boolean PANIC_MODE = Boolean.getBoolean("structurelib.panic");
    public static final Logger LOGGER = LogManager.getLogger("StructureLib");

    public static final Networking CHANNEL = NetworkImpl.getNetwork(ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "main"), 0,false);

    public static final ResourceLocation ALIGNMENT_QUERY = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "alignment_query");
    public static final ResourceLocation ALIGNMENT_DATA = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "alignment_data");
    public static final ResourceLocation UPDATE_HINT_PARTICLE = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "update_hint_particle");
    public static final ResourceLocation ERROR_HINT_PARTICLE = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "error_hint_particle");
    public static final ResourceLocation SET_CHANNEL_DATA = ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "set_channel_data");

    static CommonProxy proxy;

    public static void init() {
        StructureLibNetworking.init();
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

    public StructureLib(IEventBus bus){
        bus.addListener(this::onRegisterBlock);
        bus.addListener(this::buildContents);
        NeoForge.EVENT_BUS.addListener(this::onCommandRegistration);
        preInit();
        if (FMLEnvironment.dist.isClient()){
            NeoForge.EVENT_BUS.addListener(this::onWorldLoad);
        }
    }

    public void preInit() {
        proxy = FMLEnvironment.dist.isDedicatedServer() ? new CommonProxy() : new ClientProxy();
        proxy.preInit();
        StructureLibConfig.init();
        init();
        /*if (Loader.isModLoaded(STRUCTURECOMPAT_MODID)) {
            COMPAT = Loader.instance().getIndexedModList().get(STRUCTURECOMPAT_MODID).getMod();
        }*/
    }

    private void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Add to ingredients tab
        if (event.getTab() == creativeTab) {
            event.accept(Registry.CONSTRUCTABLE_TRIGGER);
            event.accept(Registry.FRONT_ROTATION_TOOL);
        }
    }

    private void onRegisterBlock(RegisterEvent event){
        if (event.getRegistryKey() == Registries.BLOCK) {
            Registry.init();
            creativeTab = CreativeModeTab.builder().icon(() -> new ItemStack(Registry.CONSTRUCTABLE_TRIGGER)).title(Component.translatable("itemGroup." + StructureLibAPI.MOD_ID)).build();
            event.register(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(StructureLibAPI.MOD_ID, "tab"), () -> creativeTab);
        }
    }

    private void onCommandRegistration(RegisterCommandsEvent event){
        CommandConfigureChannels.registerCommands(event.getDispatcher(), event.getCommandSelection());
    }

    @OnlyIn(Dist.CLIENT)
    private void onWorldLoad(LevelEvent.Load event){
        ClientProxy.onLevelLoad(event.getLevel());
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
