package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.item.BowItem;

// pas de dispersion des flèches (1.8.9)
// 1.21 : le tir est passé de BowItem.releaseUsing à ProjectileWeaponItem#shootProjectile (surchargé
// par BowItem), qui appelle Projectile.shootFromRotation (plus AbstractArrow). On modifie l'inaccuracy là.
@Mixin(value = BowItem.class, remap = false)
public class BowItemMixin {

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
