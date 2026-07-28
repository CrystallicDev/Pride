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

	// 1.8.9 : utiliser un item (manger, boire) coupe le sprint. La 1.9+ le laisse actif,
	// d'où un déplacement plus rapide en mangeant. On rétablit la coupure.
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void cancelSprintWhileUsingItem(CallbackInfo ci) {
		if (!PrideFeature.SLOW_WHILE_USING_ITEM.enabled()) return;
		LocalPlayer self = (LocalPlayer) (Object) this;
		if (self.isUsingItem() && self.isSprinting()) {
			self.setSprinting(false);
		}
	}

	// 1.8.9 : le sprint reste actif dans l'eau. La 1.18 le coupe dès que la tête sort
	// (aiStep n'autorise le sprint que onGround/immergé). On empêche uniquement cette
	// annulation quand on nage en avant en tenant la touche sprint.
	@WrapOperation(method = "aiStep",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V"))
	private void keepSprintInWater(LocalPlayer instance, boolean sprinting, Operation<Void> original) {
		if (!sprinting && PrideFeature.DISABLE_SWIMMING.enabled()
				&& instance.isInWater() && !instance.isUsingItem()
				&& instance.input.hasForwardImpulse()
				&& Minecraft.getInstance().options.keySprint.isDown()) {
			return; // on nage en sprint : ne pas couper
		}
		original.call(instance, sprinting);
	}
}
