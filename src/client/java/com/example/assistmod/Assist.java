package com.example.assistmod;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** The list of available assists. Add an entry here, then handle it in AssistHandler. */
public enum Assist {
    AUTO_BRIDGE("Auto Bridge", "Places a block under your feet when there's air"),
    AUTO_SPRINT("Auto Sprint", "Sprints automatically while moving forward"),
    AUTO_JUMP("Auto Jump", "Hops up one-block ledges automatically"),
    EDGE_GUARD("Edge Guard", "Sneaks automatically at ledges so you don't walk off"),
    AUTO_WALK("Auto Walk", "Holds the forward key for you"),
    AUTO_MINE("Auto Mine", "Holds the attack/mine button for you"),
    FULLBRIGHT("Fullbright", "Night vision so you can see in the dark");

    public final String label;
    public final String description;
    private boolean enabled;

    Assist(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
        save();
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("assistmod.properties");
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            return;
        }
        for (Assist a : values()) {
            a.enabled = Boolean.parseBoolean(props.getProperty(a.name(), "false"));
        }
    }

    public static void save() {
        Properties props = new Properties();
        for (Assist a : values()) {
            props.setProperty(a.name(), Boolean.toString(a.enabled));
        }
        try (OutputStream out = Files.newOutputStream(configPath())) {
            props.store(out, "Assist Menu settings");
        } catch (IOException ignored) {
        }
    }
}
