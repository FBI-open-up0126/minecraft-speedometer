package com.lucas.speedometer;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class SpeedInfo {
    public double horizontalSpeed = 0;
    public double verticalSpeed = 0;
    public double speed = 0;

    public SpeedInfo(double horizontalSpeed, double verticalSpeed, double speed) {
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
        this.speed = speed;
    }

    public static SpeedInfo calculateSpeedInfo(Vector3d pos1, Vector3d pos2, double elapsedTimeInSecs) {
        return new SpeedInfo(
            horizontalDistance(pos1, pos2) / elapsedTimeInSecs,
            verticalDistance(pos1, pos2) / elapsedTimeInSecs,
            distance(pos1, pos2) / elapsedTimeInSecs
        );
    }

    public static double horizontalDistance(Vector3d pos1, Vector3d pos2) {
        double dx = pos2.x - pos1.x;
        double dz = pos2.z - pos1.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double verticalDistance(Vector3d pos1, Vector3d pos2) {
        double dy = pos1.y - pos2.y;
        return dy;
    }

    public static double distance(Vector3d pos1, Vector3d pos2) {
        double dx = pos2.x - pos1.x;
        double dy = pos2.y - pos1.y;
        double dz = pos2.z - pos1.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
