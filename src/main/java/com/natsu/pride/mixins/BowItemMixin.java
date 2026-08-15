package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.item.BowItem;

// no arrow spread (1.8.9)
@Mixin(BowItem.class)
public class BowItemMixin {

	@ModifyArg(method = "releaseUsing",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"),
			index = 5)
	private float removeArrowDispersion(float inaccuracy) {
		if (PrideFeature.REMOVE_ARROW_DISPERSION.enabled()) {
			return 0.0F;
		}
		return inaccuracy;
	}

}
