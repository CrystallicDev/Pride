package com.natsu.pride;

import com.natsu.pride.config.ServerConfig;
import com.natsu.pride.network.PrideNetwork;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Pride.MODID)
public class Pride {
	public static final String MODID = "pride";

	// NeoForge ships Mixin + MixinExtras and inits them itself, so no manual bootstrap here.
	// the pride:features channel is OPTIONAL -> the mod can still join vanilla/Paper servers.
	public Pride(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
		PrideNetwork.register(modEventBus);
	}

}
