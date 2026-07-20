package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(value = LivingEntity.class, remap = false)
public interface PlayerAccessor {
	@Accessor//("f_20922")
	int getAttackStrengthTicker();
	@Invoker("actuallyHurt")
    void invokeActuallyHurt(DamageSource source, float amount);
}