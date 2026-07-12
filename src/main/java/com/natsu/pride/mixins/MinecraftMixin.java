package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.config.ServerConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.SwordItem;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	// swing visible en bloquant : on consomme les clics d'attaque pour l'anim seule
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void swingWhileBlocking(CallbackInfo ci) {
		if (!ServerConfig.loaded() || !ServerConfig.ALLOW_SWORD_BLOCKING.get()) return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || !player.isUsingItem()) return;
		if (!(player.getUseItem().getItem() instanceof SwordItem)) return;
		while (mc.options.keyAttack.consumeClick()) {
			player.swing(InteractionHand.MAIN_HAND);
		}
	}

	// pas de swing en droppant un item (Q)
	@WrapOperation(method = "handleKeybinds",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
	private void removeInWorldDropSwing(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
		if (ServerConfig.loaded() && ServerConfig.REMOVE_DROP_SWING.get()) return;
		original.call(instance, hand);
	}

}
