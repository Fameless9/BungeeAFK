package net.fameless.core.region;

import net.fameless.core.util.Location;
import org.jetbrains.annotations.NotNull;

// Test region to check whether AFK-Location is within the region.
public class MockRegion {

    private final String worldName;
    private final Location corner1;
    private final Location corner2;

    public MockRegion(@NotNull Location corner1, @NotNull Location corner2) {
        if (!corner1.worldName().equalsIgnoreCase(corner2.worldName())) {
            throw new IllegalArgumentException("Both corners must be in the same world.");
        }

        this.worldName = corner1.worldName();
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public boolean isLocationInRegion(@NotNull Location location) {
        if (!location.worldName().equalsIgnoreCase(worldName)) return false;

        double minX = Math.min(corner1.x(), corner2.x());
        double maxX = Math.max(corner1.x(), corner2.x());
        double minY = Math.min(corner1.y(), corner2.y());
        double maxY = Math.max(corner1.y(), corner2.y());
        double minZ = Math.min(corner1.z(), corner2.z());
        double maxZ = Math.max(corner1.z(), corner2.z());

        return location.x() >= minX && location.x() <= maxX &&
                location.y() >= minY && location.y() <= maxY &&
                location.z() >= minZ && location.z() <= maxZ;
    }
}
