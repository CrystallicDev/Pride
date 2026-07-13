package com.natsu.pride.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.config.ServerConfig;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

@Mixin(Projectile.class)
public class ProjectileMixin {

	// 1.8.9 : les flèches n'héritent pas de la vélocité du tireur (le vanilla l'ajoute
	// après avoir orienté la flèche dans shootFromRotation).
	@WrapOperation(method = "shootFromRotation",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 noVelocityInherit(Vec3 arrowDelta, double x, double y, double z, Operation<Vec3> original) {
		if (ServerConfig.loaded() && ServerConfig.REVERT_BOW.get() && (Object) this instanceof AbstractArrow) {
			return arrowDelta;
		}
		return original.call(arrowDelta, x, y, z);
	}

	// 1.8.9 : une flèche peut toucher son propre tireur après une courte immunité (5 ticks),
	// au lieu d'attendre qu'elle quitte sa boîte gonflée de 1 bloc -> permet le bow boost.
	@ModifyExpressionValue(method = "canHitEntity",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/Projectile;leftOwner:Z", opcode = Opcodes.GETFIELD))
	private boolean allowSelfHitAfterDelay(boolean leftOwner) {
		if (leftOwner) return true;
		if (!ServerConfig.loaded() || !ServerConfig.REVERT_BOW.get()) return false;
		Projectile self = (Projectile) (Object) this;
		return self instanceof AbstractArrow && self.tickCount >= 5;
	}
}
