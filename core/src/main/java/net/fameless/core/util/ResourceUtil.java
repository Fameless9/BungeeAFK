package net.fameless.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourceUtil {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static @NotNull String readResource(String path) {
        try (InputStream in = ResourceUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new FileNotFoundException("Resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonObject readJsonResource(String path) {
        try (InputStream in = ResourceUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new FileNotFoundException("Resource not found: " + path);
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static @NotNull Path extractResource(@NotNull String resourcePath, @NotNull Path targetFile) {
        try {
            Path parent = targetFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory for " + targetFile, e);
        }

        try (InputStream in = ResourceUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException("Resource not found in JAR: " + resourcePath);
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not extract resource to " + targetFile, e);
        }

        return targetFile;
    }

    public static @NotNull Path extractResourceIfMissing(@NotNull String resourcePath, @NotNull Path targetFile) {
        if (Files.exists(targetFile)) return targetFile;
        return extractResource(resourcePath, targetFile);
    }
}
