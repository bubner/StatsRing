import { @Vigilant, @SwitchProperty } from "../Vigilance/index.js";

@Vigilant("StatsRing", "§lStatsRing Health and Mana ring")

class Settings {
    constructor() {
        this.initialize(this);
    }

    @SwitchProperty({
        name: "Toggle",
        description: "Toggle ring on/off",
        category: "Settings"
    })
    active = true;

    @SwitchProperty({
        name: "Percentages",
        description: "Render percentages of health/mana next to ring",
        category: "Settings"
    })
    percentage = false;
}

export default new Settings;
