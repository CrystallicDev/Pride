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
    	// La connexion aux serveurs sans Pride (vanilla, Paper, Forge sans le mod) est autorisée
    	// via displayTest="IGNORE_ALL_VERSION" dans mods.toml ; le canal réseau est optionnel.
    	PrideNetwork.register();
    	MixinExtrasBootstrap.init();
    }

}
