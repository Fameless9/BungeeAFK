package net.fameless.core.region;

import net.fameless.core.config.PluginConfig;
import net.fameless.core.location.Location;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RegionService {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/RegionService");

    private static final class Holder {
        public static final RegionService INSTANCE = new RegionService(PluginConfig.getInstance());
    }

    public static RegionService getInstance() {
        return Holder.INSTANCE;
    }

    private final Object writeLock = new Object();

    private volatile List<Region> regions = List.of();

    private final PluginConfig pluginConfig;

    private RegionService(PluginConfig pluginConfig) {
        LOGGER.info("Initializing RegionService...");
        this.pluginConfig = pluginConfig;
        loadFromConfig();
    }

    public void loadFromConfig() {
        synchronized (writeLock) {
            List<Region> loaded = pluginConfig.readRegionsFromConfig();
            regions = List.copyOf(loaded);
            LOGGER.info("Loaded {} regions", regions.size());
        }
    }

    public boolean addRegion(Region region) {
        synchronized (writeLock) {
            for (Region r : regions) {
                if (r.getRegionName().equalsIgnoreCase(region.getRegionName())) {
                    return false;
                }
            }
            List<Region> newList = new ArrayList<>(regions);
            newList.add(region);
            regions = List.copyOf(newList);

            pluginConfig.saveRegionsToConfig(newList);
            return true;
        }
    }

    public boolean removeRegion(String name) {
        synchronized (writeLock) {
            List<Region> newList = new ArrayList<>(regions);
            boolean removed = newList.removeIf(r -> r.getRegionName().equalsIgnoreCase(name));
            if (!removed) return false;

            regions = List.copyOf(newList);
            pluginConfig.saveRegionsToConfig(newList);
            return true;
        }
    }

    public boolean containsRegion(String name) {
        for (Region r : regions) {
            if (r.getRegionName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public Region getRegion(String name) {
        for (Region r : regions) {
            if (r.getRegionName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    public void clearRegions() {
        synchronized (writeLock) {
            regions = List.of();
            pluginConfig.saveRegionsToConfig(regions);
        }
    }

    public boolean isLocationInAnyRegion(@NotNull Location location) {
        for (Region region : regions) {
            if (region.isLocationInRegion(location)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLocationInAnyBypassRegion(@NotNull Location location) {
        for (Region region : regions) {
            if (region.isLocationInRegion(location) && !region.isAfkDetectionEnabled()) {
                return true;
            }
        }
        return false;
    }

    public List<Region> getRegions() {
        return regions;
    }
}
