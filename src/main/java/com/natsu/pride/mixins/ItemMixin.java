package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

// hit & block: hold right click with a sword (empty offhand) to block
@Mixin(Item.class)
public class ItemMixin {

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onUse(Level level, Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;

		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof SwordItem)) return;
		if (hand != InteractionHand.MAIN_HAND) return;
		if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) return;

		player.startUsingItem(hand);
		cir.setReturnValue(InteractionResultHolder.consume(stack));
	}

	@Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
	private void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;
		if (stack.getItem() instanceof SwordItem) {
			cir.setReturnValue(7200);
		}
	}

	@Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
	private void getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;
		if (stack.getItem() instanceof SwordItem) {
			cir.setReturnValue(UseAnim.BLOCK);
		}
	}

}
