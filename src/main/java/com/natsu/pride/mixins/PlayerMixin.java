package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natsu.pride.features.PrideFeature;
import com.natsu.pride.utils.CombatHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

// Stage 1 (port NeoForge) : seules les méthodes CLIENT/blocage. La réécriture complète de
// `attack` (combat 1.8.9) est différée au Stage 2 (voir port_stage2/, à re-porter depuis la 1.20.1).
@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin {

	private boolean isReducingParryDamage = false;

	// 26.1 : Player.drop ne swing plus le bras (le swing du Q-drop en monde est géré dans
	// Minecraft.handleKeybinds, cf. MinecraftMixin). Plus rien à intercepter côté inventaire.

	// pas de nage (1.8.9)
	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void preventSwimming(CallbackInfo ci) {
		if (!PrideFeature.DISABLE_SWIMMING.enabled()) return;
		((Player) (Object) this).setSwimming(false);
		ci.cancel();
	}

	/**
	 * Armes lourdes : hache, masse (1.21) et trident. Elles sont équilibrées AUTOUR du délai de
	 * coup (dégâts réduits si on frappe trop tôt), contrairement à l'épée qui doit pouvoir spammer
	 * comme en 1.8.9 — d'où deux options de config distinctes.
	 */
	@Unique
	private static boolean pride$isHeavyWeapon(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof AxeItem || item instanceof MaceItem || item instanceof TridentItem;
	}

	@Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
	public void getAttackStrengthScale(float f, CallbackInfoReturnable<Float> cir) {
		if (!PrideFeature.active()) return;
		Player self = (Player) (Object) this;
		boolean heavy = pride$isHeavyWeapon(self.getMainHandItem());
		if ((PrideFeature.DISABLE_AXE_ATTACK_COOLDOWN.enabled() && heavy) ||
			(PrideFeature.DISABLE_SWORD_ATTACK_COOLDOWN.enabled() && !heavy)) {
			cir.setReturnValue(1.0F);
		}
	}

	// 26.1 : actuallyHurt gagne un ServerLevel en 1er param.
	@Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
	private void reduceParryDamage(ServerLevel level, DamageSource source, float amount, CallbackInfo ci) {
		if (isReducingParryDamage) return;
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;

		Player self = (Player) (Object) this;

		// pas de isBlocking() : Forge/NeoForge le réserve aux items avec l'ability SHIELD_BLOCK
		if (!self.isUsingItem()) return;
		if (!self.getUseItem().is(ItemTags.SWORDS)) return;
		if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) return;
		if (source.getEntity() == null) return;

		amount = (1.0F + amount) * (1 - (float) PrideFeature.blockingDamageReduction());

		isReducingParryDamage = true;
		((PlayerAccessor) (Object) self).invokeActuallyHurt(level, source, amount);
		isReducingParryDamage = false;

		ci.cancel();
	}

	// Réécriture complète de l'attaque façon 1.8.9 (dégâts/knockback/crits/sons via CombatHelper).
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void attack(Entity targetEntity, CallbackInfo ci) {
		if (!PrideFeature.active()) return;
		Player self = (Player) (Object) this;

		// en bloquant : swing autorisé, mais pas de vrai coup
		if (PrideFeature.ALLOW_SWORD_BLOCKING.enabled()
				&& self.isUsingItem() && self.getUseItem().is(ItemTags.SWORDS)) {
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
				boolean targetCanBeHurt = targetEntity.hurtOrSimulate(damagesource, totalDamage);
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
								livingentity.hurtOrSimulate(damagesource, sweepingDamage);
							}
						}
						self.level().playSound(null, self.getX(), self.getY(), self.getZ(),
								SoundEvents.PLAYER_ATTACK_SWEEP, self.getSoundSource(), 1.0F, 1.0F);
						// 26.1 : Player.sweepAttack() n'est plus public (fondu dans doSweepAttack) : on
						// reproduit juste le spawn des particules de sweep.
						if (self.level() instanceof ServerLevel sweepLevel) {
							double sdx = -Mth.sin(self.getYRot() * ((float) Math.PI / 180F));
							double sdz = Mth.cos(self.getYRot() * ((float) Math.PI / 180F));
							sweepLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, self.getX() + sdx, self.getY(0.5D),
									self.getZ() + sdz, 0, sdx, 0.0D, sdz, 0.0D);
						}
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

					// 1.21 : doPostAttackEffects remplace doPostHurtEffects/doPostDamageEffects ET l'aspect
					// de feu (fire aspect n'est plus un niveau lu à part, c'est un effet post-attaque).
					if (self.level() instanceof ServerLevel serverLevel) {
						EnchantmentHelper.doPostAttackEffects(serverLevel, targetEntity, damagesource);
					}

					ItemStack itemstack1 = self.getMainHandItem();
					Entity entity = targetEntity;
					if (targetEntity instanceof net.neoforged.neoforge.entity.PartEntity) {
						entity = ((net.neoforged.neoforge.entity.PartEntity<?>) targetEntity).getParent();
					}

					if (!self.level().isClientSide() && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
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

						// particules de dégâts (coeurs) : elles n'existent pas en 1.8
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
