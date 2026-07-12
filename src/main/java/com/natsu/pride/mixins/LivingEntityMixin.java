package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natsu.pride.config.ServerConfig;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true)
	private void onlyBlockProjectiles(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (ServerConfig.loaded() && ServerConfig.SHIELDS_ONLY_BLOCK_PROJECTILES.get()
				&& !source.isProjectile()) {
			cir.setReturnValue(false);
		}
	}

	// Remplace intégralement le mouvement dans l'eau par celui de la 1.8.9 :
	// friction 0.8 constante, gravité verticale -0.02, bump de surface, et le
	// terme Depth Strider basé sur getSpeed() (sprint inclus, comme getAIMoveSpeed 1.8.9).
	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void oldSchoolWaterMovement(Vec3 input, CallbackInfo ci) {
		if (!ServerConfig.loaded() || !ServerConfig.DISABLE_SWIMMING.get()) return;
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Player player)) return;
		if (player.getAbilities().flying) return;
		if (!(self.isEffectiveAi() || self.isControlledByLocalInstance())) return;
		FluidState fluid = self.level.getFluidState(self.blockPosition());
		if (!self.isInWater() || self.canStandOnFluid(fluid)) return;

		double startY = self.getY();
		float friction = 0.8F;
		float accel = 0.02F;
		float depthStrider = EnchantmentHelper.getDepthStrider(self);
		if (depthStrider > 3.0F) depthStrider = 3.0F;
		if (!self.isOnGround()) depthStrider *= 0.5F;
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

		self.calculateEntityAnimation(self, self instanceof FlyingAnimal);
		ci.cancel();
	}

	// équivalent du isFree(x,y,z) privé de Entity
	private static boolean isFree(LivingEntity self, double x, double y, double z) {
		AABB box = self.getBoundingBox().move(x, y, z);
		return self.level.noCollision(self, box) && !self.level.containsAnyLiquid(box);
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
