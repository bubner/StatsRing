# Stats Ring
Display Health and Mana in a ring around the crosshair on Hypixel SkyBlock.

**New:** Ported from a 1.8.9 ChatTriggers module into a 1.21.10 Fabric mod.

Optionally include percentages, visual alerts on low stats, and smooth animation. Useful for combat across SkyBlock.

![StatsRing](https://github.com/user-attachments/assets/12fdaaeb-db4d-4c34-886c-b4a1e29bb5a0)

<i>Open settings menu:</i> /ring

### Known issues
- If holding an item that does not show your current health or mana on your action bar (such as a drill hiding mana), the health or mana (or both) values will be frozen. The solution would be to predict mana and health when unavailable (like how SBA does it). StatsRing v2.1.0 and newer automatically changes the colour of the mana ring to grey to indicate a failure reading the current mana value. Note that if health cannot be read, it will remain frozen like previous versions.
- StatsRing currently does not work in the Rift Dimension and will auto-disable.

###### Copyright (c) 2023 Lucas Bubner
