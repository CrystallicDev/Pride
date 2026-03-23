package fr.natsu.pride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import fr.natsu.pride.common.capability.ParryCapabilityProvider;
import fr.natsu.pride.config.PrideConfig;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@Inject(method = "renderArmWithItem", at = @At("HEAD"))
	private void applyBlockingTransform(AbstractClientPlayer player, float partialTick, float pitch, HumanoidArm arm,
			float aimPitch, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, CallbackInfo ci) {
		if (player.isUsingItem() && player.getUseItem().getItem() instanceof SwordItem
				&& player.getUsedItemHand() == (arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND
						: InteractionHand.OFF_HAND)) {

			poseStack.mulPose(Vector3f.XP.rotationDegrees(-10f));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(arm == HumanoidArm.RIGHT ? -35f : 35f));
			poseStack.translate(arm == HumanoidArm.RIGHT ? -0.15 : 0.15, 0.1, 0.0);
		}
	}
	
}
