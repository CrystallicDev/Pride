package com.natsu.pride.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

@Mixin(Projectile.class)
public class ProjectileMixin {

	// 1.8.9: arrows don't inherit the shooter's velocity (vanilla adds it after
	// aiming the arrow in shootFromRotation).
	@WrapOperation(method = "shootFromRotation",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 noVelocityInherit(Vec3 arrowDelta, double x, double y, double z, Operation<Vec3> original) {
		if (PrideFeature.REVERT_BOW.enabled() && (Object) this instanceof AbstractArrow) {
			return arrowDelta;
		}
		return original.call(arrowDelta, x, y, z);
	}

	// 1.8.9: an arrow can hit its own shooter after a short immunity (5 ticks), instead
	// of waiting until it leaves the 1-block inflated box -> this is what enables bow boosting.
	@ModifyExpressionValue(method = "canHitEntity",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;leftOwner:Z", opcode = Opcodes.GETFIELD))
	private boolean allowSelfHitAfterDelay(boolean leftOwner) {
		if (leftOwner) return true;
		if (!PrideFeature.REVERT_BOW.enabled()) return false;
		Projectile self = (Projectile) (Object) this;
		return self instanceof AbstractArrow && self.tickCount >= 5;
	}
}
