package com.natsu.pride.events;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;

/**
 * Sweep attacks don't exist in 1.8.9, and since Pride strips the attack cooldown, vanilla's
 * sweep condition ({@code attackStrengthScale > 0.9}) is almost always true, so the sweep
 * would otherwise fire on every single hit. We shut it off through NeoForge's dedicated
 * SweepAttackEvent instead.
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
