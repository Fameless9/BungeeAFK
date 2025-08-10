package net.fameless.core.command;

import com.google.gson.JsonObject;
import net.fameless.core.BungeeAFK;
import net.fameless.core.caption.Caption;
import net.fameless.core.caption.Language;
import net.fameless.core.command.framework.CallerType;
import net.fameless.core.command.framework.Command;
import net.fameless.core.command.framework.CommandCaller;
import net.fameless.core.config.PluginConfig;
import net.fameless.core.detection.autoclicker.ActionOnDetection;
import net.fameless.core.detection.history.Detection;
import net.fameless.core.detection.history.DetectionType;
import net.fameless.core.handling.AFKHandler;
import net.fameless.core.handling.Action;
import net.fameless.core.location.Location;
import net.fameless.core.player.BAFKPlayer;
import net.fameless.core.region.MockRegion;
import net.fameless.core.region.Region;
import net.fameless.core.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MainCommand extends Command {

    public MainCommand() {
        super(
                "bungeeafk",
                List.of("bafk"),
                CallerType.NONE,
                "/bungeeafk <lang|configure|region|auto-clicker> <reload|<land>|allow-bypass|warning-delay|afk-delay|action-delay|action|caption|afk-location|reloadconfig|disable-server|enable-server|disabled-servers|enable|disable|max-cps|tolerance|toggle-bypass|bypass-permission|action> <param>",
                "bungeeafk.command"
        );
    }

    private final Set<CommandCaller> warnedLocationInRegion = new HashSet<>();

    @Override
    protected void executeCommand(CommandCaller caller, String @NotNull [] args) {
        if (args.length < 2) return;
        if (args[0].equalsIgnoreCase("configure")) {
            AFKHandler afkHandler = BungeeAFK.getAFKHandler();
            switch (args[1]) {
                case "allow-bypass" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    boolean allowBypass;
                    try {
                        allowBypass = Boolean.parseBoolean(args[2]);
                    } catch (IllegalArgumentException e) {
                        caller.sendMessage(Caption.of("command.invalid_boolean"));
                        return;
                    }

                    PluginConfig.get().set("allow-bypass", allowBypass);
                    caller.sendMessage(Caption.of("command.bypass_set",
                            TagResolver.resolver("allow", Tag.inserting(Component.text(allowBypass)))
                    ));
                }
                case "warning-delay" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }

                    long delay;
                    try {
                        int delaySeconds = Integer.parseInt(args[2]);
                        delay = delaySeconds * 1000L;
                    } catch (NumberFormatException e) {
                        caller.sendMessage(Caption.of("command.invalid_number"));
                        return;
                    }
                    afkHandler.setWarnDelayMillis(delay);
                    caller.sendMessage(Caption.of(
                            "command.warn_delay_set",
                            TagResolver.resolver("delay", Tag.inserting(Component.text(delay / 1000)))
                    ));
                }
                case "afk-delay" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }

                    long delay;
                    try {
                        int delaySeconds = Integer.parseInt(args[2]);
                        delay = delaySeconds * 1000L;
                    } catch (NumberFormatException e) {
                        caller.sendMessage(Caption.of("command.invalid_number"));
                        return;
                    }
                    afkHandler.setAfkDelayMillis(delay);
                    caller.sendMessage(Caption.of(
                            "command.afk_delay_set",
                            TagResolver.resolver("delay", Tag.inserting(Component.text(delay / 1000)))
                    ));
                }
                case "action-delay" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }

                    long delay;
                    try {
                        int delaySeconds = Integer.parseInt(args[2]);
                        delay = delaySeconds * 1000L;
                    } catch (NumberFormatException e) {
                        caller.sendMessage(Caption.of("command.invalid_number"));
                        return;
                    }
                    afkHandler.setActionDelayMillis(delay);
                    caller.sendMessage(Caption.of(
                            "command.action_delay_set",
                            TagResolver.resolver("delay", Tag.inserting(Component.text(delay / 1000)))
                    ));
                }
                case "action" -> {
                    Action action;
                    try {
                        action = Action.fromIdentifier(args[2]);
                    } catch (IllegalArgumentException e) {
                        caller.sendMessage(Caption.of("command.invalid_action"));
                        return;
                    }

                    if (!action.isAvailable()) {
                        caller.sendMessage(Caption.of("notification.action_unavailable"));
                        return;
                    }

                    if (action == Action.CONNECT && !BungeeAFK.isProxy()) {
                        caller.sendMessage(Caption.of("notification.action_unavailable"));
                        return;
                    }

                    if (action == Action.CONNECT && !Action.isAfkServerConfigured()) {
                        caller.sendMessage(Caption.of("command.action_not_configured"));
                        return;
                    }

                    afkHandler.setAction(action);
                    caller.sendMessage(Caption.of(
                            "command.action_set",
                            TagResolver.resolver("action", Tag.inserting(Component.text(action.getIdentifier().toLowerCase())))
                    ));
                }
                case "caption" -> {
                    Language language = Language.ofIdentifier(args[2]);
                    if (language == null) {
                        caller.sendMessage(Caption.of("command.invalid_language"));
                        return;
                    }

                    JsonObject languageJsonObject = Caption.getLanguageJsonObject(language);
                    if (languageJsonObject == null) {
                        caller.sendMessage(Caption.of("command.language_not_loaded", TagResolver.resolver("language", Tag.inserting(Component.text(language.getIdentifier())))));
                        return;
                    }

                    String captionKey = args[3];
                    if (!Caption.hasKey(language, captionKey)) {
                        caller.sendMessage(Caption.of("command.no_such_key", TagResolver.resolver("key", Tag.inserting(Component.text(captionKey)))));
                        return;
                    }

                    languageJsonObject.addProperty(captionKey, String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
                    Caption.loadLanguage(language, languageJsonObject);
                    Caption.saveToFile();

                    caller.sendMessage(Caption.of("command.caption_set",
                            TagResolver.resolver("language", Tag.inserting(Component.text(language.getIdentifier()))),
                            TagResolver.resolver("key", Tag.inserting(Component.text(captionKey))),
                            TagResolver.resolver("caption", Tag.inserting(Component.text(Caption.getString(language, captionKey))))
                    ));
                }
                case "afk-location" -> {
                    if (!caller.callerType().equals(CallerType.PLAYER)) {
                        caller.sendMessage(Caption.of("command.not_a_player"));
                        return;
                    }
                    BAFKPlayer<?> player = (BAFKPlayer<?>) caller;
                    Location newLocation = player.getLocation();

                    if (Region.isLocationInAnyRegion(newLocation) && !warnedLocationInRegion.contains(player)) {
                        caller.sendMessage(Caption.of("command.afk_location_in_region"));
                        warnedLocationInRegion.add(player);
                        return;
                    }

                    PluginConfig.get().set("afk-location", player.getLocation().toMap());
                    warnedLocationInRegion.remove(player);
                    player.sendMessage(Caption.of("command.afk_location_set"));
                }
                case "reloadconfig" -> {
                    PluginConfig.reload();
                    afkHandler.fetchConfigValues();
                    BungeeAFK.getAutoClickerDetector().reloadConfigValues();
                    caller.sendMessage(Caption.of("command.config_reloaded"));
                }
                case "saveconfig" -> {
                    PluginConfig.saveNow();
                    caller.sendMessage(Caption.of("command.config_saved"));
                }
                case "disable-server" -> {
                    if (!BungeeAFK.isProxy()) {
                        caller.sendMessage(Caption.of("command.only_proxy"));
                        return;
                    }
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }

                    String serverName = args[2];
                    List<String> disabledServers = PluginConfig.get().getStringList("disabled-servers");

                    if (!BungeeAFK.getPlatform().doesServerExist(serverName)) {
                        caller.sendMessage(Caption.of("command.server_not_found", TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))));
                        return;
                    }

                    if (disabledServers.contains(serverName)) {
                        caller.sendMessage(Caption.of("command.server_already_disabled", TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))));
                        return;
                    }
                    disabledServers.add(serverName);
                    PluginConfig.get().set("disabled-servers", disabledServers);
                    caller.sendMessage(Caption.of("command.server_disabled",
                            TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))
                    ));
                }
                case "enable-server" -> {
                    if (!BungeeAFK.isProxy()) {
                        caller.sendMessage(Caption.of("command.only_proxy"));
                        return;
                    }
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String serverName = args[2];

                    if (!BungeeAFK.getPlatform().doesServerExist(serverName)) {
                        caller.sendMessage(Caption.of("command.server_not_found", TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))));
                        return;
                    }

                    List<String> disabledServers = PluginConfig.get().getStringList("disabled-servers");
                    if (!disabledServers.contains(serverName)) {
                        caller.sendMessage(Caption.of("command.server_already_enabled", TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))));
                        return;
                    }
                    disabledServers.remove(serverName);
                    PluginConfig.get().set("disabled-servers", disabledServers);
                    caller.sendMessage(Caption.of("command.server_enabled",
                            TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))
                    ));
                }
                case "disabled-servers" -> {
                    if (!BungeeAFK.isProxy()) {
                        caller.sendMessage(Caption.of("command.only_proxy"));
                        return;
                    }

                    caller.sendMessage(Caption.of("command.disabled_server_list",
                            TagResolver.resolver("servers", Tag.inserting(Component.text(String.join(", ", PluginConfig.get().getStringList("disabled-servers")))))
                    ));
                }
            }
        } else if (args[0].equalsIgnoreCase("lang")) {
            if (args[1].equalsIgnoreCase("reload")) {
                Caption.loadDefaultLanguages();
                caller.sendMessage(Caption.of("command.languages_reloaded"));
                return;
            }
            Language newLanguage = Language.ofIdentifier(args[1]);
            if (newLanguage == null) {
                caller.sendMessage(Caption.of("command.invalid_language"));
                return;
            }

            Caption.setCurrentLanguage(newLanguage);
            caller.sendMessage(Caption.of(
                    "command.language_changed",
                    TagResolver.resolver("language", Tag.inserting(Component.text(newLanguage.getFriendlyName())))
            ));
        } else if (args[0].equalsIgnoreCase("region")) {
            switch (args[1]) {
                case "reload" -> {
                    PluginConfig.loadBypassRegions();
                    caller.sendMessage(Caption.of("command.regions_reloaded"));
                }
                case "list" -> {
                    List<String> regions = Region.getAllRegions().stream().map(Region::getRegionName).toList();
                    if (regions.isEmpty()) {
                        caller.sendMessage(Caption.of("command.no_regions_found"));
                    } else {
                        caller.sendMessage(Caption.of("command.region_list",
                                TagResolver.resolver("regions", Tag.inserting(Component.text(String.join(", ", regions))))
                        ));
                    }
                }
                case "details" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String regionName = args[2];
                    Region region = Region.getRegionByName(regionName).orElse(null);
                    if (region == null) {
                        caller.sendMessage(Caption.of("command.region_not_found", TagResolver.resolver("region", Tag.inserting(Component.text(regionName)))));
                        return;
                    }
                    caller.sendMessage(Caption.of("command.region_details",
                            TagResolver.resolver("region", Tag.inserting(Component.text(region.getRegionName()))),
                            TagResolver.resolver("world", Tag.inserting(Component.text(region.getWorldName()))),
                            TagResolver.resolver("corner1", Tag.inserting(Component.text(region.getCorner1().getCoordinates()))),
                            TagResolver.resolver("corner2", Tag.inserting(Component.text(region.getCorner2().getCoordinates()))),
                            TagResolver.resolver("afk-detection", Tag.inserting(Component.text(region.isAfkDetectionEnabled() ? "enabled" : "disabled")))
                    ));
                }
                case "toggle-detection" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String regionName = args[2];
                    Region region = Region.getRegionByName(regionName).orElse(null);
                    if (region == null) {
                        caller.sendMessage(Caption.of("command.region_not_found", TagResolver.resolver("region", Tag.inserting(Component.text(regionName)))));
                        return;
                    }
                    region.toggleAfkDetection();
                    caller.sendMessage(Caption.of("command.region_detection_toggled",
                            TagResolver.resolver("region", Tag.inserting(Component.text(region.getRegionName()))),
                            TagResolver.resolver("afk-detection", Tag.inserting(Component.text(region.isAfkDetectionEnabled() ? "enabled" : "disabled")))
                    ));
                }
                case "remove" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String regionName = args[2];
                    Region region = Region.getAllRegions().stream()
                            .filter(r -> r.getRegionName().equalsIgnoreCase(regionName))
                            .findFirst()
                            .orElse(null);
                    if (region == null) {
                        caller.sendMessage(Caption.of("command.region_not_found", TagResolver.resolver("region", Tag.inserting(Component.text(regionName)))));
                        return;
                    }
                    Region.removeRegion(region);
                    caller.sendMessage(Caption.of("command.region_deleted", TagResolver.resolver("region", Tag.inserting(Component.text(regionName)))));
                }
                case "add" -> {
                    if (args.length < 10) {
                        caller.sendMessage(Caption.of("command.invalid_region_format"));
                        return;
                    }
                    String regionName = args[2];
                    String worldName = args[3];

                    double x1, y1, z1, x2, y2, z2;
                    try {
                        x1 = Double.parseDouble(args[4]);
                        y1 = Double.parseDouble(args[5]);
                        z1 = Double.parseDouble(args[6]);
                        x2 = Double.parseDouble(args[7]);
                        y2 = Double.parseDouble(args[8]);
                        z2 = Double.parseDouble(args[9]);
                    } catch (NumberFormatException e) {
                        caller.sendMessage(Caption.of("command.invalid_number"));
                        return;
                    }

                    if (Region.getRegionByName(regionName).isPresent()) {
                        caller.sendMessage(Caption.of("command.region_already_exists", TagResolver.resolver("region", Tag.inserting(Component.text(regionName)))));
                        return;
                    }

                    MockRegion mockRegion = new MockRegion(
                            new Location(worldName, x1, y1, z1),
                            new Location(worldName, x2, y2, z2)
                    );

                    if (mockRegion.isLocationInRegion(Location.getConfiguredAfkZone()) && !warnedLocationInRegion.contains(caller)) {
                        caller.sendMessage(Caption.of("command.afk_location_in_region"));
                        warnedLocationInRegion.add(caller);
                        return;
                    }
                    warnedLocationInRegion.remove(caller);

                    try {
                        Region region = new Region(regionName, new Location(worldName, x1, y1, z1),
                                new Location(worldName, x2, y2, z2), false);
                        caller.sendMessage(Caption.of("command.region_created", TagResolver.resolver("region", Tag.inserting(Component.text(region.getRegionName())))));
                    } catch (IllegalArgumentException e) {
                        caller.sendMessage(Caption.of("command.invalid_region_format"));
                    }
                }
                default -> sendUsage(caller);
            }
        } else if (args[0].equalsIgnoreCase("auto-clicker")) {
            switch (args[1]) {
                case "enable" -> {
                    PluginConfig.get().set("auto-clicker.enabled", true);
                    caller.sendMessage(Caption.of("command.auto_clicker_detection_enabled"));
                }
                case "disable" -> {
                    PluginConfig.get().set("auto-clicker.enabled", false);
                    caller.sendMessage(Caption.of("command.auto_clicker_detection_disabled"));
                }
                case "toggle-bypass" -> {
                    boolean allowBypass = PluginConfig.get().getBoolean("auto-clicker.allow-bypass", true);
                    PluginConfig.get().set("auto-clicker.allow-bypass", !allowBypass);

                    caller.sendMessage(Caption.of("command.auto_clicker_bypass_toggled",
                            TagResolver.resolver("bypass", Tag.inserting(Component.text(!allowBypass)))
                    ));
                }
                case "toggle-on-server" -> {
                    if (!BungeeAFK.isProxy()) {
                        caller.sendMessage(Caption.of("command.only_proxy"));
                        return;
                    }
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String serverName = args[2];
                    if (!BungeeAFK.getPlatform().doesServerExist(serverName)) {
                        caller.sendMessage(Caption.of("command.server_not_found", TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))));
                        return;
                    }

                    List<String> disabledServers = PluginConfig.get().getStringList("auto-clicker.disabled-servers");
                    boolean enabled = disabledServers.contains(serverName);
                    if (enabled) {
                        disabledServers.remove(serverName);
                        PluginConfig.get().set("auto-clicker.disabled-servers", disabledServers);
                    } else {
                        disabledServers.add(serverName);
                        PluginConfig.get().set("auto-clicker.disabled-servers", disabledServers);
                    }
                    caller.sendMessage(Caption.of("command.auto_clicker_detection_toggled_on_server",
                            TagResolver.resolver("server", Tag.inserting(Component.text(serverName))),
                            TagResolver.resolver("status", Tag.inserting(Component.text(!enabled)))
                    ));
                }
                case "action" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String actionIdentifier = args[2];
                    ActionOnDetection action;
                    try {
                        action = ActionOnDetection.fromIdentifier(actionIdentifier);
                    } catch (IllegalArgumentException e) {
                        caller.sendMessage(Caption.of("command.auto_clicker_invalid_action"));
                        return;
                    }

                    PluginConfig.get().set("auto-clicker.action", action.getIdentifier());
                    caller.sendMessage(Caption.of("command.auto_clicker_action_set",
                            TagResolver.resolver("action", Tag.inserting(Component.text(action.getIdentifier())))));
                }
                case "toggle-notify-player" -> {
                    boolean notifyPlayer = PluginConfig.get().getBoolean("auto-clicker.notify-player", true);
                    PluginConfig.get().set("auto-clicker.notify-player", !notifyPlayer);
                    caller.sendMessage(Caption.of("command.auto_clicker_notify_player_toggled",
                            TagResolver.resolver("notify", Tag.inserting(Component.text(!notifyPlayer)))
                    ));
                }
                case "detection-history" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String playerName = args[2];
                    BAFKPlayer<?> player = BAFKPlayer.of(playerName).orElse(null);
                    if (player == null) {
                        caller.sendMessage(Caption.of("command.player_not_found", TagResolver.resolver("player", Tag.inserting(Component.text(playerName)))));
                        return;
                    }

                    List<Detection> detections = Detection.getDetectionsByPlayer(player.getName(), DetectionType.AUTO_CLICKER);
                    if (detections.isEmpty()) {
                        caller.sendMessage(Caption.of("command.auto_clicker_no_detections_found", TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))));
                    } else {
                        List<String> detectionStrings = detections.stream()
                                .map(detection ->  "- " + detection.getFriendlyString())
                                .toList();

                        caller.sendMessage(Caption.of("command.auto_clicker_detection_history",
                                TagResolver.resolver("player", Tag.inserting(Component.text(player.getName()))),
                                TagResolver.resolver("detection-count", Tag.inserting(Component.text(detections.size()))),
                                TagResolver.resolver("detections", Tag.inserting(Component.text(String.join(",\n", detectionStrings))))
                        ));
                    }
                }
            }
            BungeeAFK.getAutoClickerDetector().reloadConfigValues();
        } else if (args[0].equalsIgnoreCase("movement-pattern")) {
            switch (args[1]) {
                case "enable" -> {
                    PluginConfig.get().set("movement-pattern.enabled", true);
                    caller.sendMessage(Caption.of("command.movement_pattern_detection_enabled"));
                }
                case "disable" -> {
                    PluginConfig.get().set("movement-pattern.enabled", false);
                    caller.sendMessage(Caption.of("command.movement_pattern_detection_disabled"));
                }
                case "toggle-bypass" -> {
                    boolean allowBypass = PluginConfig.get().getBoolean("movement-pattern.allow-bypass", true);
                    PluginConfig.get().set("movement-pattern.allow-bypass", !allowBypass);

                    caller.sendMessage(Caption.of("command.movement_pattern_bypass_toggled",
                            TagResolver.resolver("bypass", Tag.inserting(Component.text(!allowBypass)))
                    ));
                }
                case "toggle-on-server" -> {
                    if (!BungeeAFK.isProxy()) {
                        caller.sendMessage(Caption.of("command.only_proxy"));
                        return;
                    }
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String serverName = args[2];
                    if (!BungeeAFK.getPlatform().doesServerExist(serverName)) {
                        caller.sendMessage(Caption.of("command.server_not_found", TagResolver.resolver("server", Tag.inserting(Component.text(serverName)))));
                        return;
                    }

                    List<String> disabledServers = PluginConfig.get().getStringList("movement-pattern.disabled-servers");
                    boolean enabled = disabledServers.contains(serverName);
                    if (enabled) {
                        disabledServers.remove(serverName);
                        PluginConfig.get().set("movement-pattern.disabled-servers", disabledServers);
                    } else {
                        disabledServers.add(serverName);
                        PluginConfig.get().set("movement-pattern.disabled-servers", disabledServers);
                    }
                    caller.sendMessage(Caption.of("command.movement_pattern_detection_toggled_on_server",
                            TagResolver.resolver("server", Tag.inserting(Component.text(serverName))),
                            TagResolver.resolver("status", Tag.inserting(Component.text(!enabled)))
                    ));
                }
                case "action" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String actionIdentifier = args[2];
                    ActionOnDetection action;
                    try {
                        action = ActionOnDetection.fromIdentifier(actionIdentifier);
                    } catch (IllegalArgumentException e) {
                        caller.sendMessage(Caption.of("command.movement_pattern_invalid_action"));
                        return;
                    }

                    PluginConfig.get().set("movement-pattern.action", action.getIdentifier());
                    caller.sendMessage(Caption.of("command.movement_pattern_action_set",
                            TagResolver.resolver("action", Tag.inserting(Component.text(action.getIdentifier())))));
                }
                case "toggle-notify-player" -> {
                    boolean notifyPlayer = PluginConfig.get().getBoolean("movement-pattern.notify-player", true);
                    PluginConfig.get().set("movement-pattern.notify-player", !notifyPlayer);
                    caller.sendMessage(Caption.of("command.movement_pattern_notify_player_toggled",
                            TagResolver.resolver("notify", Tag.inserting(Component.text(!notifyPlayer)))
                    ));
                }
                case "detection-history" -> {
                    if (args.length < 3) {
                        sendUsage(caller);
                        return;
                    }
                    String playerName = args[2];
                    BAFKPlayer<?> player = BAFKPlayer.of(playerName).orElse(null);
                    if (player == null) {
                        caller.sendMessage(Caption.of("command.player_not_found", TagResolver.resolver("player", Tag.inserting(Component.text(playerName)))));
                        return;
                    }

                    List<Detection> detections = Detection.getDetectionsByPlayer(player.getName(), DetectionType.MOVEMENT_PATTERN);
                    if (detections.isEmpty()) {
                        caller.sendMessage(Caption.of("command.movement_pattern_no_detections_found", TagResolver.resolver("player", Tag.inserting(Component.text(player.getName())))));
                    } else {
                        List<String> detectionStrings = detections.stream()
                                .map(detection ->  "- " + detection.getFriendlyString())
                                .toList();

                        caller.sendMessage(Caption.of("command.movement_pattern_detection_history",
                                TagResolver.resolver("player", Tag.inserting(Component.text(player.getName()))),
                                TagResolver.resolver("detection-count", Tag.inserting(Component.text(detections.size()))),
                                TagResolver.resolver("detections", Tag.inserting(Component.text(String.join(",\n", detectionStrings))))
                        ));
                    }
                }
                default -> sendUsage(caller);
            }
            BungeeAFK.getMovementPatternDetection().reloadConfigValues();
        } else {
            sendUsage(caller);
        }
    }

    @Override
    protected List<String> tabComplete(CommandCaller caller, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 1 -> completions.addAll(Arrays.asList("configure", "lang", "region", "auto-clicker", "movement-pattern"));
            case 2 -> {
                if (args[0].equalsIgnoreCase("configure")) {
                    completions.addAll(Arrays.asList(
                            "afk-delay", "action-delay", "action", "caption", "warning-delay",
                            "allow-bypass", "reloadconfig", "afk-location", "saveconfig"
                    ));
                    if (BungeeAFK.isProxy()) {
                        completions.addAll(Arrays.asList("disable-server", "enable-server", "disabled-servers"));
                    }
                } else if (args[0].equalsIgnoreCase("lang")) {
                    completions.add("reload");
                    for (Language language : Language.values()) {
                        completions.add(language.getIdentifier());
                    }
                } else if (args[0].equalsIgnoreCase("region")) {
                    completions.addAll(Arrays.asList("reload", "list", "remove", "add", "details", "toggle-detection"));
                } else if (args[0].equalsIgnoreCase("auto-clicker") || args[0].equalsIgnoreCase("movement-pattern")) {
                    completions.addAll(Arrays.asList(
                            "enable", "disable", "toggle-notify-player",
                            "toggle-bypass", "action", "detection-history"
                    ));
                    if (BungeeAFK.isProxy()) {
                        completions.add("toggle-on-server");
                    }
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("configure")) {
                    if (args[1].equalsIgnoreCase("action")) {
                        for (Action action : Action.getAvailableActions()) {
                            completions.add(action.getIdentifier());
                        }
                    } else if (args[1].equalsIgnoreCase("afk-delay")
                            || args[1].equalsIgnoreCase("action-delay")
                            || args[1].equalsIgnoreCase("warning-delay")) {
                        completions.add("<seconds>");
                    } else if (args[1].equalsIgnoreCase("caption")) {
                        for (Language language : Language.values()) {
                            completions.add(language.getIdentifier());
                        }
                    } else if (args[1].equalsIgnoreCase("allow-bypass")) {
                        completions.addAll(Arrays.asList("true", "false"));
                    } else if (args[1].equalsIgnoreCase("disable-server") && BungeeAFK.isProxy()) {
                        List<String> serverNames = new ArrayList<>(BungeeAFK.getPlatform().getServers());
                        serverNames.removeAll(PluginConfig.get().getStringList("disabled-servers"));
                        completions.addAll(serverNames);
                    } else if (args[1].equalsIgnoreCase("enable-server") && BungeeAFK.isProxy()) {
                        completions.addAll(PluginConfig.get().getStringList("disabled-servers"));
                    }
                } else if (args[0].equalsIgnoreCase("region")) {
                    if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("details") || args[1].equalsIgnoreCase("toggle-detection")) {
                        completions.addAll(Region.getAllRegions().stream()
                                .map(Region::getRegionName)
                                .toList());
                    } else if (args[1].equalsIgnoreCase("add")) {
                        completions.add("<regionName>");
                    }
                } else if (args[0].equalsIgnoreCase("auto-clicker")) {
                    switch (args[1].toLowerCase()) {
                        case "action" -> {
                            for (ActionOnDetection action : ActionOnDetection.values()) {
                                completions.add(action.getIdentifier());
                            }
                        }
                        case "detection-history" -> {
                            List<String> players = Detection.getDetections()
                                    .stream()
                                    .filter(d -> d.getType() == DetectionType.AUTO_CLICKER)
                                    .map(Detection::getPlayerName)
                                    .distinct()
                                    .toList();
                            completions.addAll(players);
                        }
                        case "toggle-on-server" -> {
                            if (BungeeAFK.isProxy()) {
                                completions.addAll(BungeeAFK.getPlatform().getServers());
                            } else {
                                completions.add("-- Only available on proxy --");
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("movement-pattern")) {
                    switch (args[1].toLowerCase()) {
                        case "action" -> {
                            for (net.fameless.core.detection.movementpattern.ActionOnDetection action : net.fameless.core.detection.movementpattern.ActionOnDetection.values()) {
                                completions.add(action.getIdentifier());
                            }
                        }
                        case "detection-history" -> {
                            List<String> players = Detection.getDetections()
                                    .stream()
                                    .filter(d -> d.getType() == DetectionType.MOVEMENT_PATTERN)
                                    .map(Detection::getPlayerName)
                                    .distinct()
                                    .toList();
                            completions.addAll(players);
                        }
                        case "toggle-on-server" -> {
                            if (BungeeAFK.isProxy()) {
                                completions.addAll(BungeeAFK.getPlatform().getServers());
                            } else {
                                completions.add("-- Only available on proxy --");
                            }
                        }
                    }
                }
            }
            case 4 -> {
                if (args[0].equalsIgnoreCase("configure") && args[1].equalsIgnoreCase("caption")) {
                    Language language = Language.ofIdentifier(args[2]);
                    if (language != null) {
                        JsonObject languageObj = Caption.getLanguageJsonObject(language);
                        if (languageObj != null) {
                            completions.addAll(languageObj.keySet());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<worldName>");
                }
            }
            case 5 -> {
                if (args[0].equalsIgnoreCase("configure") && args[1].equalsIgnoreCase("caption")) {
                    completions.add("<new-caption>");
                } else if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<x1>");
                }
            }
            case 6 -> {
                if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<y1>");
                }
            }
            case 7 -> {
                if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<z1>");
                }
            }
            case 8 -> {
                if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<x2>");
                }
            }
            case 9 -> {
                if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<y2>");
                }
            }
            case 10 -> {
                if (args[0].equalsIgnoreCase("region") && args[1].equalsIgnoreCase("add")) {
                    completions.add("<z2>");
                }
            }
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}
