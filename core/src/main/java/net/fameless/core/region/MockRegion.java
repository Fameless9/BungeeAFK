package net.fameless.core.region;

import net.fameless.core.location.Location;
import org.jetbrains.annotations.NotNull;

// Test region to check whether AFK-Location is within the region.
public class MockRegion {

    private final String worldName;
    private final Location corner1;
    private final Location corner2;

    public MockRegion(@NotNull Location corner1, @NotNull Location corner2) {
        if (!corner1.getWorldName().equalsIgnoreCase(corner2.getWorldName())) {
            throw new IllegalArgumentException("Both corners must be in the same world.");
        }

        this.worldName = corner1.getWorldName();
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public boolean isLocationInRegion(@NotNull Location location) {
        if (!location.getWorldName().equalsIgnoreCase(worldName)) return false;

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}
