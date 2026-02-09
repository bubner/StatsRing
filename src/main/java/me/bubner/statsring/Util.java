package me.bubner.statsring;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.regex.Pattern;

/**
 * Shared utility methods for StatsRing rendering.
 *
 * @author Lucas Bubner, 2023 (Original CT module)
 */
public final class Util {
    private static final Pattern COMMAS_FORMAT_COLOURS = Pattern.compile("(,|§.|§$)");

    private Util() {
    }

    public static boolean isInSkyBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return false;
        return objective.getDisplayName().getString().contains("SKYBLOCK");
    }

    public static float parseStat(String stat) {
        try {
            // Remove any commas or format colours
            return Integer.parseInt(COMMAS_FORMAT_COLOURS.matcher(stat).replaceAll("").trim());
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int darkenRgb(int colour, float factor) {
        int r = Math.round(((colour >> 16) & 0xFF) * factor);
        int g = Math.round(((colour >> 8) & 0xFF) * factor);
        int b = Math.round((colour & 0xFF) * factor);
        return argb(255, r, g, b);
    }

    public static void drawBoldText(GuiGraphics graphics, Minecraft mc, String text, int x, int y, int color) {
        Component component = Component.literal(text).withStyle(Style.EMPTY.withBold(true));
        graphics.drawString(mc.font, component, x, y, color, true);
    }
}
