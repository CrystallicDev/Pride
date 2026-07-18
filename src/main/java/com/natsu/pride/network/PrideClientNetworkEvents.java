package com.natsu.pride.network;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Reset du pilotage à la déconnexion : on ne garde jamais un état PILOTED d'un serveur quitté. */
@Mod.EventBusSubscriber(modid = Pride.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PrideClientNetworkEvents {

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		PrideFeature.clearPiloted();
	}
}
