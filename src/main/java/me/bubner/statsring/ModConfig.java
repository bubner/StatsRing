package me.bubner.statsring;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Properties-based configuration for StatsRing.
 * Serialises settings to a .properties file in the Fabric config directory.
 *
 * @author Lucas Bubner, 2023-2025
 */
public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("statsring.properties");
    public final Properties properties = new Properties();

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
                properties.load(reader);
            } catch (IOException e) {
                StatsRing.LOGGER.error("Failed to load config", e);
            }
        } else {
            save();
        }
    }

    public void save() {
        try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
            properties.store(writer, "StatsRing configuration");
        } catch (IOException e) {
            StatsRing.LOGGER.error("Failed to save config", e);
        }
    }

    public boolean getActive() {
        return Boolean.parseBoolean(properties.getProperty("active", "true"));
    }

    public boolean getPercentage() {
        return Boolean.parseBoolean(properties.getProperty("percentage", "true"));
    }

    public boolean getAbsorption() {
        return Boolean.parseBoolean(properties.getProperty("absorption", "true"));
    }

    public boolean getBackingImage() {
        return Boolean.parseBoolean(properties.getProperty("backingImage", "true"));
    }

    public boolean getInterpolateColour() {
        return Boolean.parseBoolean(properties.getProperty("interpolateColour", "true"));
    }

    public boolean getInterpolateBars() {
        return Boolean.parseBoolean(properties.getProperty("interpolateBars", "true"));
    }

    public float getAlertLowHpPercent() {
        try {
            return Float.parseFloat(properties.getProperty("alertLowHpPercent", "40"));
        } catch (NumberFormatException e) {
            return 40f;
        }
    }

    public float getAlertLowManaPercent() {
        try {
            return Float.parseFloat(properties.getProperty("alertLowManaPercent", "20"));
        } catch (NumberFormatException e) {
            return 20f;
        }
    }
}
