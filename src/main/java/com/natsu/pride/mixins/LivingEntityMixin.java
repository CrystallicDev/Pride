package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natsu.pride.config.ServerConfig;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * - shieldsOnlyBlockProjectiles : les boucliers ne bloquent que les projectiles.
 * - changeBodyRender : rotation du corps façon 1.8.9. L'algorithme vanilla
 *   (tickHeadTurn) est identique entre 1.8.9 et 1.18.2 (vérifié contre MCP-919) :
 *   ce qui change ici, ce sont les seuils — le corps suit la tête dès 20° d'écart
 *   (au lieu de 50°) et l'écart max tête-corps passe de 75° à 45°, ce qui donne le
 *   ressenti "le corps tourne avec la tête" des anciennes versions. On retire aussi
 *   le flip du corps en marche arrière ajouté en 1.9+.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
	private void onlyBlockProjectiles(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (ServerConfig.loaded() && ServerConfig.SHIELDS_ONLY_BLOCK_PROJECTILES.get()
				&& !source.isProjectile()) {
			cir.setReturnValue(false);
		}
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

	// en 1.8.9 le corps s'oriente vers la direction du déplacement même en marche
	// arrière ; la 1.9+ le retourne (check 95°-265°). On neutralise ce check.
	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 95.0F), require = 0)
	private float removeBackwardsBodyFlip(float value) {
		if (ServerConfig.loaded() && ServerConfig.CHANGE_BODY_RENDER.get()
				&& (Object) this instanceof Player) {
			return Float.MAX_VALUE;
		}
		return value;
	}

}
