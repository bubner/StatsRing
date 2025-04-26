# StatsRing [ChatTriggers](https://github.com/ChatTriggers/ChatTriggers) module
Display Health and Mana in a ring around the crosshair on Hypixel SkyBlock.

Optionally include percentages, visual alerts on low stats, and smooth animation. Useful for combat across SkyBlock.

![StatsRing](https://github.com/user-attachments/assets/12fdaaeb-db4d-4c34-886c-b4a1e29bb5a0)

To manually install, download the latest [release](https://github.com/bubner/StatsRing/releases/latest) and extract the `StatsRing` folder to `%appdata%/.minecraft/config/ChatTriggers/modules` in its folder on Windows (equivalently `~/.minecraft ...`).
You may also need to `/ct import Vigilance` in-game before use, as it is a dependency.

<i>Open settings menu:</i> /ring

### Known issues
- If holding an item that does not show your current health or mana on your action bar (such as a drill hiding mana), the health or mana (or both) values will be frozen. The solution would be to predict mana and health when unavailable (like how SBA does it). StatsRing v2.1.0 and newer automatically changes the colour of the mana ring to grey to indicate a failure reading the current mana value. Note that if health cannot be read, it will remain frozen like previous versions.
- StatsRing currently does not work in the Rift Dimension and will auto-disable.

###### Copyright (c) 2023 Lucas Bubner
