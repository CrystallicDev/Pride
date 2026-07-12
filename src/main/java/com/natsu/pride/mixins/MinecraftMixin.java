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

	/**
	 * Swing visible pendant le blocage à l'épée : vanilla vide les clics d'attaque
	 * sans rien faire quand un item est en cours d'utilisation. On les consomme nous-
	 * mêmes pour ne jouer QUE l'animation de bras — aucune attaque ni minage n'est
	 * déclenché (on n'appelle jamais startAttack/continueDestroyBlock).
	 */
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

	/** En 1.8.9, jeter un item (Q en jeu) ne déclenche pas d'animation de swing. */
	@WrapOperation(method = "handleKeybinds",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
	private void removeInWorldDropSwing(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
		if (ServerConfig.loaded() && ServerConfig.REMOVE_DROP_SWING.get()) return;
		original.call(instance, hand);
	}

}
