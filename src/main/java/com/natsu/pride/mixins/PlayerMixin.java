package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.features.PrideFeature;
import com.natsu.pride.utils.CombatHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin {

	private boolean isReducingParryDamage = false;

	// no swing when dropping from the inventory
	@WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;swing(Lnet/minecraft/world/InteractionHand;)V"))
	private void removeInventoryDropSwing(Player instance, InteractionHand hand, Operation<Void> original) {
		if (PrideFeature.REMOVE_DROP_SWING.enabled()) return;
		original.call(instance, hand);
	}

	// no swimming (1.8.9)
	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void preventSwimming(CallbackInfo ci) {
		if (!PrideFeature.DISABLE_SWIMMING.enabled()) return;
		((Player) (Object) this).setSwimming(false);
		ci.cancel();
	}

	// gameplay cooldown (damage/knockback scale with the attack charge). toggle per weapon type.
	@Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
	public void getAttackStrengthScale(float f, CallbackInfoReturnable<Float> cir) {
		if (!PrideFeature.active()) return;
		if (CombatHelper.attackCooldownDisabledFor(((Player) (Object) this).getMainHandItem())) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
	private void reduceParryDamage(DamageSource source, float amount, CallbackInfo ci) {
		if (isReducingParryDamage) return;
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;

		Player self = (Player) (Object) this;

		// no isBlocking() here: Forge/NeoForge reserves it for items with the SHIELD_BLOCK ability
		if (!self.isUsingItem()) return;
		if (!(self.getUseItem().getItem() instanceof SwordItem)) return;
		if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) return;
		if (source.getEntity() == null) return;

		amount = (1.0F + amount) * (1 - (float) PrideFeature.blockingDamageReduction());

		isReducingParryDamage = true;
		((PlayerAccessor) (Object) self).invokeActuallyHurt(source, amount);
		isReducingParryDamage = false;

		ci.cancel();
	}

	// full 1.8.9-style attack rewrite (damage/knockback/crits/sounds via CombatHelper).
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void attack(Entity targetEntity, CallbackInfo ci) {
		if (!PrideFeature.active()) return;
		Player self = (Player) (Object) this;

		// while blocking: the swing is allowed, but no actual hit lands
		if (PrideFeature.ALLOW_SWORD_BLOCKING.enabled()
				&& self.isUsingItem() && self.getUseItem().getItem() instanceof SwordItem) {
			ci.cancel();
			return;
		}

		if (!net.neoforged.neoforge.common.CommonHooks.onPlayerAttackTarget(self, targetEntity)) {
			ci.cancel();
			return;
		}

		if (targetEntity.isAttackable() && !targetEntity.skipAttackInteraction(self)) {
			float baseDamage = CombatHelper.getBaseDamage(self);
			float totalDamage = CombatHelper.getTotalDamage(self, targetEntity, baseDamage);
			if (baseDamage > 0 || totalDamage > 0) {
				float knockBack = CombatHelper.getTotalAttackKnockback(self, targetEntity);
				float attackStrengthScale = self.getAttackStrengthScale(0.5F);
				self.resetAttackStrengthTicker();
				boolean canSweep = CombatHelper.canAttacKSweep(self, targetEntity, attackStrengthScale);
				boolean isCriticalHit = CombatHelper.isCritical(self, targetEntity);
				float additionalDamage = totalDamage / (isCriticalHit ? 1.5f : 1);

				float targetHealth = 0.0F;
				if (targetEntity instanceof LivingEntity living) {
					targetHealth = living.getHealth();
				}

				Vec3 targetMovement = targetEntity.getDeltaMovement();
				DamageSource damagesource = self.damageSources().playerAttack(self);
				boolean targetCanBeHurt = targetEntity.hurt(damagesource, totalDamage);
				if (targetCanBeHurt) {
					if (knockBack > 0) {
						CombatHelper.knockbackOnHit(self, targetEntity, knockBack);
						self.setDeltaMovement(self.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
						self.setSprinting(false);
					}

					if (canSweep) {
						float sweepingDamage = 1.0F + (float) self.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * totalDamage;
						for (LivingEntity livingentity : self.level().getEntitiesOfClass(LivingEntity.class,
								self.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(self, targetEntity))) {
							if (livingentity != self && livingentity != targetEntity && !self.isAlliedTo(livingentity)
									&& (!(livingentity instanceof ArmorStand stand) || !stand.isMarker())
									&& self.distanceToSqr(livingentity) < 9.0) {
								livingentity.knockback(0.4F,
										Mth.sin(self.getYRot() * ((float) Math.PI / 180F)),
										-Mth.cos(self.getYRot() * ((float) Math.PI / 180F)));
								livingentity.hurt(damagesource, sweepingDamage);
							}
						}
						self.level().playSound(null, self.getX(), self.getY(), self.getZ(),
								SoundEvents.PLAYER_ATTACK_SWEEP, self.getSoundSource(), 1.0F, 1.0F);
						self.sweepAttack();
					}

					CombatHelper.foodExhaustion(self);

					if (targetEntity instanceof ServerPlayer && targetEntity.hurtMarked) {
						((ServerPlayer) targetEntity).connection.send(new ClientboundSetEntityMotionPacket(targetEntity));
						targetEntity.hurtMarked = false;
						targetEntity.setDeltaMovement(targetMovement);
					}
					CombatHelper.playSounds(self, targetEntity, canSweep, isCriticalHit);

					if (isCriticalHit) {
						self.crit(targetEntity);
					}
					if (additionalDamage > 0.0F) {
						self.magicCrit(targetEntity);
					}

					self.setLastHurtMob(targetEntity);

					// doPostAttackEffects rolls the post-hit effects and the fire aspect together
					// (fire aspect isn't a separately-read level anymore, it's a post-attack effect).
					if (self.level() instanceof ServerLevel serverLevel) {
						EnchantmentHelper.doPostAttackEffects(serverLevel, targetEntity, damagesource);
					}

					ItemStack itemstack1 = self.getMainHandItem();
					Entity entity = targetEntity;
					if (targetEntity instanceof net.neoforged.neoforge.entity.PartEntity) {
						entity = ((net.neoforged.neoforge.entity.PartEntity<?>) targetEntity).getParent();
					}

					if (!self.level().isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
						ItemStack copy = itemstack1.copy();
						itemstack1.hurtEnemy((LivingEntity) entity, self);
						if (itemstack1.isEmpty()) {
							net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(self, copy, InteractionHand.MAIN_HAND);
							self.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
						}
					}

					if (targetEntity instanceof LivingEntity living) {
						float damageDealt = targetHealth - living.getHealth();
						self.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));

						// damage indicator particles: they don't exist in 1.8
						if (!PrideFeature.REVERT_DAMAGE_LOGIC.enabled() && self.level() instanceof ServerLevel serverLevel && damageDealt > 2.0F) {
							int particles = (int) ((double) damageDealt * 0.5D);
							serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(),
									targetEntity.getY(0.5D), targetEntity.getZ(), particles, 0.1D, 0.0D, 0.1D, 0.2D);
						}
					}
				} else if (PrideFeature.PLAY_WEAK_HIT_SOUNDS.enabled()) {
					self.level().playSound(null, self.getX(), self.getY(), self.getZ(),
							SoundEvents.PLAYER_ATTACK_NODAMAGE, self.getSoundSource(), 1.0F, 1.0F);
				}
			}
		}
		ci.cancel();
	}

}
