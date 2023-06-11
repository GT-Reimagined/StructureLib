package com.gtnewhorizon.structurelib.alignment;

import static java.lang.Math.abs;


import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class IntegerAxisSwap {

    private final Vec3i forFirstAxis;
    private final Vec3i forSecondAxis;
    private final Vec3i forThirdAxis;

    public IntegerAxisSwap(Direction forFirstAxis, Direction forSecondAxis, Direction forThirdAxis) {
        this.forFirstAxis = forFirstAxis.getNormal();
        this.forSecondAxis = forSecondAxis.getNormal();
        this.forThirdAxis = forThirdAxis.getNormal();
        if (abs(this.forFirstAxis.getX()) + abs(this.forSecondAxis.getX()) + abs(this.forThirdAxis.getX()) != 1
                || abs(this.forFirstAxis.getY()) + abs(this.forSecondAxis.getY()) + abs(this.forThirdAxis.getY()) != 1
                || abs(this.forFirstAxis.getZ()) + abs(this.forSecondAxis.getZ()) + abs(this.forThirdAxis.getZ())
                        != 1) {
            throw new IllegalArgumentException(
                    "Axis are overlapping/missing! " + forFirstAxis
                            .name() + " " + forSecondAxis.name() + " " + forThirdAxis.name());
        }
    }

    public Vec3i translate(Vec3i point) {
        return new Vec3i(
                forFirstAxis.getX() * point.getX() + forFirstAxis.getY() * point.getY()
                        + forFirstAxis.getZ() * point.getZ(),
                forSecondAxis.getX() * point.getX() + forSecondAxis.getY() * point.getY()
                        + forSecondAxis.getZ() * point.getZ(),
                forThirdAxis.getX() * point.getX() + forThirdAxis.getY() * point.getY()
                        + forThirdAxis.getZ() * point.getZ());
    }

    public Vec3i inverseTranslate(Vec3i point) {
        return new Vec3i(
                forFirstAxis.getX() * point.getX() + forSecondAxis.getX() * point.getY()
                        + forThirdAxis.getX() * point.getZ(),
                forFirstAxis.getY() * point.getX() + forSecondAxis.getY() * point.getY()
                        + forThirdAxis.getY() * point.getZ(),
                forFirstAxis.getZ() * point.getX() + forSecondAxis.getZ() * point.getY()
                        + forThirdAxis.getZ() * point.getZ());
    }

    public Vec3 translate(Vec3 point) {
        return new Vec3(
                forFirstAxis.getX() * point.x + forFirstAxis.getY() * point.y
                        + forFirstAxis.getZ() * point.z,
                forSecondAxis.getX() * point.x + forSecondAxis.getY() * point.y
                        + forSecondAxis.getZ() * point.z,
                forThirdAxis.getX() * point.x + forThirdAxis.getY() * point.y
                        + forThirdAxis.getZ() * point.z);
    }

    public Vec3 inverseTranslate(Vec3 point) {
        return new Vec3(
                forFirstAxis.getX() * point.x + forSecondAxis.getX() * point.y
                        + forThirdAxis.getX() * point.z,
                forFirstAxis.getY() * point.x + forSecondAxis.getY() * point.y
                        + forThirdAxis.getY() * point.z,
                forFirstAxis.getZ() * point.x + forSecondAxis.getZ() * point.y
                        + forThirdAxis.getZ() * point.z);
    }

    public void translate(int[] point, int[] out) {
        out[0] = forFirstAxis.getX() * point[0] + forFirstAxis.getY() * point[1] + forFirstAxis.getZ() * point[2];
        out[1] = forSecondAxis.getX() * point[0] + forSecondAxis.getY() * point[1] + forSecondAxis.getZ() * point[2];
        out[2] = forThirdAxis.getX() * point[0] + forThirdAxis.getY() * point[1] + forThirdAxis.getZ() * point[2];
    }

    public void inverseTranslate(int[] point, int[] out) {
        out[0] = forFirstAxis.getX() * point[0] + forSecondAxis.getX() * point[1] + forThirdAxis.getX() * point[2];
        out[1] = forFirstAxis.getY() * point[0] + forSecondAxis.getY() * point[1] + forThirdAxis.getY() * point[2];
        out[2] = forFirstAxis.getZ() * point[0] + forSecondAxis.getZ() * point[1] + forThirdAxis.getZ() * point[2];
    }

    public void translate(double[] point, double[] out) {
        out[0] = forFirstAxis.getX() * point[0] + forFirstAxis.getY() * point[1] + forFirstAxis.getZ() * point[2];
        out[1] = forSecondAxis.getX() * point[0] + forSecondAxis.getY() * point[1] + forSecondAxis.getZ() * point[2];
        out[2] = forThirdAxis.getX() * point[0] + forThirdAxis.getY() * point[1] + forThirdAxis.getZ() * point[2];
    }

    public void inverseTranslate(double[] point, double[] out) {
        out[0] = forFirstAxis.getX() * point[0] + forSecondAxis.getX() * point[1] + forThirdAxis.getX() * point[2];
        out[1] = forFirstAxis.getY() * point[0] + forSecondAxis.getY() * point[1] + forThirdAxis.getY() * point[2];
        out[2] = forFirstAxis.getZ() * point[0] + forSecondAxis.getZ() * point[1] + forThirdAxis.getZ() * point[2];
    }
}
