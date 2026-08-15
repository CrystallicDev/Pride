package com.natsu.pride.network;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

// default bus = GAME (the NeoForge event bus), which is right for ClientPlayerNetworkEvent.
/** Reset piloting on disconnect: we never keep a PILOTED state from a server we left. */
@EventBusSubscriber(modid = Pride.MODID, value = Dist.CLIENT)
public class PrideClientNetworkEvents {

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		PrideFeature.clearPiloted();
	}
}
