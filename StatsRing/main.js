/// <reference types="../CTAutocomplete" />
/// <reference lib="es2015" />

/**
 * StatsRing
 * @author Lucas Bubner, 2023
 */

import Settings from "./config.js";

register("command", () => {
    Settings.openGUI();
}).setName("ring");

let hp = NaN,
    maxHp = NaN,
    mana = NaN,
    maxMana = NaN;
let manaLock = false;
let hpScale = 0,
    manaScale = 0;
let ticks = 0,
    fourHundredMilliCycle = false;

register("actionbar", (e) => {
    // Only run if the player is in SkyBlock and has the plugin enabled
    if (!Settings.active || !ChatLib.removeFormatting(Scoreboard.getTitle()).includes("SKYBLOCK")) return;

    // Get the action bar message
    const msg = ChatLib.removeFormatting(e.message.getText());

    // Extract health information
    if (msg.includes("❤")) {
        const hpStats = msg.split("❤")[0];
        hp = parseStat(hpStats.split("/")[0]);
        maxHp = parseStat(hpStats.split("/")[1]);
    }

    // Extract direct mana information
    if (msg.includes("✎")) {
        manaLock = false;
        // Find mana information on action bar
        const manaStats = msg.split("✎")[0];
        // Find RHS of action bar, which is where the info will be
        const p1 = manaStats.split("/");
        maxMana = parseStat(p1[p1.length - 1]);
        // Mana is seperated by curr/max
        const p2 = p1[p1.length - 2].split(" ");
        mana = parseStat(p2[p2.length - 1]);
    } else if (msg.includes("-") && msg.includes("Mana (") && !manaLock) {
        // Extract delta mana information
        const p1 = msg.split("-");
        const sub = p1[p1.length - 1].split(" ")[0];
        mana -= parseStat(sub);
        // Only run extraction once
        manaLock = true;
    }
});

// Replace commas with nothing and parse as int
function parseStat(stat) {
    return parseInt(stat.replace(/,/g, ""));
}

function lerp(start, end, t) {
    return start + (end - start) * t;
}

function darkenRgb(colour, factor) {
    return Renderer.color(
        Math.round(((colour >> 16) & 0xff) * factor),
        Math.round(((colour >> 8) & 0xff) * factor),
        Math.round((colour & 0xff) * factor),
        255
    );
}

const ring = new Image("ring-2.png", "https://raw.githubusercontent.com/bubner/StatsRing/main/assets/ring-2.png");
const hpPercent = new Text("", 0, 0).setColor(Renderer.RED);
const manaPercent = new Text("", 0, 0).setColor(Renderer.AQUA);

const hpPercentLowText = new Text("§l!!!", 0, 0).setColor(Renderer.RED).setShadow(true);
const manaPercentLowText = new Text("§l!!!", 0, 0).setColor(Renderer.AQUA).setShadow(true);

const heightScale = 21;

hpPercent.setScale(0.75);
manaPercent.setScale(0.75);
hpPercent.setShadow(true);
manaPercent.setShadow(true);

register("worldUnload", () => {
    hp = NaN;
    maxHp = NaN;
    mana = NaN;
    maxMana = NaN;
    manaLock = false;
    hpScale = 0;
    manaScale = 0;
    ticks = 0;
    fourHundredMilliCycle = false;
});

register("tick", () => {
    ticks++;
    if (ticks >= 20 * 0.4) {
        // 20 ticks/sec
        fourHundredMilliCycle = !fourHundredMilliCycle;
        ticks = 0;
    }
});

register("renderOverlay", () => {
    // Don't render if values cannot be calculated
    const valuesAreNaN = isNaN(hp) || isNaN(maxHp) || isNaN(mana) || isNaN(maxMana);
    if (!Settings.active || valuesAreNaN || !ChatLib.removeFormatting(Scoreboard.getTitle()).includes("SKYBLOCK")) return;

    // Calculate center of screen
    const xCenter = Renderer.screen.getWidth() / 2;
    const yCenter = Renderer.screen.getHeight() / 2;

    // Draw ring
    if (Settings.backingImage) {
        const ringSize = 35;
        Renderer.drawImage(ring, xCenter - ringSize / 2, yCenter - ringSize / 2, ringSize, ringSize);
    }

    // Health calculations
    let healthPercent = (hp / maxHp) * 100;
    if (!Settings.absorption) healthPercent = Math.min(100, healthPercent);
    let hpColour =
        healthPercent > 100
            ? Renderer.color(255, 217, 0, 255) // Absorption
            : Renderer.color(
                  Settings.interpolateColour ? Math.round(lerp(139, 255, healthPercent / 100)) : 255, // r
                  0, // g
                  0, // b
                  255 // a
              );
    // Health bar
    const currentHpScale = Math.min(heightScale, heightScale * (hp / maxHp));
    hpScale = Settings.interpolateBars ? lerp(hpScale, currentHpScale, 0.1) : currentHpScale;
    const lowHpPercent = parseFloat(Settings.alertLowHpPercent);
    if (!isNaN(lowHpPercent) && healthPercent <= lowHpPercent && fourHundredMilliCycle) {
        hpPercentLowText.setX(xCenter - 13.5);
        hpPercentLowText.setY(yCenter - 21.5);
        hpPercentLowText.draw();
        hpColour = Settings.interpolateColour ? Renderer.RED : Renderer.WHITE;
        Renderer.drawRect(darkenRgb(hpColour, 0.25), xCenter - 11, yCenter + 10 - heightScale, 2.5, heightScale - hpScale);
    }
    Renderer.drawRect(hpColour, xCenter - 11, yCenter + 10 - hpScale, 2.5, hpScale);

    // Mana bar
    const manaPercentage = Math.min(100, (mana / maxMana) * 100);
    let manaColour = Renderer.color(
        Settings.interpolateColour ? Math.round(lerp(200, 0, manaPercentage / 100)) : 0, // r
        Settings.interpolateColour ? Math.round(lerp(100, 255, manaPercentage / 100)) : 255, // g
        Settings.interpolateColour ? Math.round(lerp(255, 255, manaPercentage / 100)) : 255, // b
        255 // a
    );
    const currentManaScale = Math.min(heightScale, heightScale * (mana / maxMana));
    manaScale = Settings.interpolateBars ? lerp(manaScale, currentManaScale, 0.1) : currentManaScale;
    const lowManaPercent = parseFloat(Settings.alertLowManaPercent);
    if (!isNaN(lowManaPercent) && manaPercentage <= lowManaPercent && !fourHundredMilliCycle) {
        manaPercentLowText.setX(xCenter + 6.5);
        manaPercentLowText.setY(yCenter + 12.5);
        manaPercentLowText.draw();
        manaColour = Settings.interpolateColour ? Renderer.AQUA : Renderer.WHITE;
        Renderer.drawRect(darkenRgb(manaColour, 0.25), xCenter + 9, yCenter + 10 - heightScale, 2.5, heightScale - manaScale);
    }
    Renderer.drawRect(manaColour, xCenter + 9, yCenter + 10 - manaScale, 2.5, manaScale);

    if (!Settings.percentage) return;

    // Draw HP percentages
    hpPercent.setColor(hpColour);
    const roundedHpPercent = Math.round(healthPercent);
    hpPercent.setString(roundedHpPercent + "%");
    hpPercent.setX(roundedHpPercent >= 100 ? xCenter - 32 : xCenter - 28);
    hpPercent.setY(yCenter - hpPercent.getHeight() / 2);
    hpPercent.draw();

    // Draw mana percentages
    manaPercent.setColor(manaColour);
    manaPercent.setString(`${Math.round(manaPercentage)}%`);
    manaPercent.setX(xCenter + 15);
    manaPercent.setY(yCenter - manaPercent.getHeight() / 2);
    manaPercent.draw();
});
