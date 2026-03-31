package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;


import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	/*@Inject(method = "renderArmWithItem", at = @At("HEAD"))
	private void applyBlockingTransform(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
			float aimPitch, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, CallbackInfo ci) {
		if (player.isUsingItem() && player.getUseItem().getItem() instanceof SwordItem
				&& player.getUsedItemHand() == (hand)) {

			poseStack.mulPose(Vector3f.XP.rotationDegrees(-10f));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(hand == InteractionHand.MAIN_HAND ? -35f : 35f));
			poseStack.translate(hand == InteractionHand.MAIN_HAND ? -0.15 : 0.15, 0.1, 0.0);
		}
		
		
		
	}*/
	
	
	
}
