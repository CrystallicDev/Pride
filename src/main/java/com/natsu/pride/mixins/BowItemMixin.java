package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.item.BowItem;

// pas de dispersion des flèches (1.8.9)
@Mixin(value = BowItem.class, remap = false)
public class BowItemMixin {

	// 1.20.6 : le tir est passé de releaseUsing à shootProjectile (ProjectileWeaponItem), qui appelle
	// Projectile.shootFromRotation (et non plus AbstractArrow). On modifie l'inaccuracy (arg 5) là.
	@ModifyArg(method = "shootProjectile",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/Projectile;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"),
			index = 5)
	private float removeArrowDispersion(float inaccuracy) {
		if (PrideFeature.REMOVE_ARROW_DISPERSION.enabled()) {
			return 0.0F;
		}
		return inaccuracy;
	}

}
