package me.bubner.statsring;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

/**
 * Port of main.js from the ChatTriggers StatsRing module.
 * Handles action bar parsing for HP/Mana and renders the ring overlay on the HUD.
 *
 * @author Lucas Bubner, 2023-2025
 */
public class StatsRingRenderer {
    private static final ResourceLocation RING_TEXTURE = ResourceLocation.fromNamespaceAndPath("statsring", "ring-2.png");

    private static final int HEIGHT_SCALE = 21;
    private static final int RING_SIZE = 35;
    private static final int BAR_WIDTH = 3;

    // Predefined colours (ARGB)
    private static final int COLOR_RED = 0xFFFF0000;
    private static final int COLOR_AQUA = 0xFF00FFFF;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GRAY = 0xFF808080;

    // Parsed stat values
    private float hp = Float.NaN;
    private float maxHp = Float.NaN;
    private float mana = Float.NaN;
    private float maxMana = Float.NaN;

    // 0 = mana read OK, 1 = mana frozen/missing, 2 = NOT ENOUGH MANA
    private int manaReadStatus = 0;
    private float secInterval = 0.4f;

    // Interpolated bar heights
    private float hpScale = 0;
    private float manaScale = 0;

    // Animation cycle
    private int ticks = 0;
    private boolean cycle = false;

    private final ModConfig config;

    public StatsRingRenderer(ModConfig config) {
        this.config = config;
    }

    /**
     * Register all Fabric event listeners.
     */
    public void register() {
        ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetState());
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onGameMessage(Component message, boolean overlay) {
        if (!overlay) return;
        if (!config.getActive() || !isInSkyBlock()) return;

        String msg = message.getString();

        // Extract health information
        if (msg.contains("\u2764")) {
            String hpStats = msg.split("\u2764")[0];
            String[] hpParts = hpStats.split("/");
            if (hpParts.length >= 2) {
                hp = parseStat(hpParts[0]);
                maxHp = parseStat(hpParts[1]);
            }
        }

        // Extract mana information
        if (msg.contains("\u270E")) {
            manaReadStatus = 0;
            String manaStats = msg.split("\u270E")[0];
            String[] p1 = manaStats.split("/");
            if (p1.length >= 2) {
                maxMana = parseStat(p1[p1.length - 1]);
                String[] p2 = p1[p1.length - 2].split(" ");
                mana = parseStat(p2[p2.length - 1]);
            }
        } else {
            manaReadStatus = msg.contains("NOT ENOUGH MANA") ? 2 : 1;
        }
    }

    private void resetState() {
        hp = Float.NaN;
        maxHp = Float.NaN;
        mana = Float.NaN;
        maxMana = Float.NaN;
        hpScale = 0;
        manaScale = 0;
        ticks = 0;
        cycle = false;
    }

    private void onTick() {
        ticks++;
        if (ticks >= (int) (20 * secInterval)) {
            cycle = !cycle;
            ticks = 0;
        }
    }

    // HudRenderCallback is deprecated in favour of HudElementRegistry, but still functional
    @SuppressWarnings("deprecation")
    private void onHudRender(GuiGraphics graphics, DeltaTracker deltaTracker) {
        boolean valuesAreNaN = Float.isNaN(hp) || Float.isNaN(maxHp) || Float.isNaN(mana) || Float.isNaN(maxMana);
        if (!config.getActive() || valuesAreNaN || !isInSkyBlock()) return;

        Minecraft mc = Minecraft.getInstance();
        int xCenter = mc.getWindow().getGuiScaledWidth() / 2;
        int yCenter = mc.getWindow().getGuiScaledHeight() / 2;

        // Draw backing ring image
        if (config.getBackingImage()) {
            int ringX = xCenter - RING_SIZE / 2;
            int ringY = yCenter - RING_SIZE / 2;
            graphics.blit(RenderType::guiTextured, RING_TEXTURE, ringX, ringY, 0, 0, RING_SIZE, RING_SIZE, RING_SIZE, RING_SIZE);
        }

        // === Health bar ===
        float healthPercent = (hp / maxHp) * 100f;
        if (!config.getAbsorption()) healthPercent = Math.min(100f, healthPercent);

        int hpColour;
        if (healthPercent > 100f) {
            // Absorption - gold
            hpColour = argb(255, 255, 217, 0);
        } else {
            int r = config.getInterpolateColour() ? Math.round(lerp(139, 255, healthPercent / 100f)) : 255;
            hpColour = argb(255, r, 0, 0);
        }

        float currentHpScale = Math.min(HEIGHT_SCALE, HEIGHT_SCALE * (hp / maxHp));
        hpScale = config.getInterpolateBars() ? lerp(hpScale, currentHpScale, 0.1f) : currentHpScale;
        int hpBarHeight = Math.round(hpScale);

        float lowHpPercent = config.getAlertLowHpPercent();
        if (lowHpPercent >= 0 && healthPercent <= lowHpPercent && cycle) {
            // Flash "!!!" above HP bar
            drawBoldText(graphics, mc, "!!!", xCenter - 14, yCenter - 22, hpColour);
            hpColour = config.getInterpolateColour() ? COLOR_RED : COLOR_WHITE;
            // Draw dark background for empty portion
            graphics.fill(xCenter - 11, yCenter + 10 - HEIGHT_SCALE, xCenter - 11 + BAR_WIDTH, yCenter + 10 - hpBarHeight, darkenRgb(hpColour, 0.25f));
        }

        // Draw HP bar
        graphics.fill(xCenter - 11, yCenter + 10 - hpBarHeight, xCenter - 11 + BAR_WIDTH, yCenter + 10, hpColour);

        // === Mana bar ===
        float manaPercentage = Math.min(100f, (mana / maxMana) * 100f);

        int manaColour;
        if (config.getInterpolateColour()) {
            int r = Math.round(lerp(200, 0, manaPercentage / 100f));
            int g = Math.round(lerp(100, 255, manaPercentage / 100f));
            manaColour = argb(255, r, g, 255);
        } else {
            manaColour = COLOR_AQUA;
        }

        float currentManaScale = Math.min(HEIGHT_SCALE, HEIGHT_SCALE * (mana / maxMana));
        manaScale = config.getInterpolateBars() ? lerp(manaScale, currentManaScale, 0.1f) : currentManaScale;
        int manaBarHeight = Math.round(manaScale);

        float lowManaPercent = config.getAlertLowManaPercent();
        secInterval = manaReadStatus == 2 ? 0.2f : 0.4f;

        if (manaReadStatus == 1) {
            // Mana frozen - grey background
            graphics.fill(xCenter + 9, yCenter + 10 - HEIGHT_SCALE, xCenter + 9 + BAR_WIDTH, yCenter + 10 - manaBarHeight, darkenRgb(COLOR_GRAY, 0.25f));
        } else if (((lowManaPercent >= 0 && manaPercentage <= lowManaPercent) || manaReadStatus == 2) && !cycle) {
            // Flash "!!!" below mana bar
            int foreColour = manaReadStatus == 2 ? COLOR_RED : (config.getInterpolateColour() ? COLOR_AQUA : COLOR_WHITE);
            drawBoldText(graphics, mc, "!!!", xCenter + 7, yCenter + 13, foreColour);
            manaColour = foreColour;
            // Draw dark background
            graphics.fill(xCenter + 9, yCenter + 10 - HEIGHT_SCALE, xCenter + 9 + BAR_WIDTH, yCenter + 10 - manaBarHeight, darkenRgb(manaColour, 0.25f));
        }

        // Draw mana bar
        int manaBarColour = manaReadStatus != 1 ? manaColour : COLOR_GRAY;
        graphics.fill(xCenter + 9, yCenter + 10 - manaBarHeight, xCenter + 9 + BAR_WIDTH, yCenter + 10, manaBarColour);

        // === Percentages ===
        if (!config.getPercentage()) return;

        float scale = 0.75f;

        // HP percentage
        String hpText = Math.round(healthPercent) + "%";
        int hpTextX = Math.round(healthPercent) >= 100 ? xCenter - 32 : xCenter - 28;
        int hpTextY = yCenter - Math.round(mc.font.lineHeight * scale / 2f);
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, hpText, Math.round(hpTextX / scale), Math.round(hpTextY / scale), hpColour, true);
        graphics.pose().popPose();

        // Mana percentage
        int manaTextColour = manaReadStatus != 1 ? manaColour : COLOR_GRAY;
        String manaText = Math.round(manaPercentage) + "%";
        int manaTextX = xCenter + 15;
        int manaTextY = yCenter - Math.round(mc.font.lineHeight * scale / 2f);
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, manaText, Math.round(manaTextX / scale), Math.round(manaTextY / scale), manaTextColour, true);
        graphics.pose().popPose();
    }

    // --- Utility methods ---

    private static boolean isInSkyBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return false;
        return objective.getDisplayName().getString().contains("SKYBLOCK");
    }

    private static float parseStat(String stat) {
        try {
            return Integer.parseInt(stat.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int darkenRgb(int colour, float factor) {
        int r = Math.round(((colour >> 16) & 0xFF) * factor);
        int g = Math.round(((colour >> 8) & 0xFF) * factor);
        int b = Math.round((colour & 0xFF) * factor);
        return argb(255, r, g, b);
    }

    private static void drawBoldText(GuiGraphics graphics, Minecraft mc, String text, int x, int y, int color) {
        Component component = Component.literal(text).withStyle(Style.EMPTY.withBold(true));
        graphics.drawString(mc.font, component, x, y, color, true);
    }
}
