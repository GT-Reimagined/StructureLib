package com.gtnewhorizon.structurelib.alignment.enumerable;

import com.google.common.collect.ImmutableSet;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IntegerAxisSwap;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.gtnewhorizon.structurelib.alignment.IAlignment.FLIPS_COUNT;
import static com.gtnewhorizon.structurelib.alignment.IAlignment.ROTATIONS_COUNT;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;

public enum ExtendedFacing {

    DOWN_NORMAL_NONE("down normal none"),
    DOWN_NORMAL_HORIZONTAL("down normal horizontal"),
    DOWN_NORMAL_VERTICAL("down normal vertical"),
    DOWN_NORMAL_BOTH("down normal both"),
    DOWN_CLOCKWISE_NONE("down clockwise none"),
    DOWN_CLOCKWISE_HORIZONTAL("down clockwise horizontal"),
    DOWN_CLOCKWISE_VERTICAL("down clockwise vertical"),
    DOWN_CLOCKWISE_BOTH("down clockwise both"),
    DOWN_UPSIDE_DOWN_NONE("down upside down none"),
    DOWN_UPSIDE_DOWN_HORIZONTAL("down upside down horizontal"),
    DOWN_UPSIDE_DOWN_VERTICAL("down upside down vertical"),
    DOWN_UPSIDE_DOWN_BOTH("down upside down both"),
    DOWN_COUNTER_CLOCKWISE_NONE("down counter clockwise none"),
    DOWN_COUNTER_CLOCKWISE_HORIZONTAL("down counter clockwise horizontal"),
    DOWN_COUNTER_CLOCKWISE_VERTICAL("down counter clockwise vertical"),
    DOWN_COUNTER_CLOCKWISE_BOTH("down counter clockwise both"),
    UP_NORMAL_NONE("up normal none"),
    UP_NORMAL_HORIZONTAL("up normal horizontal"),
    UP_NORMAL_VERTICAL("up normal vertical"),
    UP_NORMAL_BOTH("up normal both"),
    UP_CLOCKWISE_NONE("up clockwise none"),
    UP_CLOCKWISE_HORIZONTAL("up clockwise horizontal"),
    UP_CLOCKWISE_VERTICAL("up clockwise vertical"),
    UP_CLOCKWISE_BOTH("up clockwise both"),
    UP_UPSIDE_DOWN_NONE("up upside down none"),
    UP_UPSIDE_DOWN_HORIZONTAL("up upside down horizontal"),
    UP_UPSIDE_DOWN_VERTICAL("up upside down vertical"),
    UP_UPSIDE_DOWN_BOTH("up upside down both"),
    UP_COUNTER_CLOCKWISE_NONE("up counter clockwise none"),
    UP_COUNTER_CLOCKWISE_HORIZONTAL("up counter clockwise horizontal"),
    UP_COUNTER_CLOCKWISE_VERTICAL("up counter clockwise vertical"),
    UP_COUNTER_CLOCKWISE_BOTH("up counter clockwise both"),
    NORTH_NORMAL_NONE("north normal none"),
    NORTH_NORMAL_HORIZONTAL("north normal horizontal"),
    NORTH_NORMAL_VERTICAL("north normal vertical"),
    NORTH_NORMAL_BOTH("north normal both"),
    NORTH_CLOCKWISE_NONE("north clockwise none"),
    NORTH_CLOCKWISE_HORIZONTAL("north clockwise horizontal"),
    NORTH_CLOCKWISE_VERTICAL("north clockwise vertical"),
    NORTH_CLOCKWISE_BOTH("north clockwise both"),
    NORTH_UPSIDE_DOWN_NONE("north upside down none"),
    NORTH_UPSIDE_DOWN_HORIZONTAL("north upside down horizontal"),
    NORTH_UPSIDE_DOWN_VERTICAL("north upside down vertical"),
    NORTH_UPSIDE_DOWN_BOTH("north upside down both"),
    NORTH_COUNTER_CLOCKWISE_NONE("north counter clockwise none"),
    NORTH_COUNTER_CLOCKWISE_HORIZONTAL("north counter clockwise horizontal"),
    NORTH_COUNTER_CLOCKWISE_VERTICAL("north counter clockwise vertical"),
    NORTH_COUNTER_CLOCKWISE_BOTH("north counter clockwise both"),
    SOUTH_NORMAL_NONE("south normal none"),
    SOUTH_NORMAL_HORIZONTAL("south normal horizontal"),
    SOUTH_NORMAL_VERTICAL("south normal vertical"),
    SOUTH_NORMAL_BOTH("south normal both"),
    SOUTH_CLOCKWISE_NONE("south clockwise none"),
    SOUTH_CLOCKWISE_HORIZONTAL("south clockwise horizontal"),
    SOUTH_CLOCKWISE_VERTICAL("south clockwise vertical"),
    SOUTH_CLOCKWISE_BOTH("south clockwise both"),
    SOUTH_UPSIDE_DOWN_NONE("south upside down none"),
    SOUTH_UPSIDE_DOWN_HORIZONTAL("south upside down horizontal"),
    SOUTH_UPSIDE_DOWN_VERTICAL("south upside down vertical"),
    SOUTH_UPSIDE_DOWN_BOTH("south upside down both"),
    SOUTH_COUNTER_CLOCKWISE_NONE("south counter clockwise none"),
    SOUTH_COUNTER_CLOCKWISE_HORIZONTAL("south counter clockwise horizontal"),
    SOUTH_COUNTER_CLOCKWISE_VERTICAL("south counter clockwise vertical"),
    SOUTH_COUNTER_CLOCKWISE_BOTH("south counter clockwise both"),
    WEST_NORMAL_NONE("west normal none"),
    WEST_NORMAL_HORIZONTAL("west normal horizontal"),
    WEST_NORMAL_VERTICAL("west normal vertical"),
    WEST_NORMAL_BOTH("west normal both"),
    WEST_CLOCKWISE_NONE("west clockwise none"),
    WEST_CLOCKWISE_HORIZONTAL("west clockwise horizontal"),
    WEST_CLOCKWISE_VERTICAL("west clockwise vertical"),
    WEST_CLOCKWISE_BOTH("west clockwise both"),
    WEST_UPSIDE_DOWN_NONE("west upside down none"),
    WEST_UPSIDE_DOWN_HORIZONTAL("west upside down horizontal"),
    WEST_UPSIDE_DOWN_VERTICAL("west upside down vertical"),
    WEST_UPSIDE_DOWN_BOTH("west upside down both"),
    WEST_COUNTER_CLOCKWISE_NONE("west counter clockwise none"),
    WEST_COUNTER_CLOCKWISE_HORIZONTAL("west counter clockwise horizontal"),
    WEST_COUNTER_CLOCKWISE_VERTICAL("west counter clockwise vertical"),
    WEST_COUNTER_CLOCKWISE_BOTH("west counter clockwise both"),
    EAST_NORMAL_NONE("east normal none"),
    EAST_NORMAL_HORIZONTAL("east normal horizontal"),
    EAST_NORMAL_VERTICAL("east normal vertical"),
    EAST_NORMAL_BOTH("east normal both"),
    EAST_CLOCKWISE_NONE("east clockwise none"),
    EAST_CLOCKWISE_HORIZONTAL("east clockwise horizontal"),
    EAST_CLOCKWISE_VERTICAL("east clockwise vertical"),
    EAST_CLOCKWISE_BOTH("east clockwise both"),
    EAST_UPSIDE_DOWN_NONE("east upside down none"),
    EAST_UPSIDE_DOWN_HORIZONTAL("east upside down horizontal"),
    EAST_UPSIDE_DOWN_VERTICAL("east upside down vertical"),
    EAST_UPSIDE_DOWN_BOTH("east upside down both"),
    EAST_COUNTER_CLOCKWISE_NONE("east counter clockwise none"),
    EAST_COUNTER_CLOCKWISE_HORIZONTAL("east counter clockwise horizontal"),
    EAST_COUNTER_CLOCKWISE_VERTICAL("east counter clockwise vertical"),
    EAST_COUNTER_CLOCKWISE_BOTH("east counter clockwise both");

    public static final ExtendedFacing DEFAULT = NORTH_NORMAL_NONE;
    public static final ExtendedFacing[] VALUES = values();
    public static final Map<Direction, List<ExtendedFacing>> FOR_FACING = new HashMap<>();
    public static final int STATES_COUNT = VALUES.length;

    static {
        stream(values()).forEach(
                extendedFacing -> FOR_FACING.compute(extendedFacing.direction, ((direction, extendedFacings) -> {
                    if (extendedFacings == null) {
                        extendedFacings = new ArrayList<>();
                    }
                    extendedFacings.add(extendedFacing);
                    return extendedFacings;
                })));
    }

    private static final Map<String, ExtendedFacing> NAME_LOOKUP = stream(VALUES)
            .collect(toMap(ExtendedFacing::getName2, (extendedFacing) -> extendedFacing));
    private static final EnumMap<Direction, ImmutableSet<ExtendedFacing>> LOOKUP_BY_DIRECTION = stream(VALUES)
            .collect(
                    groupingBy(
                            ExtendedFacing::getDirection,
                            () -> new EnumMap<>(Direction.class),
                            collectingAndThen(toSet(), ImmutableSet::copyOf)));
    private static final EnumMap<Rotation, ImmutableSet<ExtendedFacing>> LOOKUP_BY_ROTATION = stream(VALUES).collect(
            groupingBy(
                    ExtendedFacing::getRotation,
                    () -> new EnumMap<>(Rotation.class),
                    collectingAndThen(toSet(), ImmutableSet::copyOf)));
    private static final EnumMap<Flip, ImmutableSet<ExtendedFacing>> LOOKUP_BY_FLIP = stream(VALUES).collect(
            groupingBy(
                    ExtendedFacing::getFlip,
                    () -> new EnumMap<>(Flip.class),
                    collectingAndThen(toSet(), ImmutableSet::copyOf)));
    private final Direction direction;
    private final Direction a, b, c;
    private final Rotation rotation;
    private final Flip flip;

    private final String name;
    private final IntegerAxisSwap integerAxisSwap;

    ExtendedFacing(String name) {
        this.name = name;
        direction = Direction.values()[ordinal() / (ROTATIONS_COUNT * FLIPS_COUNT)];
        rotation = Rotation.VALUES[ordinal() / FLIPS_COUNT - direction.ordinal() * ROTATIONS_COUNT];
        flip = Flip.VALUES[ordinal() % FLIPS_COUNT];
        Direction a, b, c;
        switch (direction) {
            case DOWN:
                a = Direction.WEST;
                b = Direction.SOUTH;
                c = Direction.UP;
                break;
            case UP:
                a = Direction.EAST;
                b = Direction.SOUTH;
                c = Direction.DOWN;
                break;
            case NORTH:
                a = Direction.WEST;
                b = Direction.DOWN;
                c = Direction.SOUTH;
                break;
            case SOUTH:
                a = Direction.EAST;
                b = Direction.DOWN;
                c = Direction.NORTH;
                break;
            case WEST:
                a = Direction.SOUTH;
                b = Direction.DOWN;
                c = Direction.EAST;
                break;
            case EAST:
                a = Direction.NORTH;
                b = Direction.DOWN;
                c = Direction.WEST;
                break;
            default:
                throw new RuntimeException("Is impossible...");
        }
        switch (flip) { // This duplicates some axis swaps since flip boolean would do, but seems more convenient to use
            case HORIZONTAL:
                a = a.getOpposite();
                break;
            case BOTH:
                a = a.getOpposite();
            case VERTICAL:
                b = b.getOpposite();
                break;
            case NONE:
                break;
            default:
                throw new RuntimeException("Even more impossible...");
        }
        switch (rotation) {
            case CLOCKWISE: {
                Direction _a = a;
                a = b;
                b = _a.getOpposite();
                break;
            }
            case UPSIDE_DOWN:
                a = a.getOpposite();
                b = b.getOpposite();
                break;
            case COUNTER_CLOCKWISE: {
                Direction _a = a;
                a = b.getOpposite();
                b = _a;
                break;
            }
            case NORMAL:
                break;
            default:
                throw new RuntimeException("More impossible...");
        }
        this.a = a;
        this.b = b;
        this.c = c;
        integerAxisSwap = new IntegerAxisSwap(a, b, c);
    }

    public static ExtendedFacing of(Direction direction, Rotation rotation, Flip flip) {
        if (direction == null) {
            return VALUES[IAlignment.getAlignmentIndex(Direction.NORTH, rotation, flip)];
        }
        return VALUES[IAlignment.getAlignmentIndex(direction, rotation, flip)];
    }

    public static ExtendedFacing of(Direction direction) {
        if (direction == null) {
            return DEFAULT;
        }
        return VALUES[IAlignment.getAlignmentIndex(direction, Rotation.NORMAL, Flip.NONE)];
    }

    public static ImmutableSet<ExtendedFacing> getAllWith(Direction direction) {
        return LOOKUP_BY_DIRECTION.get(direction);
    }

    public static ImmutableSet<ExtendedFacing> getAllWith(Rotation rotation) {
        return LOOKUP_BY_ROTATION.get(rotation);
    }

    public static ImmutableSet<ExtendedFacing> getAllWith(Flip flip) {
        return LOOKUP_BY_FLIP.get(flip);
    }

    public ExtendedFacing with(Direction direction) {
        return of(direction, rotation, flip);
    }

    public ExtendedFacing with(Rotation rotation) {
        return of(direction, rotation, flip);
    }

    public ExtendedFacing with(Flip flip) {
        return of(direction, rotation, flip);
    }

    public ExtendedFacing getOppositeDirection() {
        return of(direction.getOpposite(), rotation, flip);
    }

    public ExtendedFacing getOppositeRotation() {
        return of(direction, rotation.getOpposite(), flip);
    }

    public ExtendedFacing getOppositeFlip() {
        return of(direction, rotation, flip.getOpposite());
    }

    /**
     * Gets the same effective facing achieved by different rot/flip combo
     *
     * @return same effective facing, but different enum value
     */
    public ExtendedFacing getDuplicate() {
        return of(direction, rotation.getOpposite(), flip.getOpposite());
    }

    public int getIndex() {
        return ordinal();
    }

    public String getName2() {
        return this.name;
    }

    public String getLocalizedName() {
        return Component.translatable("structurelib.facing." + getIndex()).getString();
    }

    public static ExtendedFacing byName(String name) {
        return name == null ? null : NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
    }

    public static ExtendedFacing byIndex(int index) {
        return VALUES[abs(index % VALUES.length)];
    }

    public static ExtendedFacing random(Random rand) {
        return VALUES[rand.nextInt(VALUES.length)];
    }

    public Direction getDirection() {
        return direction;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public Flip getFlip() {
        return flip;
    }

    /**
     * Translates relative to front facing offset to world offset
     *
     * @param abcOffset A,B,C offset (facing relative {@code L-->R,U-->D,F-->B})
     * @return X, Y, Z offset in world
     */
    public Vec3 getLevelOffset(Vec3 abcOffset) {
        return integerAxisSwap.inverseTranslate(abcOffset);
    }

    public Vec3i getLevelOffset(Vec3i abcOffset) {
        return integerAxisSwap.inverseTranslate(abcOffset);
    }

    public void getLevelOffset(int[] point, int[] out) {
        integerAxisSwap.inverseTranslate(point, out);
    }

    public void getLevelOffset(double[] point, double[] out) {
        integerAxisSwap.inverseTranslate(point, out);
    }

    /**
     * do note the ABC coordinate system is (X, -Y, Z).
     */
    public Direction getLevelDirection(Direction abcDirection) {
        switch (abcDirection) {
            case NORTH:
                return getRelativeForwardInLevel();
            case SOUTH:
                return getRelativeBackInLevel();
            case UP:
                return getRelativeDownInLevel();
            case DOWN:
                return getRelativeUpInLevel();
            case EAST:
                return getRelativeLeftInLevel();
            case WEST:
                return getRelativeRightInLevel();
            default:
                return null;
        }
    }

    /**
     * Translates world offset to relative front facing offset
     *
     * @param xyzOffset X,Y,Z offset in world
     * @return A, B, C offset (facing relative {@code L-->R,U-->D,F-->B})
     */
    public Vec3 getOffsetABC(Vec3 xyzOffset) {
        return integerAxisSwap.translate(xyzOffset);
    }

    public Vec3i getOffsetABC(Vec3i xyzOffset) {
        return integerAxisSwap.translate(xyzOffset);
    }

    public void getOffsetABC(int[] point, int[] out) {
        integerAxisSwap.translate(point, out);
    }

    public void getOffsetABC(double[] point, double[] out) {
        integerAxisSwap.translate(point, out);
    }

    public IntegerAxisSwap getIntegerAxisSwap() {
        return integerAxisSwap;
    }

    public Direction getRelativeLeftInLevel() {
        return a;
    }

    public Direction getRelativeRightInLevel() {
        return a.getOpposite();
    }

    public Direction getRelativeDownInLevel() {
        return b;
    }

    public Direction getRelativeUpInLevel() {
        return b.getOpposite();
    }

    public Direction getRelativeBackInLevel() {
        return c;
    }

    public Direction getRelativeForwardInLevel() {
        return c.getOpposite();
    }
}
