package fr.natsu.pride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.natsu.pride.common.capability.ParryCapabilityProvider;
import fr.natsu.pride.config.PrideConfig;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
