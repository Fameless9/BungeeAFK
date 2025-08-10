package net.fameless.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DetectionUtil {

    public static double calculateStdDev(@NotNull List<Long> intervals) {
        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = intervals.stream()
                .mapToDouble(i -> Math.pow(i - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

}
