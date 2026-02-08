package me.bubner.statsring;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Cloth Config screen for StatsRing settings.
 * Replaces the original Vigilance config GUI from config.js.
 *
 * @author Lucas Bubner, 2023-2025
 */
public class ConfigScreen {
    private ConfigScreen() {
    }

    /**
     * Create the Cloth Config settings screen.
     *
     * @param parent the parent screen to return to
     * @return the built config screen
     */
    public static Screen create(Screen parent) {
        ModConfig config = StatsRing.CONFIG;
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("\u00A7lStats Ring: \u00A7r\u00A7cHealth \u00A7rand \u00A7bMana \u00A7rring"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Core category
        ConfigCategory core = builder.getOrCreateCategory(Component.literal("Core"));
        core.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable ring"), config.getActive())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Render the ring (on/off)."))
                .setSaveConsumer(val -> config.properties.setProperty("active", String.valueOf(val)))
                .build());
        core.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show percentages"), config.getPercentage())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Render percentages of health/mana next to ring (on/off)."))
                .setSaveConsumer(val -> config.properties.setProperty("percentage", String.valueOf(val)))
                .build());
        core.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show absorption"), config.getAbsorption())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show over 100% health for absorption hearts (on/off)."))
                .setSaveConsumer(val -> config.properties.setProperty("absorption", String.valueOf(val)))
                .build());

        // Display category
        ConfigCategory display = builder.getOrCreateCategory(Component.literal("Display"));
        display.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show backing image"), config.getBackingImage())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show the background image for the ring that surrounds the bars (on/off)."))
                .setSaveConsumer(val -> config.properties.setProperty("backingImage", String.valueOf(val)))
                .build());
        display.addEntry(entryBuilder.startBooleanToggle(Component.literal("Interpolate colour"), config.getInterpolateColour())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Linear interpolates mana and health colour depending on percentage filled (on/off)."))
                .setSaveConsumer(val -> config.properties.setProperty("interpolateColour", String.valueOf(val)))
                .build());
        display.addEntry(entryBuilder.startBooleanToggle(Component.literal("Interpolate bars"), config.getInterpolateBars())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Linear interpolates the filled progress of the bars (on/off)."))
                .setSaveConsumer(val -> config.properties.setProperty("interpolateBars", String.valueOf(val)))
                .build());

        // Visual Warnings category
        ConfigCategory warnings = builder.getOrCreateCategory(Component.literal("Visual Warnings"));
        warnings.addEntry(entryBuilder.startIntField(Component.literal("Low HP percent alert threshold"), (int) config.getAlertLowHpPercent())
                .setDefaultValue(40)
                .setMin(-1)
                .setMax(100)
                .setTooltip(Component.literal("Visually flashes HP when equal to or below this percentage (% from 0 to 100, -1 to disable)"))
                .setSaveConsumer(val -> config.properties.setProperty("alertLowHpPercent", String.valueOf(val)))
                .build());
        warnings.addEntry(entryBuilder.startIntField(Component.literal("Low Mana percent alert threshold"), (int) config.getAlertLowManaPercent())
                .setDefaultValue(20)
                .setMin(-1)
                .setMax(100)
                .setTooltip(Component.literal("Visually flashes Mana when equal to or below this percentage (% from 0 to 100, -1 to disable)"))
                .setSaveConsumer(val -> config.properties.setProperty("alertLowManaPercent", String.valueOf(val)))
                .build());

        builder.setSavingRunnable(config::save);
        return builder.build();
    }
}
