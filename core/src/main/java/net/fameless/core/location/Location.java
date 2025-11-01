package net.fameless.core.location;

import com.google.gson.JsonObject;
import net.fameless.core.config.PluginConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;

public record Location(String worldName, double x, double y, double z, float pitch, float yaw) {

    public static @NotNull Location getConfiguredAfkZone() {
        if (!PluginConfig.get().contains("afk-location")) {
            throw new IllegalStateException("AFK location is not configured in the plugin config.");
        }
        Map<String, Object> afkZone = PluginConfig.get().getSection("afk-location");
        return fromMap(afkZone);
    }

    public Location(String worldName, double x, double y, double z) {
        this(worldName, x, y, z, 0f, 0f);
    }

    public @NotNull JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("worldName", worldName);
        obj.addProperty("x", x);
        obj.addProperty("y", y);
        obj.addProperty("z", z);
        obj.addProperty("pitch", pitch);
        obj.addProperty("yaw", yaw);
        return obj;
    }

    public static @NotNull Location fromJson(@NotNull JsonObject json) {
        String worldName = json.get("worldName").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        float pitch = json.has("pitch") ? json.get("pitch").getAsFloat() : 0.0f;
        float yaw = json.has("yaw") ? json.get("yaw").getAsFloat() : 0.0f;
        return new Location(worldName, x, y, z, pitch, yaw);
    }

    public @NotNull @Unmodifiable Map<String, Object> toMap() {
        return Map.of(
                "world", worldName,
                "x", x,
                "y", y,
                "z", z,
                "pitch", pitch,
                "yaw", yaw
        );
    }

    public static @NotNull Location fromMap(@NotNull Map<String, Object> map) {
        String worldName = map.get("world").toString();
        double x = Double.parseDouble(map.get("x").toString());
        double y = Double.parseDouble(map.get("y").toString());
        double z = Double.parseDouble(map.get("z").toString());
        float pitch = map.containsKey("pitch") ? Float.parseFloat(map.get("pitch").toString()) : 0.0f;
        float yaw = map.containsKey("yaw") ? Float.parseFloat(map.get("yaw").toString()) : 0.0f;
        return new Location(worldName, x, y, z, pitch, yaw);
    }

    public @NotNull String getCoordinates() {
        return String.format("X: %.2f, Y: %.2f, Z: %.2f",
                x, y, z);
    }

    public boolean equalsIgnorePitchAndYaw(@NotNull Location other) {
        return this.worldName.equals(other.worldName) &&
               this.x == other.x &&
               this.y == other.y &&
               this.z == other.z;
    }

    public @NotNull Location getBlockLocation() {
        return new Location(
                this.worldName,
                Math.floor(this.x),
                Math.floor(this.y),
                Math.floor(this.z)
        );
    }

    public boolean equalsBlock(@NotNull Location other) {
        return getBlockLocation().equals(other.getBlockLocation());
    }

    public @NotNull Location floor() {
        return new Location(
                this.worldName,
                Math.floor(this.x),
                Math.floor(this.y),
                Math.floor(this.z),
                this.pitch,
                this.yaw
        );
    }

    public @NotNull Location withoutPitchAndYaw() {
        return new Location(this.worldName, this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Location(String name, double x1, double y1, double z1, float pitch1, float yaw1) &&
               this.worldName.equals(name) &&
               this.x == x1 &&
               this.y == y1 &&
               this.z == z1 &&
               this.pitch == pitch1 &&
               this.yaw == yaw1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z, pitch, yaw);
    }

    @Override
    public @NotNull String toString() {
        return toJson().toString();
    }
}
