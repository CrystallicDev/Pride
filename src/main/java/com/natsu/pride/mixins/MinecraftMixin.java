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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.SwordItem;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	// visible swing while blocking: we swallow the attack clicks just for the animation
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void swingWhileBlocking(CallbackInfo ci) {
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || !player.isUsingItem()) return;
		if (!(player.getUseItem().getItem() instanceof SwordItem)) return;
		while (mc.options.keyAttack.consumeClick()) {
			player.swing(InteractionHand.MAIN_HAND);
		}
	}

	// no swing when dropping an item (Q)
	@WrapOperation(method = "handleKeybinds",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
	private void removeInWorldDropSwing(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
		if (PrideFeature.REMOVE_DROP_SWING.enabled()) return;
		original.call(instance, hand);
	}

}
