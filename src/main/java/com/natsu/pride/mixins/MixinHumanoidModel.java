package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natsu.pride.config.ServerConfig;

import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel<T extends LivingEntity> {

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void restoreOldBodyRotation(T entity, float limbSwing, float limbSwingAmount,
                                        float ageInTicks, float netHeadYaw, float headPitch,
                                        CallbackInfo ci) {
        if (!ServerConfig.CHANGE_BODY_RENDER.get()) return;
        if (!(entity instanceof Player)) return;

        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        float clampedYaw = Mth.clamp(netHeadYaw, -45f, 45f);

        self.body.yRot = 0f;
        self.head.yRot = clampedYaw * Mth.DEG_TO_RAD;
        self.hat.yRot = self.head.yRot;
    }
}