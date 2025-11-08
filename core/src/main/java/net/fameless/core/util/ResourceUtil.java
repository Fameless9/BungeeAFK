package net.fameless.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class ResourceUtil {

    private static final Gson GSON = new GsonBuilder()
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

    @Contract("_ -> new")
    public static @NotNull File getFile(String path) {
        return new File(Objects.requireNonNull(ResourceUtil.class.getClassLoader().getResource(path)).getFile());
    }

    public static JsonObject readJsonResource(String path) {
        try (InputStream in = ResourceUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new FileNotFoundException("Resource not found: " + path);
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static @NotNull File extractResourceIfMissing(String resourcePath, @NotNull File targetFile) {
        if (targetFile.exists()) return targetFile;

        try {
            Files.createDirectories(targetFile.getParentFile().toPath());
            try (InputStream in = ResourceUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (in == null) throw new FileNotFoundException("Resource not found in JAR: " + resourcePath);
                Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to extract resource: " + resourcePath, e);
        }
        return targetFile;
    }
}
