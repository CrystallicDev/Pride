package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.natsu.pride.config.ServerConfig;

import net.minecraft.world.item.BowItem;

// pas de dispersion des flèches (1.8.9)
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
