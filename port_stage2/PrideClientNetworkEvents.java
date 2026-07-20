package com.natsu.pride.network;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/** Reset du pilotage à la déconnexion : on ne garde jamais un état PILOTED d'un serveur quitté. */
@EventBusSubscriber(modid = Pride.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PrideClientNetworkEvents {

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		PrideFeature.clearPiloted();
	}
}
