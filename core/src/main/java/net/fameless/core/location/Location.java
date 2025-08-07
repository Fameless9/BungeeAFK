package net.fameless.core.location;

import com.google.gson.JsonObject;
import net.fameless.core.config.PluginConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Location {

    public static @NotNull Location getConfiguredAfkZone() {
        if (!PluginConfig.get().contains("afk-location")) {
            throw new IllegalStateException("AFK location is not configured in the plugin config.");
        }
        Map<String, Object> afkZone = PluginConfig.get().getSection("afk-location");
        return fromMap(afkZone);
    }

    private String worldName;
    private double x;
    private double y;
    private double z;
    private float pitch = 0.0f;
    private float yaw = 0.0f;

    public Location(String worldName, double x, double y, double z, float pitch, float yaw) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Location(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public JsonObject toJson() {
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

    public Map<String, Object> toMap() {
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

    public String getCoordinates() {
        return String.format("X: %.2f, Y: %.2f, Z: %.2f",
                x, y, z);
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
