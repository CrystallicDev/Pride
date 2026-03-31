package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
        method = "use",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUse(Level level, Player player, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        /*ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof SwordItem)) return;
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof ShieldItem) return;

        player.startUsingItem(hand);
        cir.setReturnValue(InteractionResultHolder.consume(stack));*/
    }
    
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof SwordItem) {
        	System.out.println("SWORD BLOCK !");
            cir.setReturnValue(7200);			
            return;
        }
        if (stack.getItem().isEdible()) {
            cir.setReturnValue(stack.getFoodProperties(null).isFastFood() ? 16 : 32);
            return;
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (stack.getItem() instanceof SwordItem) {
            cir.setReturnValue(UseAnim.BLOCK);
            return;
        }
        cir.setReturnValue(stack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE);
    }
}