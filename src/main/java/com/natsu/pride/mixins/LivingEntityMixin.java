package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.natsu.pride.config.ServerConfig;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
	private void onlyBlockProjectiles(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (ServerConfig.loaded() && ServerConfig.SHIELDS_ONLY_BLOCK_PROJECTILES.get()
				&& !source.isProjectile()) {
			cir.setReturnValue(false);
		}
	}

	// 1.8.9 : vitesse de nage constante, pas de boost de sprint quand submergé
	@ModifyExpressionValue(method = "travel",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z", ordinal = 0))
	private boolean flatWaterSpeed(boolean original) {
		if (ServerConfig.loaded() && ServerConfig.DISABLE_SWIMMING.get()) return false;
		return original;
	}

	@Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
	private void oldSchoolBodyRotation(float targetYaw, float dist, CallbackInfoReturnable<Float> cir) {
		if (!ServerConfig.loaded() || !ServerConfig.CHANGE_BODY_RENDER.get()) return;
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Player)) return;

		float f = Mth.wrapDegrees(targetYaw - self.yBodyRot);
		self.yBodyRot += f * 0.3F;
		float f1 = Mth.wrapDegrees(self.getYRot() - self.yBodyRot);
		boolean backwards = f1 < -90.0F || f1 >= 90.0F;
		f1 = Mth.clamp(f1, -45.0F, 45.0F);
		self.yBodyRot = self.getYRot() - f1;
		if (f1 * f1 > 400.0F) {
			self.yBodyRot += f1 * 0.2F;
		}
		cir.setReturnValue(backwards ? -dist : dist);
	}

	// retire le retournement du corps en marche arrière (ajouté en 1.9)
	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 95.0F), require = 0)
	private float removeBackwardsBodyFlip(float value) {
		if (ServerConfig.loaded() && ServerConfig.CHANGE_BODY_RENDER.get()
				&& (Object) this instanceof Player) {
			return Float.MAX_VALUE;
		}
		return value;
	}

}
