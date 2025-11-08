package net.fameless.core.region;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fameless.core.location.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class Region {

    private static final List<Region> REGIONS = new ArrayList<>();

    private final String regionName;
    private final String worldName;
    private final Location corner1;
    private final Location corner2;
    private boolean afkDetection;

    public Region(String regionName, @NotNull Location corner1, @NotNull Location corner2, boolean afkDetection) {
        if (!corner1.worldName().equalsIgnoreCase(corner2.worldName())) {
            throw new IllegalArgumentException("Both corners must be in the same world.");
        }

        for (Region region : REGIONS) {
            if (region.regionName.equalsIgnoreCase(regionName)) {
                throw new IllegalArgumentException("Region with name '" + regionName + "' already exists.");
            }
        }

        this.regionName = regionName;
        this.worldName = corner1.worldName();
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.afkDetection = afkDetection;
        REGIONS.add(this);
    }

    public void toggleAfkDetection() {
        this.afkDetection = !this.afkDetection;
    }

    public boolean isAfkDetectionEnabled() {
        return afkDetection;
    }

    public String getRegionName() {
        return regionName;
    }

    @Contract(pure = true)
    public static @NotNull @Unmodifiable List<Region> getAllRegions() {
        return List.copyOf(REGIONS);
    }

    public static Optional<Region> getRegionByName(@NotNull String regionName) {
        for (Region region : REGIONS) {
            if (region.regionName.equalsIgnoreCase(regionName)) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
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

    public static void removeRegion(@NotNull Region region) {
        if (!REGIONS.remove(region)) {
            throw new IllegalArgumentException("Region not found: " + region);
        }
    }

    public static void clearRegions() {
        REGIONS.clear();
    }

    public static boolean isLocationInAnyRegion(@NotNull Location location) {
        for (Region region : REGIONS) {
            if (region.isLocationInRegion(location)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLocationInAnyBypassRegion(@NotNull Location location) {
        for (Region region : REGIONS) {
            if (region.isLocationInRegion(location) && !region.isAfkDetectionEnabled()) {
                return true;
            }
        }
        return false;
    }

    public Location getMinimumCorner() {
        return new Location(worldName,
                Math.min(corner1.x(), corner2.x()),
                Math.min(corner1.y(), corner2.y()),
                Math.min(corner1.z(), corner2.z())
        );
    }

    public Location getMaximumCorner() {
        return new Location(worldName,
                Math.max(corner1.x(), corner2.x()),
                Math.max(corner1.y(), corner2.y()),
                Math.max(corner1.z(), corner2.z())
        );
    }

    public double getVolume() {
        double dx = Math.abs(corner1.x() - corner2.x()) + 1;
        double dy = Math.abs(corner1.y() - corner2.y()) + 1;
        double dz = Math.abs(corner1.z() - corner2.z()) + 1;
        return dx * dy * dz;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("regionName", regionName);
        json.addProperty("afkDetection", afkDetection);
        json.addProperty("worldName", worldName);
        json.add("corner1", corner1.toJson());
        json.add("corner2", corner2.toJson());
        return json;
    }

    public static @NotNull Region fromJson(@NotNull JsonObject json) {
        if (!json.has("corner1") || !json.has("corner2") || !json.has("regionName") || !json.has("afkDetection")) {
            throw new JsonParseException("Failed to deserialize Region: Missing required region params in jsonObject.");
        }
        String regionName = json.get("regionName").getAsString();
        boolean afkDetection = json.get("afkDetection").getAsBoolean();
        Location corner1 = Location.fromJson(json.getAsJsonObject("corner1"));
        Location corner2 = Location.fromJson(json.getAsJsonObject("corner2"));
        return new Region(regionName, corner1, corner2, afkDetection);
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "regionName", regionName,
                "afkDetection", afkDetection,
                "worldName", worldName,
                "corner1", corner1.toMap(),
                "corner2", corner2.toMap()
        );
    }

    @Contract("_ -> new")
    public static @NotNull Region fromMap(@NotNull Map<String, Object> map) {
        if (!map.containsKey("corner1") || !map.containsKey("corner2") || !map.containsKey("regionName") || !map.containsKey("afkDetection")) {
            throw new IllegalArgumentException("Failed to deserialize Region: Map must contain 'afkDetection', 'corner1', 'corner2' and 'regionName' keys.");
        }
        String regionName = (String) map.get("regionName");
        boolean afkDetection = (boolean) map.get("afkDetection");
        Location corner1 = Location.fromMap((Map<String, Object>) map.get("corner1"));
        Location corner2 = Location.fromMap((Map<String, Object>) map.get("corner2"));
        return new Region(regionName, corner1, corner2, afkDetection);
    }

    @Override
    public String toString() {
        return "Region{" +
                "regionName='" + regionName + '\'' +
                "worldName='" + worldName + '\'' +
                "afkDetection=" + afkDetection +
                ", corner1=" + corner1 +
                ", corner2=" + corner2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;
        return regionName.equalsIgnoreCase(region.regionName);
    }

    @Override
    public int hashCode() {
        return regionName.toLowerCase().hashCode();
    }
}
