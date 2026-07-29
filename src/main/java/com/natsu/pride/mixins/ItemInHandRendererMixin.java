package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SolidBucketItem;

@Mixin(value = ItemInHandRenderer.class, remap = false)
public class ItemInHandRendererMixin {

	// La pose de blocage épée n'est plus gérée ici : en 26.1 renderArmWithItem applique nativement la
	// pose "block" pour toute anim d'usage BLOCK (hors bouclier), donc l'épée (à qui ItemMixin fait
	// renvoyer ItemUseAnimation.BLOCK) est déjà posée correctement. Un transform en plus la doublait.

	@WrapOperation(method = "tick",
			at = @At(value = "INVOKE",
					target = "Lnet/neoforged/neoforge/client/ClientHooks;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z",
					remap = false))
	private boolean removeBucketReequipAnimation(ItemStack from, ItemStack to, int slot, Operation<Boolean> original) {
		boolean requip = original.call(from, to, slot);
		if (requip && PrideFeature.REMOVE_BUCKET_ANIMATION.enabled()
				&& pride$isBucketLike(from) && pride$isBucketLike(to)) {
			return false;
		}
		return requip;
	}

	private static boolean pride$isBucketLike(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof BucketItem || item == Items.MILK_BUCKET || item instanceof SolidBucketItem;
	}

}
