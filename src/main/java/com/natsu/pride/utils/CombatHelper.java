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
	 * Armes lourdes (hache, masse, trident, lance) : elles sont équilibrées AUTOUR du délai de coup
	 * (dégâts réduits si on frappe trop tôt), contrairement à l'épée qui doit pouvoir spammer comme en
	 * 1.8.9 — d'où deux options de config distinctes. Détection par tag (hache/lance sont data-driven
	 * en 26.1, plus de classe) + classe pour masse/trident qui n'ont pas de tag dédié.
	 */
	public static boolean isHeavyWeapon(ItemStack stack) {
		return stack.is(ItemTags.AXES) || stack.is(ItemTags.SPEARS)
				|| stack.getItem() instanceof MaceItem || stack.getItem() instanceof TridentItem;
	}


	/**
	 * Returns the base damage of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getBaseDamage(Player player) {
		// Le -2 des haches est porté par leur modificateur d'attribut (AxeAttributeHandler),
		// donc déjà pris en compte ici et affiché dans le tooltip.
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

	// 1.21 : getDamageBonus(item, MobType) supprimé. Le bonus de dégâts d'enchant (sharpness, smite,
	// bane, impaling) se calcule via modifyDamage, côté serveur uniquement (0 en prédiction client).
	private static float enchantDamageBonus(Player player, Entity target, float baseDamage) {
		if (player.level() instanceof ServerLevel serverLevel) {
			return EnchantmentHelper.modifyDamage(serverLevel, player.getMainHandItem(), target,
					player.damageSources().playerAttack(player), baseDamage) - baseDamage;
		}
		return 0.0F;
	}

	// 1.21 : getKnockbackBonus(player) supprimé. Le bonus de l'enchant Knockback passe par
	// modifyKnockback (base 0), côté serveur uniquement (0 en prédiction client).
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
			// Les armes lourdes gardent le délai des coups (dégâts réduits si on frappe trop tôt),
			// même en combat 1.8 — sauf si leur cooldown est explicitement désactivé.
			if (isHeavyWeapon(player.getMainHandItem())
					&& !PrideFeature.DISABLE_AXE_ATTACK_COOLDOWN.enabled()) {
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
		// 26.1 : walkDist/walkDistO retirés. getKnownMovement() donne le déplacement horizontal du tick.
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
