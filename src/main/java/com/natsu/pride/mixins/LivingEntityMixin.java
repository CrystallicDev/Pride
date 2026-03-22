package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.common.capability.ParryCapabilityProvider;
import com.natsu.pride.config.PrideConfig;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	/*@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cb) {
		LivingEntity self = (LivingEntity)(Object)this;
		
		if (!(self instanceof Player player)) return;
		if (source.isBypassArmor() || source.isBypassInvul() || source.isBypassMagic()) return;
		
		player.getCapability(ParryCapabilityProvider.PARRY_CAP).ifPresent(cap -> {
			if (cap.isParrying() && cap.getCooldown() == 0) {
				int cd = PrideConfig.getSwordBlockingSuccessCooldown();
				if (cd > 0) {
					cap.setParrying(false);
					cap.setCooldown(cd);
				}
				
			}
			
			
		});
	}*/
	
}
