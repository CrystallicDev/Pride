package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;

/**
 * MLG 1.8.9 : en 1.8.9 le waterlogging n'existe pas, donc vider un seau vise toujours à poser une
 * vraie source où l'on peut atterrir. En moderne, viser une dalle/un escalier "absorbe" l'eau dans
 * le bloc et fait rater le clutch.
 *
 * <p>Fix : on fait échouer {@code canPlaceLiquid} quand le joueur ne sneak PAS. {@code emptyContents}
 * retombe alors tout seul sur la face cliquée ({@code pos.relative(direction)}) et y pose une source.
 * En sneakant, le comportement vanilla est conservé (waterlogging volontaire).
 */
@Mixin(value = BucketItem.class, remap = false)
public class BucketItemMixin {

	// 26.1 : emptyContents prend un LivingEntity (au lieu de Player). NeoForge ajoute une surcharge
	// à 5 args avec l'ItemStack — c'est ELLE qui contient l'appel canPlaceLiquid (vérifié au bytecode).
	// target canPlaceLiquid sans descripteur : matche quelle que soit sa signature.
	@ModifyExpressionValue(
			method = "emptyContents(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid"))
	private boolean pride$noWaterlogUnlessSneaking(boolean original, @Local(argsOnly = true) LivingEntity user) {
		if (!PrideFeature.WATERLOG_ONLY_WHEN_SNEAKING.enabled()) return original;
		if (user != null && user.isShiftKeyDown()) return original;
		return false;
	}
}
