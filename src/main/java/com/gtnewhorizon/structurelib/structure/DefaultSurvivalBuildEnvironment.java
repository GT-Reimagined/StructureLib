package com.gtnewhorizon.structurelib.structure;


import net.minecraft.world.entity.player.Player;

class DefaultSurvivalBuildEnvironment implements ISurvivalBuildEnvironment {

    private final IItemSource source;
    private final Player actor;

    public DefaultSurvivalBuildEnvironment(IItemSource source, Player actor) {
        this.source = source;
        this.actor = actor;
    }

    @Override
    public IItemSource getSource() {
        return source;
    }

    @Override
    public Player getActor() {
        return actor;
    }
}
