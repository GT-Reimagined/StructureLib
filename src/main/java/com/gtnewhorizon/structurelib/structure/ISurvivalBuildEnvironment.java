package com.gtnewhorizon.structurelib.structure;


import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/*
 * Architecture notes: this should never get any callback from survival build. All structure significant code belongs
 * within the IStructureElement. This will make the user easier to maintain the IStructureDefinition. Structure Element
 * knows the most about what this structure element should do, not the caller.
 */
public interface ISurvivalBuildEnvironment {

    /**
     * Get the source of the items. This will be invoked exactly once per structure element build.
     * <p>
     * If the call chain ever got into the old API, this will be called at least once per
     * {@link IStructureDefinition#survivalBuild(Object, ItemStack, String, Level, ExtendedFacing, int, int, int, int, int, int, int, ISurvivalBuildEnvironment, boolean)}
     * call.
     */
    IItemSource getSource();

    /**
     * Get the origin of action. This will be invoked at least once per
     * {@link IStructureDefinition#survivalBuild(Object, ItemStack, String, Level, ExtendedFacing, int, int, int, int, int, int, int, ISurvivalBuildEnvironment, boolean)}
     * call.
     */
    Player getActor();

    /**
     * Create a default implementation with three parameters all being constants.
     *
     * @param source from where to drain resource
     * @param actor  source of action
     */
    static ISurvivalBuildEnvironment create(IItemSource source, Player actor) {
        return new DefaultSurvivalBuildEnvironment(source, actor);
    }
}
