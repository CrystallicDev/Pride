package com.natsu.pride.events;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;

/**
 * 1.8.9 : les attaques de sweep n'existent pas. Le rewrite complet de {@code Player.attack} (qui
 * gérait ça) est différé (port_stage2) ; en attendant, comme Pride retire le cooldown d'attaque,
 * la condition de sweep vanilla ({@code attackStrengthScale > 0.9}) est presque toujours vraie et le
 * sweep se déclenche à chaque coup. On le neutralise via l'event NeoForge dédié.
 */
@EventBusSubscriber(modid = Pride.MODID)
public class PrideCombatEvents {

	@SubscribeEvent
	public static void onSweep(SweepAttackEvent event) {
		if (PrideFeature.DISABLE_SWEEPING_ATTACKS.enabled()) {
			event.setSweeping(false);
		}
	}
}
