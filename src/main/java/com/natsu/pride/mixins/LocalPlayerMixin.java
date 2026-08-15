package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

@Mixin(value = LocalPlayer.class, remap = false)
public class LocalPlayerMixin {

	// 1.8.9: using an item (eating, drinking) cancels sprint. 1.9+ keeps it going,
	// so you move faster while eating. we put the cutoff back.
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void cancelSprintWhileUsingItem(CallbackInfo ci) {
		if (!PrideFeature.SLOW_WHILE_USING_ITEM.enabled()) return;
		LocalPlayer self = (LocalPlayer) (Object) this;
		if (self.isUsingItem() && self.isSprinting()) {
			self.setSprinting(false);
		}
	}

	// 1.8.9: sprint stays on in water. 1.18 kills it as soon as your head comes out
	// (aiStep only allows sprint onGround/submerged). we only block that cancel when
	// you're swimming forward while holding the sprint key.
	@WrapOperation(method = "aiStep",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V"))
	private void keepSprintInWater(LocalPlayer instance, boolean sprinting, Operation<Void> original) {
		if (!sprinting && PrideFeature.DISABLE_SWIMMING.enabled()
				&& instance.isInWater() && !instance.isUsingItem()
				&& instance.input.hasForwardImpulse()
				&& Minecraft.getInstance().options.keySprint.isDown()) {
			return; // swimming while sprinting: don't cut it
		}
		original.call(instance, sprinting);
	}
}
