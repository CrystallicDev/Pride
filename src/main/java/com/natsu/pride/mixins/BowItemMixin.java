package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.natsu.pride.config.ServerConfig;

import net.minecraft.world.item.BowItem;

/**
 * En 1.8.9 les flèches n'avaient aucune dispersion : on supprime l'inaccuracy
 * passée par l'arc lors du tir (1.0F en vanilla 1.9+).
 */
@Mixin(BowItem.class)
public class BowItemMixin {

	@ModifyArg(method = "releaseUsing",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"),
			index = 5)
	private float removeArrowDispersion(float inaccuracy) {
		if (ServerConfig.loaded() && ServerConfig.REMOVE_ARROW_DISPERSION.get()) {
			return 0.0F;
		}
		return inaccuracy;
	}

}
