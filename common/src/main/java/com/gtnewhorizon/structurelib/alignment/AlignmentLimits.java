package com.gtnewhorizon.structurelib.alignment;


import com.gtnewhorizon.structurelib.alignment.enumerable.Flip;
import com.gtnewhorizon.structurelib.alignment.enumerable.Rotation;
import net.minecraft.core.Direction;

class AlignmentLimits implements IAlignmentLimits {

    protected final boolean[] validStates;

    AlignmentLimits(boolean[] validStates) {
        this.validStates = validStates;
    }

    @Override
    public boolean isNewExtendedFacingValid(Direction direction, Rotation rotation, Flip flip) {
        return validStates[IAlignment.getAlignmentIndex(direction, rotation, flip)];
    }
}
