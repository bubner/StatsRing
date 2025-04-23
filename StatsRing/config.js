import { @Vigilant, @SwitchProperty, @TextProperty } from "../Vigilance/index.js";

@Vigilant("StatsRing", "§lStats Ring: §r§cHealth §rand §bMana §rring")

class Settings {
    constructor() {
        this.initialize(this);
    }

    @SwitchProperty({
        name: "Enable ring",
        description: "Render the ring (on/off).",
        category: "Core"
    })
    active = true;

    @SwitchProperty({
        name: "Show percentages",
        description: "Render percentages of health/mana next to ring (on/off).",
        category: "Core"
    })
    percentage = true;

    @SwitchProperty({
        name: "Show absorption",
        description: "Show over 100%% health for absorption hearts (on/off).",
        category: "Core"
    })
    absorption = true;

    @SwitchProperty({
        name: "Show backing image",
        description: "Show the background image for the ring that surround the bars (on/off).",
        category: "Display"
    })
    backingImage = true;

    @SwitchProperty({
        name: "Interpolate colour",
        description: "Linear interpolates mana and health colour depending on percentage filled (on/off).",
        category: "Display"
    })
    interpolateColour = true;

    @SwitchProperty({
        name: "Interpolate bars",
        description: "Linear interpolates the filled progress of the bars (on/off).",
        category: "Display"
    })
    interpolateBars = true;

    @TextProperty({
        name: "Low HP percent alert threshold",
        description: "Visually flashes HP when equal to or below this percentage (%% from 0 to 100, -1 to disable)",
        category: "Visual Warnings",
    })
    alertLowHpPercent = "40";

    @TextProperty({
        name: "Low Mana percent alert threshold",
        description: "Visually flashes Mana when equal to or below this percentage (%% from 0 to 100, -1 to disable)",
        category: "Visual Warnings",
    })
    alertLowManaPercent = "20";
}

export default new Settings;
