package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.natsu.pride.config.ServerConfig;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.SwordItem;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@Inject(method = "renderArmWithItem",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
	private void applyBlockingPose(AbstractClientPlayer player, float partialTicks, float pitch,
			InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress,
			PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
		if (!ServerConfig.loaded() || !ServerConfig.ALLOW_SWORD_BLOCKING.get()) return;
		if (!(stack.getItem() instanceof SwordItem)) return;
		if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0) return;
		if (player.getUsedItemHand() != hand) return;

		boolean mainHand = hand == InteractionHand.MAIN_HAND;
		HumanoidArm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
		float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;

		if (swingProgress > 0.0F) {
			float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
			float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(side * f * -20.0F));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(side * f1 * -20.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(f1 * -80.0F));
		}

		poseStack.translate(side * -0.14142136F, 0.08F, 0.14142136F);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(-102.25F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(side * 13.365F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(side * 78.05F));
	}

	@WrapOperation(method = "tick",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraftforge/client/ForgeHooksClient;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z",
					remap = false))
	private boolean removeBucketReequipAnimation(ItemStack from, ItemStack to, int slot, Operation<Boolean> original) {
		boolean requip = original.call(from, to, slot);
		if (requip && ServerConfig.loaded() && ServerConfig.REMOVE_BUCKET_ANIMATION.get()
				&& pride$isBucketLike(from) && pride$isBucketLike(to)) {
			return false;
		}
		return requip;
	}

	private static boolean pride$isBucketLike(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof BucketItem || item instanceof MilkBucketItem || item instanceof SolidBucketItem;
	}

}
