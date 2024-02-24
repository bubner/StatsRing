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

// Get player information from the action bar
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

// Ring image
const ring = new Image("ring.png", "https://raw.githubusercontent.com/bubner/StatsRing/main/assets/ring.png");

// Percentage text boxes
const hpPercent = new Text("", 0, 0).setColor(Renderer.RED);
const manaPercent = new Text("", 0, 0).setColor(Renderer.AQUA);

hpPercent.setScale(0.75);
manaPercent.setScale(0.75);
hpPercent.setShadow(true);
manaPercent.setShadow(true);

// Render stats as the dual ring
register("renderOverlay", () => {
    // Don't render if values cannot be calculated
    const valuesAreNaN = isNaN(hp) || isNaN(maxHp) || isNaN(mana) || isNaN(maxMana);
    if (!Settings.active || valuesAreNaN || !ChatLib.removeFormatting(Scoreboard.getTitle()).includes("SKYBLOCK"))
        return;

    // Calculate center of screen
    const xCenter = Renderer.screen.getWidth() / 2;
    const yCenter = Renderer.screen.getHeight() / 2;

    // Draw ring
    const ringSize = 35;
    Renderer.drawImage(ring, xCenter - ringSize / 2, yCenter - ringSize / 2, ringSize, ringSize);

    // Health calculations
    const percentage = Math.round((hp / maxHp) * 100);
    const hpColour = Renderer.color(
        255, // r
        percentage > 100 ? 217 : 0, // g
        0, // b
        255 // a
    );

    // Health bar
    Renderer.drawRect(
        hpColour,
        xCenter - 10,
        yCenter + 8 - Math.min(15, 15 * (hp / maxHp)),
        1.5,
        Math.min(15, 15 * (hp / maxHp))
    );

    // Mana bar
    Renderer.drawRect(
        Renderer.AQUA,
        xCenter + 10,
        yCenter + 8 - Math.min(15, 15 * (mana / maxMana)),
        1.5,
        Math.min(15, 15 * (mana / maxMana))
    );

    if (!Settings.percentage) return;

    // Draw HP percentages
    hpPercent.setColor(hpColour);
    hpPercent.setString(percentage + "%");
    hpPercent.setX(xCenter - 32);
    hpPercent.setY(yCenter - hpPercent.getHeight() / 2);
    hpPercent.draw();

    // Draw mana percentages
    manaPercent.setString(`${Math.min(100, Math.round((mana / maxMana) * 100))}%`);
    manaPercent.setX(xCenter + 15);
    manaPercent.setY(yCenter - manaPercent.getHeight() / 2);
    manaPercent.draw();
});
