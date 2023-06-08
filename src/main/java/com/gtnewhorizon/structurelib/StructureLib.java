package com.gtnewhorizon.structurelib;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.structurelib.command.CommandConfigureChannels;
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

    @SidedProxy(
            serverSide = "com.gtnewhorizon.structurelib.CommonProxy",
            clientSide = "com.gtnewhorizon.structurelib.ClientProxy")
    static CommonProxy proxy;

    static final SimpleNetworkWrapper net = NetworkRegistry.INSTANCE.newSimpleChannel(StructureLibAPI.MOD_ID);

    static {
        net.registerMessage(
                AlignmentMessage.ServerHandler.class,
                AlignmentMessage.AlignmentQuery.class,
                0,
                Side.SERVER);
        net.registerMessage(AlignmentMessage.ClientHandler.class, AlignmentMessage.AlignmentData.class, 1, Side.CLIENT);
        net.registerMessage(UpdateHintParticleMessage.Handler.class, UpdateHintParticleMessage.class, 2, Side.CLIENT);
        net.registerMessage(SetChannelDataMessage.Handler.class, SetChannelDataMessage.class, 3, Side.SERVER);
        net.registerMessage(ErrorHintParticleMessage.Handler.class, ErrorHintParticleMessage.class, 4, Side.CLIENT);

        try {
            DEBUG_MODE = Boolean.parseBoolean(System.getProperty("structurelib.debug"));
        } catch (IllegalArgumentException | NullPointerException e) {
            // turn on debug by default in dev mode
            // this will be overridden if above property is present and set to false
            DEBUG_MODE = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        }
    }

    public static final XSTR RANDOM = new XSTR();

    @Mod.Instance
    static StructureLib INSTANCE;

    static Object COMPAT;
    public static final CreativeModeTab creativeTab = new CreativeModeTab("structurelib") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registry.HINT_0);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        ConfigurationHandler.INSTANCE.init(e.getSuggestedConfigurationFile());
        proxy.preInit(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance(), new GuiHandler());

        if (Loader.isModLoaded(STRUCTURECOMPAT_MODID)) {
            COMPAT = Loader.instance().getIndexedModList().get(STRUCTURECOMPAT_MODID).getMod();
        }
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
