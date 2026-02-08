package me.bubner.statsring;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StatsRing - Display Health and Mana in a ring around the crosshair on Hypixel SkyBlock.
 * Ported from a 1.8.9 ChatTriggers module into a 1.21.10 Fabric mod.
 *
 * @author Lucas Bubner, 2023 (Original CT module)
 */
public class StatsRing implements ClientModInitializer {
	public static final String MOD_ID = "statsring";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		final ModConfig config = new ModConfig();
		config.load();

		new StatsRingRenderer(config).register();

		// Register /ring command to open the settings GUI
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("ring").executes(context -> {
				Minecraft client = Minecraft.getInstance();
				client.execute(() -> client.setScreen(ConfigScreen.create(null, config)));
				return 1;
			}))
		);
	}
}