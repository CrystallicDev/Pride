package com.natsu.pride;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.natsu.pride.config.ServerConfig;
import com.natsu.pride.network.PrideNetwork;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(Pride.MODID)
public class Pride {
	public static final String MODID = "pride";

    public Pride() {
    	ModLoadingContext.get().registerConfig(Type.SERVER, ServerConfig.SPEC);
    	// connecting to servers without Pride (vanilla, Paper, Forge without the mod) is allowed
    	// via displayTest="IGNORE_ALL_VERSION" in mods.toml; the network channel is optional.
    	PrideNetwork.register();
    	MixinExtrasBootstrap.init();
    }

}
