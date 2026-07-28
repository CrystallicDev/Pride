package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@Mixin(value = LivingEntity.class, remap = false)
public interface PlayerAccessor {
	@Accessor//("f_20922")
	int getAttackStrengthTicker();
	// 26.1 : actuallyHurt gagne un ServerLevel en 1er param.
	@Invoker("actuallyHurt")
    void invokeActuallyHurt(ServerLevel level, DamageSource source, float amount);
}