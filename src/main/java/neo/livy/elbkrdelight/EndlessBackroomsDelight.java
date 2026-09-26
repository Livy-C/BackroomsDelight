package neo.livy.elbkrdelight;

import neo.livy.elbkrdelight.item.ModItems;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndlessBackroomsDelight implements ModInitializer {
	public static final String MOD_ID = "endless_backrooms_delight";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.initialize();

		LOGGER.info("Initializing {}", MOD_ID);
	}

	/**
	 * Creates a {@link ResourceLocation} in this mod's namespace.
	 *
	 * @param path the resource path, without a namespace prefix
	 * @return the namespaced resource location
	 */
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
