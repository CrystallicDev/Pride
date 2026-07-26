package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natsu.pride.features.PrideFeature;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(value = LivingEntity.class, remap = false)
public class LivingEntityMixin {

	@Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
	private void onlyBlockProjectiles(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (PrideFeature.SHIELDS_ONLY_BLOCK_PROJECTILES.enabled()
				&& !source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
			cir.setReturnValue(false);
		}
	}

	// Knockback de base 1.8.9 : identique à la 1.18 sauf le soulèvement vertical, qui s'applique
	// à chaque coup (la 1.18 ne soulève que si la cible est au sol).
	@Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
	private void oldSchoolKnockback(double strength, double x, double z, CallbackInfo ci) {
		if (!PrideFeature.REVERT_KNOCKBACK.enabled()) return;
		LivingEntity self = (LivingEntity) (Object) this;
		LivingKnockBackEvent event = net.neoforged.neoforge.common.CommonHooks.onLivingKnockBack(self, (float) strength, x, z);
		if (event.isCanceled()) {
			ci.cancel();
			return;
		}
		strength = event.getStrength();
		x = event.getRatioX();
		z = event.getRatioZ();
		strength *= 1.0D - self.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		if (strength > 0.0D) {
			self.hasImpulse = true;
			Vec3 current = self.getDeltaMovement();
			Vec3 push = new Vec3(x, 0.0D, z).normalize().scale(strength);
			self.setDeltaMovement(
					current.x / 2.0D - push.x,
					Math.min(0.4D, current.y / 2.0D + strength),
					current.z / 2.0D - push.z);
		}
		ci.cancel();
	}

	// Remplace intégralement le mouvement dans l'eau par celui de la 1.8.9 :
	// friction 0.8 constante, gravité verticale -0.02, bump de surface, et le
	// terme Depth Strider basé sur getSpeed() (sprint inclus, comme getAIMoveSpeed 1.8.9).
	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void oldSchoolWaterMovement(Vec3 input, CallbackInfo ci) {
		if (!PrideFeature.DISABLE_SWIMMING.enabled()) return;
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Player player)) return;
		if (player.getAbilities().flying) return;
		if (!(self.isEffectiveAi() || self.isControlledByLocalInstance())) return;
		FluidState fluid = self.level().getFluidState(self.blockPosition());
		if (!self.isInWater() || self.canStandOnFluid(fluid)) return;

		double startY = self.getY();
		float friction = 0.8F;
		float accel = 0.02F;
		// 1.21 : Depth Strider n'est plus un niveau d'enchant (0-3) mais l'attribut
		// WATER_MOVEMENT_EFFICIENCY (0.0-1.0). On le remet sur l'échelle 0-3 de la formule 1.8.9.
		float depthStrider = (float) self.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) * 3.0F;
		if (depthStrider > 3.0F) depthStrider = 3.0F;
		if (!self.onGround()) depthStrider *= 0.5F;
		if (depthStrider > 0.0F) {
			friction += (0.54600006F - friction) * depthStrider / 3.0F;
			accel += (self.getSpeed() - accel) * depthStrider / 3.0F;
		}

		self.moveRelative(accel, input);
		self.move(MoverType.SELF, self.getDeltaMovement());
		Vec3 motion = self.getDeltaMovement();
		if (self.horizontalCollision && self.onClimbable()) {
			motion = new Vec3(motion.x, 0.2D, motion.z);
		}
		motion = motion.multiply(friction, 0.8D, friction);
		motion = new Vec3(motion.x, motion.y - 0.02D, motion.z);
		if (self.horizontalCollision && isFree(self, motion.x, motion.y + 0.6D - self.getY() + startY, motion.z)) {
			motion = new Vec3(motion.x, 0.3D, motion.z);
		}
		self.setDeltaMovement(motion);

		self.calculateEntityAnimation(self instanceof FlyingAnimal);
		ci.cancel();
	}

	// équivalent du isFree(x,y,z) privé de Entity
	private static boolean isFree(LivingEntity self, double x, double y, double z) {
		AABB box = self.getBoundingBox().move(x, y, z);
		return self.level().noCollision(self, box) && !self.level().containsAnyLiquid(box);
	}

	@Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
	private void oldSchoolBodyRotation(float targetYaw, float dist, CallbackInfoReturnable<Float> cir) {
		if (!PrideFeature.CHANGE_BODY_RENDER.enabled()) return;
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
		if (PrideFeature.CHANGE_BODY_RENDER.enabled()
				&& (Object) this instanceof Player) {
			return Float.MAX_VALUE;
		}
		return value;
	}

}
