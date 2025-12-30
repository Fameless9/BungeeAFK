package net.fameless.core.config.adapter;

import net.fameless.core.region.Region;

import java.util.Map;

public class RegionTypeAdapter implements TypeAdapter<Region> {

    @Override
    @SuppressWarnings("unchecked")
    public Region adapt(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                    "RegionTypeAdapter expected Map<String, Object>, got: " + value.getClass()
            );
        }
        return Region.fromMap((Map<String, Object>) value);
    }
}
