package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;

/**
 * 1.8.9 MLG: waterlogging doesn't exist in 1.8.9, so emptying a bucket always aims to place a
 * real source you can land in. On modern versions, aiming at a slab or stairs "soaks up" the water
 * into the block, and the clutch fails.
 *
 * <p>Fix: we make {@code canPlaceLiquid} fail when the player is NOT sneaking. {@code emptyContents}
 * then falls back on its own to the clicked face ({@code pos.relative(direction)}) and places a source there.
 * Sneaking keeps the vanilla behaviour (waterlogging on purpose).
 */
@Mixin(value = BucketItem.class, remap = false)
public class BucketItemMixin {

	// NeoForge adds a 5-arg emptyContents overload that takes the ItemStack, and that's the one
	// actually holding the canPlaceLiquid call (checked against the bytecode).
	// canPlaceLiquid target with no descriptor: matches whatever the signature happens to be.
	@ModifyExpressionValue(
			method = "emptyContents(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid"))
	private boolean pride$noWaterlogUnlessSneaking(boolean original, @Local(argsOnly = true) LivingEntity user) {
		if (!PrideFeature.WATERLOG_ONLY_WHEN_SNEAKING.enabled()) return original;
		if (user != null && user.isShiftKeyDown()) return original;
		return false;
	}
}
