package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.item.BowItem;

// no arrow spread (1.8.9)
// shooting goes through ProjectileWeaponItem#shootProjectile (overridden by BowItem), which
// calls Projectile.shootFromRotation; we tweak the inaccuracy right there.
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
