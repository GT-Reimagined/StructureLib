package com.gtnewhorizon.structurelib.command;

import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.constructable.ChannelDataAccessor;
import com.gtnewhorizon.structurelib.item.ItemConstructableTrigger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StructureLibAPI.MOD_ID)
public class CommandConfigureChannels {

    private static DynamicCommandExceptionType INVALID_ITEM = new DynamicCommandExceptionType(s -> new TextComponent("must hold a hologram"));

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("sl_channel")
            .then(Commands.literal("get")
                .then(Commands.argument("channel_name", StringArgumentType.greedyString()))
                .executes(context -> processCommand(context, SubCommand.GET)))
            .then(Commands.literal("set")
                .then(Commands.argument("channel_name", StringArgumentType.greedyString())
                    .then(Commands.argument("value", IntegerArgumentType.integer(1))
                        .executes(context -> processCommand(context, SubCommand.SET)))))
            .then(Commands.literal("unset")
                .then(Commands.argument("channel_name", StringArgumentType.greedyString())
                    .executes(context -> processCommand(context, SubCommand.UNSET))))
            .then(Commands.literal("wipe").executes(context -> processCommand(context, SubCommand.WIPE)))
            .then(Commands.literal("getall").executes(context -> processCommand(context, SubCommand.GETALL))));
    }

    private static int processCommand(CommandContext<CommandSourceStack> context, SubCommand command) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack heldStack = player.getMainHandItem();
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof ItemConstructableTrigger)){
            throw INVALID_ITEM.create(null);
        }
        return switch (command){
            case GET -> {
                String channel = context.getArgument("channel_name", String.class);
                if (ChannelDataAccessor.hasSubChannel(heldStack, channel)){
                    player.sendMessage(new TextComponent(channel + " value: " + ChannelDataAccessor.getChannelData(heldStack, channel)), player.getUUID());
                    yield 1;
                }
                player.sendMessage(new TextComponent(channel + " value: N/A"), player.getUUID());
                yield 0;
            }
            case SET -> {
                String channel = context.getArgument("channel_name", String.class);
                int value = context.getArgument("value", Integer.class);
                ChannelDataAccessor.setChannelData(heldStack, channel, value);
                player.containerMenu.sendAllDataToRemote();
                player.sendMessage(new TextComponent(channel + " value: " + value), player.getUUID());
                yield 1;
            }
            case UNSET -> {
                String channel = context.getArgument("channel_name", String.class);
                if (ChannelDataAccessor.hasSubChannel(heldStack, channel)){
                    ChannelDataAccessor.unsetChannelData(heldStack, channel);
                    player.sendMessage(new TextComponent(channel + " cleared"), player.getUUID());
                    yield 1;
                }
                player.sendMessage(new TextComponent(channel + " no value"), player.getUUID());
                yield 0;
            }
            case WIPE -> {
                ChannelDataAccessor.wipeChannelData(heldStack);
                yield 1;
            }
            case GETALL -> {
                if (!ChannelDataAccessor.hasSubChannel(heldStack)) {
                    player.sendMessage(new TextComponent("No subchannel"), player.getUUID());
                } else {
                    player.sendMessage(new TranslatableComponent(
                        "item.structurelib.constructableTrigger.desc.lshift.0",
                        ChannelDataAccessor.countChannelData(heldStack)), player.getUUID());
                    ChannelDataAccessor.iterateChannelData(heldStack).map(e -> e.getKey() + ": " + e.getValue())
                        .map(TextComponent::new).forEach(c -> player.sendMessage(c, player.getUUID()));
                }
                yield 1;
            }
        };
    }

    enum SubCommand{
        GET, SET, UNSET, WIPE, GETALL;
    }

    @SubscribeEvent
    public static void onCommandRegistration(RegisterCommandsEvent event){
        registerCommands(event.getDispatcher(), event.getEnvironment());
    }
}
