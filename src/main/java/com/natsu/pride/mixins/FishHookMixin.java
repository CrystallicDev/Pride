package com.natsu.pride.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FishingHook.class)
public class FishHookMixin {

	@Inject(method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", at = @At("TAIL"))
	private void onInit(Player p_37106_, Level p_37107_, int p_37108_, int p_37109_, CallbackInfo ci) {
		FishingHook hook = (FishingHook)(Object)this;
		Player player = hook.getOwner() instanceof Player p ? p : null;
		if (player == null) {
			//original.call(hook, x, y, z);
			return;
		}

		float yaw = player.getYRot();
		float pitch = player.getXRot();

		float f = 0.4F;
		double motionX = (double)(-Mth.sin(yaw / 180.0F * (float)Math.PI) * Mth.cos(pitch / 180.0F * (float)Math.PI) * f);
		double motionZ = (double)(Mth.cos(yaw / 180.0F * (float)Math.PI) * Mth.cos(pitch / 180.0F * (float)Math.PI) * f);
		double motionY = (double)(-Mth.sin(pitch / 180.0F * (float)Math.PI) * f);

		// handleHookCasting avec speed=1.5F et inaccuracy=1.0F
		float speed = 1.5F;
		float inaccuracy = 1.0F;
		double len = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		motionX = (motionX / len) * speed;
		motionY = (motionY / len) * speed;
		motionZ = (motionZ / len) * speed;

		Random rand = hook.getLevel().getRandom();
		motionX += rand.nextGaussian() * 0.0075D * inaccuracy;
		motionY += rand.nextGaussian() * 0.0075D * inaccuracy;
		motionZ += rand.nextGaussian() * 0.0075D * inaccuracy;

		hook.setDeltaMovement(motionX, motionY, motionZ);
	}
	
	/*@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;setDeltaMovement(DDD)V"))
	private void modifyVelocity(FishingHook hook, double x, double y, double z, Operation<Void> original) {
		// This is taken from the litteral MCP 1.8.9 : Marcelektro/MCP-919/blob/main/src/minecraft/net/minecraft/entity/projectile/EntityFishHook.java
		
	}*/

	@Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onHitEntity(EntityHitResult result, CallbackInfo ci) {
        FishingHook hook = (FishingHook)(Object)this;
        Entity target = result.getEntity();
        Entity owner = hook.getOwner();
        if (!(owner instanceof Player player)) return;
		target.hurt(DamageSource.thrown(hook, player), 0.0F);
        ci.cancel();
    }
	
}
