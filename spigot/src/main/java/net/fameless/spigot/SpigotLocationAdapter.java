package net.fameless.spigot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpigotLocationAdapter {

    @Contract("_ -> new")
    public static @NotNull Location adapt(@NotNull net.fameless.core.location.Location location) {
        World world = Bukkit.getWorld(location.worldName());
        if (world == null) {
            throw new IllegalArgumentException("World '" + location.worldName() + "' not found for location conversion");
        }
        return new Location(
            world,
            location.x(),
            location.y(),
            location.z(),
            location.yaw(),
            location.pitch()
        );
    }

    @Contract("_ -> new")
    public static net.fameless.core.location.@NotNull Location adapt(@NotNull Location location) {
        return new net.fameless.core.location.Location(
            Objects.requireNonNull(location.getWorld()).getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getPitch(),
            location.getYaw()
        );
    }

}
