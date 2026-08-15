package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;

/**
 * 1.8.9 MLG: waterlogging doesn't exist in 1.8.9, so emptying a bucket always places a real
 * source you can land in. On modern versions, aiming at a slab or stairs "soaks up" the water into
 * the block and the clutch fails.
 *
 * <p>Fix: we make {@code canPlaceLiquid} fail when the player is NOT sneaking. {@code emptyContents}
 * then falls back on its own to the clicked face and places a source there. Sneaking keeps the
 * vanilla behaviour (waterlogging on purpose).
 */
@Mixin(BucketItem.class)
public class BucketItemMixin {

	@ModifyExpressionValue(
			method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z"))
	private boolean pride$noWaterlogUnlessSneaking(boolean original, @Local(argsOnly = true) Player player) {
		if (!PrideFeature.WATERLOG_ONLY_WHEN_SNEAKING.enabled()) return original;
		if (player != null && player.isShiftKeyDown()) return original;
		return false;
	}
}
