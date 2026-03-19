package fr.natsu.pride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import fr.natsu.pride.config.PrideConfig;
import fr.natsu.pride.utils.CombatHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(Player.class)
public abstract class PlayerMixin {

	/**
     * Disable the attack cooldown when needed
     * @reason Required
     * @author Natsu91
     * */
	@Overwrite
	public float getAttackStrengthScale(float f) {
		Player self = (Player) (Object) this;
		int attackThicker = ((PlayerAccessor)(Object)this).getAttackStrengthTicker();
		ItemStack item = self.getMainHandItem();
		if (item == null) return 0.0F;
		if ((PrideConfig.isDisableAttackCooldownForAxe() && item.getItem() instanceof AxeItem) || (PrideConfig.isDisableAttackCooldownForSword() && !(item.getItem() instanceof AxeItem))) {
			return 1.0F;
		}
		return Mth.clamp(((float)attackThicker + f) / self.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
	}
	
	@ModifyArg(
	        method = "hurt",
	        at = @At(
	            value = "INVOKE",
	            target = "Lnet/minecraft/world/entity/player/Player;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"
	        ),
	        index = 1 
	    )
	    private float reduceParryDamage(DamageSource source, float amount) {
	        Player self = (Player)(Object) this;

	        if (!self.isBlocking()) return amount;
	        if (!(self.getUseItem().getItem() instanceof SwordItem)) return amount;
	        if (source.isBypassArmor()) return amount;

	        if (source.getEntity() != null) {
	        	amount = (1.0F + amount) * PrideConfig.getSwordBlockingDamageReduction();		// From MCP mappings : https://github.com/Marcelektro/MCP-919/blob/main/src/minecraft/net/minecraft/entity/player/EntityPlayer.java
	        	return amount;
	        }

	        return amount;
	    }
	
	/**
     * Allow for custom damage and knockback calculations
     * @reason Required
     * @author Natsu91
     * */
	@Overwrite
	public void attack(Entity targetEntity) {
		Player self = (Player) (Object) this;
		ItemStack item = self.getMainHandItem();
		boolean isWeaponAxe = (item != null ? (item.getItem() instanceof AxeItem) : false);
		boolean isWeaponSword = (item != null ? (item.getItem() instanceof SwordItem) : false);
		
		
		if (targetEntity.isAttackable()) {
			if (!targetEntity.skipAttackInteraction(self)) {
				float baseDamage = CombatHelper.getBaseDamage(self);
				float totalDamage = CombatHelper.getTotalDamage(self, targetEntity, baseDamage);
				//If the damage is not > 0, nothing will happen, common to both versions
				//We calculated Critical hits early, but it is a multiplier, so it does not
				//matter
				if (baseDamage > 0 || totalDamage > 0) {
					float knockBack = CombatHelper.getTotalAttackKnockback(self);
					float attackStrengthScale = self.getAttackStrengthScale(0.5F);
					boolean canSweep = CombatHelper.canAttacKSweep(self, targetEntity, attackStrengthScale);
					float targetHealth = 0.0F;
					boolean shouldSetOnFire = false;
					boolean isCriticalHit = CombatHelper.isCritical(self, targetEntity);
					float additionalDamage = totalDamage / (isCriticalHit ? 1.5f : 1);
		
					//Fire Aspect and Health storing can stay the same
					int hasFireAspect = EnchantmentHelper.getFireAspect(self);
					if (targetEntity instanceof LivingEntity) {
						targetHealth = ((LivingEntity) targetEntity).getHealth();
						if (hasFireAspect > 0 && !targetEntity.isOnFire()) {
							shouldSetOnFire = true;
							targetEntity.setSecondsOnFire(1);
						}
					}
					
					Vec3 targetMovement = targetEntity.getDeltaMovement();
					boolean targetCanBeHurt = targetEntity.hurt(DamageSource.playerAttack(self), totalDamage);
					if (targetCanBeHurt) {
						if (knockBack > 0) {
							// This handles 1.8 / 1.9+ knockbacks
							CombatHelper.knockbackOnHit(self, targetEntity, knockBack);
							// This is the same between both versions
							self.setDeltaMovement(self.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
							self.setSprinting(false);
						}

						if (canSweep) {
							//This is the same sweep method than vanilla
							float sweepingDamage = 1.0F + EnchantmentHelper.getSweepingDamageRatio(self) * totalDamage;
							for (LivingEntity livingentity : self.level.getEntitiesOfClass(LivingEntity.class,
									self.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(self, targetEntity))) {
								if (livingentity != self && livingentity != targetEntity && !self.isAlliedTo(livingentity)
										&& (!(livingentity instanceof ArmorStand)
												|| !((ArmorStand) livingentity).isMarker())
										&& self.canHit(livingentity, 0)) { // Original check was dist < 3, range is 3,
																			// so vanilla used padding=0
									livingentity.knockback((double) 0.4F,
											(double) Mth.sin(self.getYRot() * ((float) Math.PI / 180F)),
											(double) (-Mth.cos(self.getYRot() * ((float) Math.PI / 180F))));
									livingentity.hurt(DamageSource.playerAttack(self), sweepingDamage);
								}
							}

							self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
									SoundEvents.PLAYER_ATTACK_SWEEP, self.getSoundSource(), 1.0F, 1.0F);
							self.sweepAttack();
						}

						CombatHelper.foodExhaustion(self);
						
						//Everything after this is default from vanilla
						//Nothing much will change, except the sound handling.
						
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
						if (targetEntity instanceof LivingEntity) {
							EnchantmentHelper.doPostHurtEffects((LivingEntity) targetEntity, self);
						}

						EnchantmentHelper.doPostDamageEffects(self, targetEntity);
						ItemStack itemstack1 = self.getMainHandItem();
						Entity entity = targetEntity;
						if (targetEntity instanceof net.minecraftforge.entity.PartEntity) {
							entity = ((net.minecraftforge.entity.PartEntity<?>) targetEntity).getParent();
						}

						if (!self.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
							ItemStack copy = itemstack1.copy();
							itemstack1.hurtEnemy((LivingEntity) entity, self);
							if (itemstack1.isEmpty()) {
								net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(self, copy,
										InteractionHand.MAIN_HAND);
								self.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}

						if (targetEntity instanceof LivingEntity) {
							float damageDealt = targetHealth - ((LivingEntity) targetEntity).getHealth();
							self.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
							if (hasFireAspect > 0) {
								targetEntity.setSecondsOnFire(hasFireAspect * 4);
							}

							if (self.level instanceof ServerLevel && damageDealt > 2.0F) {
								int hasEnoughDamageForParticles = (int) ((double) damageDealt * 0.5D);
								((ServerLevel) self.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(),
										targetEntity.getY(0.5D), targetEntity.getZ(), hasEnoughDamageForParticles, 0.1D, 0.0D, 0.1D,
										0.2D);
							}
						}
					} else {
						self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
								SoundEvents.PLAYER_ATTACK_NODAMAGE, self.getSoundSource(), 1.0F, 1.0F);
						if (shouldSetOnFire) {
							targetEntity.clearFire();
						}
					}
					
				}
			}
		}
	}
	
	
	/*
	private void actualAttackMapped(Entity target) {
		Player self = (Player) (Object) this;

		if (target.isAttackable()) {
			if (!target.skipAttackInteraction(self)) {
				float baseAttackDamage = (float) self.getAttributeValue(Attributes.ATTACK_DAMAGE);
				float additionalDamage;
				if (target instanceof LivingEntity) {
					additionalDamage = EnchantmentHelper.getDamageBonus(self.getMainHandItem(),
							((LivingEntity) target).getMobType());
				} else {
					additionalDamage = EnchantmentHelper.getDamageBonus(self.getMainHandItem(), MobType.UNDEFINED);
				}

				float attackStrengthScale = self.getAttackStrengthScale(0.5F);
				baseAttackDamage *= 0.2F + attackStrengthScale * attackStrengthScale * 0.8F;
				additionalDamage *= attackStrengthScale;
				self.resetAttackStrengthTicker();
				if (baseAttackDamage > 0.0F || additionalDamage > 0.0F) {			//AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
					boolean isAttackTimerOk = attackStrengthScale > 0.9F;
					boolean sprintAndAttackTimerOk = false;
					float attackKnockback = (float) self.getAttributeValue(Attributes.ATTACK_KNOCKBACK); 
					// Forge: Initialize self value to the attack knockback attribute of the player, which is by default 0
					attackKnockback += EnchantmentHelper.getKnockbackBonus(self);
					if (self.isSprinting() && isAttackTimerOk) {
						self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
								SoundEvents.PLAYER_ATTACK_KNOCKBACK, self.getSoundSource(), 1.0F, 1.0F);
						++attackKnockback;
						sprintAndAttackTimerOk = true;
					}

					boolean isCriticalHit = isAttackTimerOk && self.fallDistance > 0.0F && !self.isOnGround()
							&& !self.onClimbable() && !self.isInWater() && !self.hasEffect(MobEffects.BLINDNESS)
							&& !self.isPassenger() && target instanceof LivingEntity;
					isCriticalHit = isCriticalHit && !self.isSprinting();
					net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks
							.getCriticalHit(self, target, isCriticalHit, isCriticalHit ? 1.5F : 1.0F);
					isCriticalHit = hitResult != null;
					if (isCriticalHit) {
						baseAttackDamage *= hitResult.getDamageModifier();
					}

					baseAttackDamage += additionalDamage;
					boolean canAttackSweep = false;
					double walkDistanceDelta = (double) (self.walkDist - self.walkDistO);
					if (isAttackTimerOk && !isCriticalHit && !sprintAndAttackTimerOk && self.isOnGround()
							&& walkDistanceDelta < (double) self.getSpeed()) {
						ItemStack itemstack = self.getItemInHand(InteractionHand.MAIN_HAND);
						canAttackSweep = itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP);
					}

					float targetHealth = 0.0F;
					boolean shouldSetOnFire = false;
					int hasFireAspect = EnchantmentHelper.getFireAspect(self);
					if (target instanceof LivingEntity) {
						targetHealth = ((LivingEntity) target).getHealth();
						if (hasFireAspect > 0 && !target.isOnFire()) {
							shouldSetOnFire = true;
							target.setSecondsOnFire(1);
						}
					}

					Vec3 targetMovement = target.getDeltaMovement();
					boolean targetCanBeHurt = target.hurt(DamageSource.playerAttack(self), baseAttackDamage);
					if (targetCanBeHurt) {
						if (attackKnockback > 0) {
							if (target instanceof LivingEntity) {
								((LivingEntity) target).knockback((double) ((float) attackKnockback * 0.5F),
										(double) Mth.sin(self.getYRot() * ((float) Math.PI / 180F)),
										(double) (-Mth.cos(self.getYRot() * ((float) Math.PI / 180F))));
							} else {
								target.push(
										(double) (-Mth.sin(self.getYRot() * ((float) Math.PI / 180F))
												* (float) attackKnockback * 0.5F),
										0.1D, (double) (Mth.cos(self.getYRot() * ((float) Math.PI / 180F))
												* (float) attackKnockback * 0.5F));
							}

							self.setDeltaMovement(self.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
							self.setSprinting(false);
						}

						if (canAttackSweep) {
							float sweepingDamage = 1.0F
									+ EnchantmentHelper.getSweepingDamageRatio(self) * baseAttackDamage;

							for (LivingEntity livingentity : self.level.getEntitiesOfClass(LivingEntity.class,
									self.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(self, target))) {
								if (livingentity != self && livingentity != target && !self.isAlliedTo(livingentity)
										&& (!(livingentity instanceof ArmorStand)
												|| !((ArmorStand) livingentity).isMarker())
										&& self.canHit(livingentity, 0)) { // Original check was dist < 3, range is 3,
																			// so vanilla used padding=0
									livingentity.knockback((double) 0.4F,
											(double) Mth.sin(self.getYRot() * ((float) Math.PI / 180F)),
											(double) (-Mth.cos(self.getYRot() * ((float) Math.PI / 180F))));
									livingentity.hurt(DamageSource.playerAttack(self), sweepingDamage);
								}
							}

							self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
									SoundEvents.PLAYER_ATTACK_SWEEP, self.getSoundSource(), 1.0F, 1.0F);
							self.sweepAttack();
						}

						if (target instanceof ServerPlayer && target.hurtMarked) {
							((ServerPlayer) target).connection.send(new ClientboundSetEntityMotionPacket(target));
							target.hurtMarked = false;
							target.setDeltaMovement(targetMovement);
						}

						if (isCriticalHit) {
							self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
									SoundEvents.PLAYER_ATTACK_CRIT, self.getSoundSource(), 1.0F, 1.0F);
							self.crit(target);
						}

						if (!isCriticalHit && !canAttackSweep) {
							if (isAttackTimerOk) {
								self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
										SoundEvents.PLAYER_ATTACK_STRONG, self.getSoundSource(), 1.0F, 1.0F);
							} else {
								self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
										SoundEvents.PLAYER_ATTACK_WEAK, self.getSoundSource(), 1.0F, 1.0F);
							}
						}

						if (additionalDamage > 0.0F) {
							self.magicCrit(target);
						}

						self.setLastHurtMob(target);
						if (target instanceof LivingEntity) {
							EnchantmentHelper.doPostHurtEffects((LivingEntity) target, self);
						}

						EnchantmentHelper.doPostDamageEffects(self, target);
						ItemStack itemstack1 = self.getMainHandItem();
						Entity entity = target;
						if (target instanceof net.minecraftforge.entity.PartEntity) {
							entity = ((net.minecraftforge.entity.PartEntity<?>) target).getParent();
						}

						if (!self.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
							ItemStack copy = itemstack1.copy();
							itemstack1.hurtEnemy((LivingEntity) entity, self);
							if (itemstack1.isEmpty()) {
								net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(self, copy,
										InteractionHand.MAIN_HAND);
								self.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}

						if (target instanceof LivingEntity) {
							float damageDealt = targetHealth - ((LivingEntity) target).getHealth();
							self.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
							if (hasFireAspect > 0) {
								target.setSecondsOnFire(hasFireAspect * 4);
							}

							if (self.level instanceof ServerLevel && damageDealt > 2.0F) {
								int hasEnoughDamageForParticles = (int) ((double) damageDealt * 0.5D);
								((ServerLevel) self.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(),
										target.getY(0.5D), target.getZ(), hasEnoughDamageForParticles, 0.1D, 0.0D, 0.1D,
										0.2D);
							}
						}

						self.causeFoodExhaustion(0.1F);
					} else {
						self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
								SoundEvents.PLAYER_ATTACK_NODAMAGE, self.getSoundSource(), 1.0F, 1.0F);
						if (shouldSetOnFire) {
							target.clearFire();
						}
					}
				}

			}
		}
	}*/
}
