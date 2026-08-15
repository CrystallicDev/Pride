package com.natsu.pride;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.natsu.pride.config.ServerConfig;
import com.natsu.pride.network.PrideNetwork;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.network.NetworkConstants;

@Mod(Pride.MODID)
public class Pride {
	public static final String MODID = "pride";

    public Pride() {
    	ModLoadingContext.get().registerConfig(Type.SERVER, ServerConfig.SPEC);
    	// lets you connect to servers that don't have Pride (vanilla, Paper, Forge without the mod):
    	// all the features just stay off in that case (see PrideFeature).
    	ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
    			() -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
    	PrideNetwork.register();
    	MixinExtrasBootstrap.init();
    }

}
