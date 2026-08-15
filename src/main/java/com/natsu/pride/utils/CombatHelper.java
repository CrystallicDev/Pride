package com.natsu.pride.utils;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CombatHelper {

	/**
	 * Heavy weapons (axe, mace, trident, spear): balanced AROUND the attack delay (reduced damage
	 * if you swing too early), unlike the sword which needs to spam like in 1.8.9, hence two
	 * separate config options. Detected by tag (axe/spear are data-driven, no class to match) plus
	 * a class check for mace/trident, which have no dedicated tag.
	 */
	public static boolean isHeavyWeapon(ItemStack stack) {
		return stack.is(ItemTags.AXES) || stack.is(ItemTags.SPEARS)
				|| stack.getItem() instanceof MaceItem || stack.getItem() instanceof TridentItem;
	}

	/** Is the attack cooldown disabled for the held weapon? A config toggle per weapon type. */
	public static boolean attackCooldownDisabledFor(ItemStack stack) {
		if (stack.is(ItemTags.AXES)) return PrideFeature.DISABLE_AXE_ATTACK_COOLDOWN.enabled();
		if (stack.is(ItemTags.SPEARS)) return PrideFeature.DISABLE_SPEAR_ATTACK_COOLDOWN.enabled();
		if (stack.getItem() instanceof MaceItem) return PrideFeature.DISABLE_MACE_ATTACK_COOLDOWN.enabled();
		if (stack.getItem() instanceof TridentItem) return PrideFeature.DISABLE_TRIDENT_ATTACK_COOLDOWN.enabled();
		// sword + everything else (light)
		return PrideFeature.DISABLE_SWORD_ATTACK_COOLDOWN.enabled();
	}


	/**
	 * Returns the base damage of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getBaseDamage(Player player) {
		// the axe's -2 rides on its attribute modifier (AxeAttributeHandler), so it's already
		// counted here and shown in the tooltip.
		return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}
	
	/**
	 * Returns the attack knockback of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getTotalAttackKnockback(Player player, Entity target) {
		float attackKnockback = (float) player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		attackKnockback += enchantKnockbackBonus(player, target);

		if (PrideFeature.REVERT_KNOCKBACK.enabled()) {
			//1.8.9 knockback method. By default, it only counts the weapon's knockback, the
			// knockback enchants, and whether or not the attacker is sprinting.
			if (player.isSprinting()) {
				attackKnockback++;
			}
		} else {
			//Default knockback, here it is slightly different, because it plays a sound, and
			//only adds knockback if the player is sprinting and if his attack cooldown is high
			//enough. This will get bypassed if disableAttackCooldown is enabled, because
			//getAttackStrengthScale is overriden by PlayerMixin.
			boolean isAttackTimerOk = player.getAttackStrengthScale(0.5F) > 0.9F;
			if (player.isSprinting() && isAttackTimerOk) {
				player.level().playSound((Player) null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
				++attackKnockback;
			}
		}

		return attackKnockback;
	}

	// the enchant damage bonus (sharpness, smite, bane, impaling) comes from modifyDamage now,
	// server-side only (0 in client prediction).
	private static float enchantDamageBonus(Player player, Entity target, float baseDamage) {
		if (player.level() instanceof ServerLevel serverLevel) {
			return EnchantmentHelper.modifyDamage(serverLevel, player.getMainHandItem(), target,
					player.damageSources().playerAttack(player), baseDamage) - baseDamage;
		}
		return 0.0F;
	}

	// the Knockback enchant bonus goes through modifyKnockback (base 0), server-side only
	// (0 in client prediction).
	private static float enchantKnockbackBonus(Player player, Entity target) {
		if (player.level() instanceof ServerLevel serverLevel) {
			return EnchantmentHelper.modifyKnockback(serverLevel, player.getMainHandItem(), target,
					player.damageSources().playerAttack(player), 0.0F);
		}
		return 0.0F;
	}

	/**
	 * Returns the total attack damage of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getTotalDamage(Player player, Entity target, float baseDamage) {
		if (PrideFeature.REVERT_DAMAGE_LOGIC.enabled()) {
			// Calculate the damage based on mappings from 1.8.9.
			float additionalDamage = enchantDamageBonus(player, target, baseDamage);
			// heavy weapons keep the attack delay (reduced damage if you swing too early), even
			// in 1.8 combat, unless the cooldown for THIS weapon is explicitly disabled.
			if (isHeavyWeapon(player.getMainHandItem())
					&& !attackCooldownDisabledFor(player.getMainHandItem())) {
				float attackStrengthScale = player.getAttackStrengthScale(0.5F);
				baseDamage *= 0.2F + attackStrengthScale * attackStrengthScale * 0.8F;
				additionalDamage *= attackStrengthScale;
			}
			//Slight change in the Critical Hits detection, to use forge's damage modifier hook
			boolean isCriticalHit = isCritical(player, target);
			net.neoforged.neoforge.event.entity.player.CriticalHitEvent hitResult = net.neoforged.neoforge.common.CommonHooks.fireCriticalHit(player, target, isCriticalHit, isCriticalHit ? 1.5F : 1.0F);
				isCriticalHit = hitResult.isCriticalHit();
			if (isCriticalHit) {
				baseDamage *= hitResult.getDamageMultiplier();
			}
			return baseDamage + additionalDamage;
		} else {
			// Calculates the damage in the Vanilla way. This will provide weird results if
			// the attack cooldown is disabled, because damage will stay at maximum, and the
			// game was not balanced for that
			float additionalDamage = enchantDamageBonus(player, target, baseDamage);

			float attackStrengthScale = player.getAttackStrengthScale(0.5F);
			baseDamage *= 0.2F + attackStrengthScale * attackStrengthScale * 0.8F;
			additionalDamage *= attackStrengthScale;
			
			if (baseDamage > 0.0F || additionalDamage > 0.0F) {
				
			}
			
			//Checking Sweep
			boolean isCriticalHit = isCritical(player, target) && !player.isSprinting();
			net.neoforged.neoforge.event.entity.player.CriticalHitEvent hitResult = net.neoforged.neoforge.common.CommonHooks.fireCriticalHit(player, target, isCriticalHit, isCriticalHit ? 1.5F : 1.0F);
				isCriticalHit = hitResult.isCriticalHit();
			if (isCriticalHit) {
				baseDamage *= hitResult.getDamageMultiplier();
			}
			
			additionalDamage += baseDamage;
			return additionalDamage;
		}
	}
	
	public static boolean isCritical(Player player, Entity target) {
		boolean isAttackTimerOk = player.getAttackStrengthScale(0.5F) > 0.9F;			// getAttackStrengthScale should be bypassed by a Mixin in PlayerMixin using Pride's authority
		return isAttackTimerOk && player.fallDistance > 0.0F && !player.onGround()
				&& !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS)
				&& !player.isPassenger() && target instanceof LivingEntity;
	}

	public static boolean canAttacKSweep(Player player, Entity target, float attackStrengthScale) {
		if (PrideFeature.DISABLE_SWEEPING_ATTACKS.enabled()) return false;
		boolean isAttackTimerOk = attackStrengthScale > 0.9F;
		
		boolean sprintAndAttackTimerOk = player.isSprinting() && isAttackTimerOk;
		boolean canAttackSweep = false;
		// getKnownMovement() gives this tick's horizontal movement.
		double walkDistanceDelta = player.getKnownMovement().horizontalDistance();
		if (isAttackTimerOk && !isCritical(player, target) && !sprintAndAttackTimerOk && player.onGround()
				&& walkDistanceDelta < (double) player.getSpeed()) {
			ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
			canAttackSweep = itemstack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SWORD_SWEEP);
		}
		
		return canAttackSweep;
		
	}
	
	public static void playSounds(Player self, Entity targetEntity, boolean canSweep, boolean isCriticalHit) {
		boolean isAttackTimerOk = self.getAttackStrengthScale(0.5F) > 0.9F;

		if (isCriticalHit && PrideFeature.PLAY_CRIT_SOUNDS.enabled()) {
			self.level().playSound((Player) null, self.getX(), self.getY(), self.getZ(),
					SoundEvents.PLAYER_ATTACK_CRIT, self.getSoundSource(), 1.0F, 1.0F);
		}

		if (!isCriticalHit && !canSweep) {
			if (isAttackTimerOk && PrideFeature.PLAY_STRONG_HIT_SOUNDS.enabled()) {
				self.level().playSound((Player) null, self.getX(), self.getY(), self.getZ(),
						SoundEvents.PLAYER_ATTACK_STRONG, self.getSoundSource(), 1.0F, 1.0F);
			} else if (!isAttackTimerOk && PrideFeature.PLAY_WEAK_HIT_SOUNDS.enabled()) {
				self.level().playSound((Player) null, self.getX(), self.getY(), self.getZ(),
						SoundEvents.PLAYER_ATTACK_WEAK, self.getSoundSource(), 1.0F, 1.0F);
			}
		}
	}

	public static void knockbackOnHit(Player self, Entity targetEntity, float knockBack) {
		if (PrideFeature.REVERT_KNOCKBACK.enabled()) {
			if (knockBack > 0) {
				double dx = (double) (-Mth.sin(self.getYRot() * (float) Math.PI / 180.0F) * (float) knockBack * 0.5F);
				double dy = 0.1D;
				double dz = (double) (Mth.cos(self.getYRot() * (float) Math.PI / 180.0F) * (float) knockBack * 0.5F);
				targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(dx, dy, dz));
			}
		} else 
			if (targetEntity instanceof LivingEntity) {
				((LivingEntity) targetEntity).knockback((double) ((float) knockBack * 0.5F),
						(double) Mth.sin(self.getYRot() * ((float) Math.PI / 180F)),
						(double) (-Mth.cos(self.getYRot() * ((float) Math.PI / 180F))));
			} else {
				targetEntity.push(
						(double) (-Mth.sin(self.getYRot() * ((float) Math.PI / 180F))
								* (float) knockBack * 0.5F),
						0.1D, (double) (Mth.cos(self.getYRot() * ((float) Math.PI / 180F))
								* (float) knockBack * 0.5F));
			}
	}

	public static void foodExhaustion(Player self) {
		if (PrideFeature.REVERT_DAMAGE_LOGIC.enabled()) {
			self.causeFoodExhaustion(0.3F);
		} else {
			self.causeFoodExhaustion(0.1F);
		}
	}
	
}
