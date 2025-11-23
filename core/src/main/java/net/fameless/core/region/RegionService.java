package net.fameless.core.region;

import net.fameless.core.config.Config;
import net.fameless.core.location.Location;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionService {

    private static final Logger LOGGER = LoggerFactory.getLogger("BungeeAFK/RegionService");

    private static final class Holder {
        public static final RegionService INSTANCE = new RegionService();
    }

    public static RegionService getInstance() {
        return Holder.INSTANCE;
    }

    private static final Object writeLock = new Object();

    private volatile List<Region> regions = List.of();

    private RegionService() {
        LOGGER.info("Initializing RegionService...");
        loadFromConfig();
    }

    public void loadFromConfig() {
        synchronized (writeLock) {
            List<Region> loaded = readRegionsFromConfig();
            regions = List.copyOf(loaded);
            LOGGER.info("Loaded {} region(s)", regions.size());
        }
    }

    public boolean addRegion(@NotNull Region region) {
        synchronized (writeLock) {
            if (containsRegion(region.getRegionName())) return false;

            List<Region> newList = new ArrayList<>(regions);
            newList.add(region);
            regions = List.copyOf(newList);

            saveRegionsToConfig(newList);
            return true;
        }
    }

    public boolean removeRegion(String name) {
        synchronized (writeLock) {
            List<Region> newList = new ArrayList<>(regions);
            boolean removed = newList.removeIf(r -> r.getRegionName().equalsIgnoreCase(name));
            if (!removed) return false;

            regions = List.copyOf(newList);
            saveRegionsToConfig(newList);
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
            saveRegionsToConfig(regions);
        }
    }

    private @NotNull List<Region> readRegionsFromConfig() {
        List<Region> list = new ArrayList<>();
        Map<String, Object> regionSection = Config.getInstance().getSection("bypass-regions");

        regionSection.values().forEach(regionEntry -> {
            if (!(regionEntry instanceof Map<?,?>)) {
                LOGGER.warn("Invalid Region entry found in config: {}", regionEntry.toString());
            } else {
                list.add(Region.fromMap((Map<String, Object>) regionEntry));
            }
        });

        return list;
    }

    private void saveRegionsToConfig(@NotNull List<Region> regions) {
        Map<String, Object> map = new HashMap<>();
        for (Region region : regions) {
            map.put(region.getRegionName(), region.toMap());
        }

        Config.getInstance().set("bypass-regions", map);
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
