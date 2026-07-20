package com.natsu.pride;

import com.natsu.pride.config.ServerConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Pride.MODID)
public class Pride {
	public static final String MODID = "pride";

	// NeoForge fournit Mixin + MixinExtras et les initialise lui-même (pas de bootstrap manuel).
	// Sans payload réseau requis, le mod peut rejoindre des serveurs vanilla/Paper (pas de DisplayTest).
	public Pride(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
	}

}
