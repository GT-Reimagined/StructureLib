package com.gtnewhorizon.structurelib.fabric;

import com.gtnewhorizon.structurelib.ClientProxy;
import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import net.fabricmc.api.ClientModInitializer;

public class StructureLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientWorldEvents.LOAD.register((client, world) -> ClientProxy.onLevelLoad(world));
    }
}
