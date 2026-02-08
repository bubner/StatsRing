package me.bubner.statsring;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix3x2fStack;

import static me.bubner.statsring.Util.*;

/**
 * Port of main.js from the ChatTriggers StatsRing module.
 * Handles action bar parsing for HP/Mana and renders the ring overlay on the HUD.
 *
 * @author Lucas Bubner, 2023 (Original CT module)
 */
public class StatsRingRenderer implements HudElement {
    private static final ResourceLocation RING_TEXTURE = ResourceLocation.fromNamespaceAndPath("statsring", "ring-2.png");

    private static final int HEIGHT_SCALE = 21;
    private static final int RING_SIZE = 35;
    private static final int BAR_WIDTH = 3;

    private static final int COLOR_RED = 0xFFFF0000;
    private static final int COLOR_AQUA = 0xFF00FFFF;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GRAY = 0xFF808080;

    private float hp = Float.NaN;
    private float maxHp = Float.NaN;
    private float mana = Float.NaN;
    private float maxMana = Float.NaN;

    private ManaReadStatus manaReadStatus = ManaReadStatus.OK;
    private float secInterval = 0.4f;

    private float hpScale = 0;
    private float manaScale = 0;

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
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                ResourceLocation.fromNamespaceAndPath("statsring", "ring"),
                this
        );
    }

    private void onGameMessage(Component message, boolean overlay) {
        if (!overlay) return;
        if (!config.getActive() || !isInSkyBlock()) return;

        String msg = message.getString();

        // Extract health information
        if (msg.contains("❤")) {
            String hpStats = msg.split("❤")[0];
            String[] hpParts = hpStats.split("/");
            if (hpParts.length >= 2) {
                hp = parseStat(hpParts[0]);
                maxHp = parseStat(hpParts[1]);
            }
        }

        // Extract mana information
        if (msg.contains("✎")) {
            manaReadStatus = ManaReadStatus.OK;
            String manaStats = msg.split("✎")[0];
            String[] p1 = manaStats.split("/");
            if (p1.length >= 2) {
                maxMana = parseStat(p1[p1.length - 1]);
                String[] p2 = p1[p1.length - 2].split(" ");
                mana = parseStat(p2[p2.length - 1]);
            }
        } else {
            manaReadStatus = msg.contains("NOT ENOUGH MANA") ? ManaReadStatus.NOT_ENOUGH_MANA : ManaReadStatus.FROZEN;
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

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        boolean valuesAreNaN = Float.isNaN(hp) || Float.isNaN(maxHp) || Float.isNaN(mana) || Float.isNaN(maxMana);
        if (!config.getActive() || valuesAreNaN || !isInSkyBlock()) return;

        Minecraft mc = Minecraft.getInstance();
        int xCenter = mc.getWindow().getGuiScaledWidth() / 2;
        int yCenter = mc.getWindow().getGuiScaledHeight() / 2;

        // Draw backing ring image
        if (config.getBackingImage()) {
            int ringX = xCenter - RING_SIZE / 2;
            int ringY = yCenter - RING_SIZE / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, RING_TEXTURE, ringX, ringY, 0, 0, RING_SIZE, RING_SIZE, RING_SIZE, RING_SIZE);
        }

        // === Health bar ===
        float healthPercent = (hp / maxHp) * 100f;
        if (!config.getAbsorption()) healthPercent = Math.min(100f, healthPercent);

        int hpColour;
        if (healthPercent > 100f) {
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
            drawBoldText(graphics, mc, "!!!", xCenter - 14, yCenter - 22, hpColour);
            hpColour = config.getInterpolateColour() ? COLOR_RED : COLOR_WHITE;
            graphics.fill(xCenter - 11, yCenter + 10 - HEIGHT_SCALE, xCenter - 11 + BAR_WIDTH, yCenter + 10 - hpBarHeight, darkenRgb(hpColour, 0.25f));
        }

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
        secInterval = manaReadStatus == ManaReadStatus.NOT_ENOUGH_MANA ? 0.2f : 0.4f;

        if (manaReadStatus == ManaReadStatus.FROZEN) {
            graphics.fill(xCenter + 9, yCenter + 10 - HEIGHT_SCALE, xCenter + 9 + BAR_WIDTH, yCenter + 10 - manaBarHeight, darkenRgb(COLOR_GRAY, 0.25f));
        } else if (((lowManaPercent >= 0 && manaPercentage <= lowManaPercent) || manaReadStatus == ManaReadStatus.NOT_ENOUGH_MANA) && !cycle) {
            int foreColour = manaReadStatus == ManaReadStatus.NOT_ENOUGH_MANA ? COLOR_RED : (config.getInterpolateColour() ? COLOR_AQUA : COLOR_WHITE);
            drawBoldText(graphics, mc, "!!!", xCenter + 7, yCenter + 13, foreColour);
            manaColour = foreColour;
            graphics.fill(xCenter + 9, yCenter + 10 - HEIGHT_SCALE, xCenter + 9 + BAR_WIDTH, yCenter + 10 - manaBarHeight, darkenRgb(manaColour, 0.25f));
        }

        int manaBarColour = manaReadStatus != ManaReadStatus.FROZEN ? manaColour : COLOR_GRAY;
        graphics.fill(xCenter + 9, yCenter + 10 - manaBarHeight, xCenter + 9 + BAR_WIDTH, yCenter + 10, manaBarColour);

        // === Percentages ===
        if (!config.getPercentage()) return;

        float scale = 0.75f;

        // HP percentage
        String hpText = Math.round(healthPercent) + "%";
        int hpTextX = Math.round(healthPercent) >= 100 ? xCenter - 32 : xCenter - 28;
        int hpTextY = yCenter - Math.round(mc.font.lineHeight * scale / 2f);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(hpTextX, hpTextY);
        pose.scale(scale);
        pose.translate(-hpTextX, -hpTextY);
        graphics.drawString(mc.font, hpText, hpTextX, hpTextY, hpColour, true);
        pose.popMatrix();

        // Mana percentage
        int manaTextColour = manaReadStatus != ManaReadStatus.FROZEN ? manaColour : COLOR_GRAY;
        String manaText = Math.round(manaPercentage) + "%";
        int manaTextX = xCenter + 15;
        int manaTextY = yCenter - Math.round(mc.font.lineHeight * scale / 2f);
        pose.pushMatrix();
        pose.translate(manaTextX, manaTextY);
        pose.scale(scale);
        pose.translate(-manaTextX, -manaTextY);
        graphics.drawString(mc.font, manaText, manaTextX, manaTextY, manaTextColour, true);
        pose.popMatrix();
    }
}
