package fr.natsu.pride.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FishingHook.class)
public class FishHookMixin {

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;setDeltaMovement(DDD)V"))
	private void modifyVelocity(FishingHook hook, double x, double y, double z, Operation<Void> original) {

		Player player = hook.getOwner() instanceof Player p ? p : null;
		if (player == null) {
			original.call(hook, x, y, z);
			return;
		}

		float yaw = player.getYRot();
		float pitch = player.getXRot();

		float f = -Mth.sin(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
		float f1 = -Mth.sin(pitch * ((float) Math.PI / 180F));
		float f2 = Mth.cos(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));

		float speed = 1.5F;

		double vx = f * speed;
		double vy = f1 * speed;
		double vz = f2 * speed;

		Random rand = hook.getLevel().getRandom();
		// This is taken from the litteral MCP 1.8.9 : Marcelektro/MCP-919/blob/main/src/minecraft/net/minecraft/entity/projectile/EntityFishHook.java
		vx = (double)(-Mth.sin(yaw / 180.0F * (float)Math.PI) * Mth.cos(pitch / 180.0F * (float)Math.PI) * f);
		vy = (double)(Mth.cos(yaw / 180.0F * (float)Math.PI) * Mth.cos(pitch / 180.0F * (float)Math.PI) * f);
		vz = (double)(-Mth.sin(pitch / 180.0F * (float)Math.PI) * f);

		hook.setDeltaMovement(vx, vy, vz);

	}

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
