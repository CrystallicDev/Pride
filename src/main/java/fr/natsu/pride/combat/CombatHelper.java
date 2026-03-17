package fr.natsu.pride.combat;

import fr.natsu.pride.Pride;
import fr.natsu.pride.config.PrideConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CombatHelper {

	
	/**
	 * Returns the base damage of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getBaseDamage(Player player) {
		ItemStack item = player.getMainHandItem();
		boolean isWeaponAxe = (item != null ? (item.getItem() instanceof AxeItem) : false);
		if (PrideConfig.isRevertDamageLogic() && isWeaponAxe) {
			//Nerfing axe damage by 2, should be enough to rebalance damage overall
			return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) - 2;
		}
		return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}
	
	/**
	 * Returns the attack knockback of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getTotalAttackKnockback(Player player) {
		ItemStack item = player.getMainHandItem();
		boolean isWeaponAxe = (item != null ? (item.getItem() instanceof AxeItem) : false);
		float base = (float) player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		boolean isAttackTimerOk = player.getAttackStrengthScale(0.5F) > 0.9F;			// getAttackStrengthScale sShould be bypassed by a Mixin in PlayerMixin using Pride's authority
		
		if (PrideConfig.isRevertKnockback()) {
			//1.8.9 knockback method. By default, it only counts the weapon's knockback, the
			// knockback enchants, and whether or not the attacker is sprinting.
			float attackKnockback = (float) player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
			attackKnockback += EnchantmentHelper.getKnockbackBonus(player);
			if (player.isSprinting()) { attackKnockback++; }
		} else {
			//Default knockback, here it is slightly different, because it plays a sound, and
			//only adds knockback if the player is sprinting and if his attack cooldown is high
			//enough. This will get bypassed is disableAttackCooldown is enabled, because getAttackStrengthValue will
			//be overriden when necessary.
			float attackKnockback = (float) player.getAttributeValue(Attributes.ATTACK_KNOCKBACK); 
			attackKnockback += EnchantmentHelper.getKnockbackBonus(player);
			if (player.isSprinting() && isAttackTimerOk) {
				player.level.playSound((Player) null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
				++attackKnockback;
			}
		}
		
		return base;
	}

	/**
	 * Returns the total attack damage of an {@link Player} depending on {@link Pride}'s config
	 * */
	public static float getTotalDamage(Player player, Entity target, float baseDamage) {
		if (PrideConfig.isRevertDamageLogic()) {
			// Calculate the damage based on mappings from 1.8.9. 
			float additionalDamage;
			if (target instanceof LivingEntity) {
				additionalDamage = EnchantmentHelper.getDamageBonus(player.getMainHandItem(),
						((LivingEntity) target).getMobType());
			} else {
				additionalDamage = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
			}
			//Slight change in the Critical Hits detection, to use forge's damage modifier hook
			boolean isCriticalHit = isCritical(player, target);
			net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks
					.getCriticalHit(player, target, isCriticalHit, isCriticalHit ? 1.5F : 1.0F);
			isCriticalHit = hitResult != null;
			if (isCriticalHit) {
				baseDamage *= hitResult.getDamageModifier();
			}
			return baseDamage + additionalDamage;
		} else {
			// Calculates the damage in the Vanilla way. This will provide weird results if
			// the attack cooldown is disabled, because damage will stay at maximum, and the
			// game was not balanced for that
			float additionalDamage;
			if (target instanceof LivingEntity) {
				additionalDamage = EnchantmentHelper.getDamageBonus(player.getMainHandItem(),
						((LivingEntity) target).getMobType());
			} else {
				additionalDamage = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
			}

			float attackStrengthScale = player.getAttackStrengthScale(0.5F);
			baseDamage *= 0.2F + attackStrengthScale * attackStrengthScale * 0.8F;
			additionalDamage *= attackStrengthScale;
			
			if (baseDamage > 0.0F || additionalDamage > 0.0F) {
				
			}
			
			//Checking Sweep
			boolean isCriticalHit = isCritical(player, target) && !player.isSprinting();
			net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks
					.getCriticalHit(player, target, isCriticalHit, isCriticalHit ? 1.5F : 1.0F);
			isCriticalHit = hitResult != null;
			if (isCriticalHit) {
				baseDamage *= hitResult.getDamageModifier();
			}
			
			additionalDamage += baseDamage;
			return additionalDamage;
		}
	}
	
	public static boolean isCritical(Player player, Entity target) {
		boolean isAttackTimerOk = player.getAttackStrengthScale(0.5F) > 0.9F;			// getAttackStrengthScale sShould be bypassed by a Mixin in PlayerMixin using Pride's authority
		return isAttackTimerOk && player.fallDistance > 0.0F && !player.isOnGround()
				&& !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS)
				&& !player.isPassenger() && target instanceof LivingEntity;
	}

	public static boolean canAttacKSweep(Player player, Entity target, float attackStrengthScale) {
		if (PrideConfig.isNoSweeping()) return false;
		boolean isAttackTimerOk = attackStrengthScale > 0.9F;
		
		boolean sprintAndAttackTimerOk = player.isSprinting() && isAttackTimerOk;
		boolean canAttackSweep = false;
		double walkDistanceDelta = (double) (player.walkDist - player.walkDistO);
		if (isAttackTimerOk && !isCritical(player, target) && !sprintAndAttackTimerOk && player.isOnGround()
				&& walkDistanceDelta < (double) player.getSpeed()) {
			ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
			canAttackSweep = itemstack.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_SWEEP);
		}
		
		return canAttackSweep;
		
	}
	
	public static void playSounds(Player self, Entity targetEntity, boolean canSweep, boolean isCriticalHit) {
		boolean isAttackTimerOk = self.getAttackStrengthScale(0.5F) > 0.9F;
		
		if (isCriticalHit && PrideConfig.isPlayCritSounds()) {
			self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
					SoundEvents.PLAYER_ATTACK_CRIT, self.getSoundSource(), 1.0F, 1.0F);
			self.crit(targetEntity);
		}

		if (!isCriticalHit && !canSweep) {
			if (isAttackTimerOk && PrideConfig.isPlayStrongHitsSounds()) {
				self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
						SoundEvents.PLAYER_ATTACK_STRONG, self.getSoundSource(), 1.0F, 1.0F);
			} else if (PrideConfig.isPlayWeakHitsSounds()){
				self.level.playSound((Player) null, self.getX(), self.getY(), self.getZ(),
						SoundEvents.PLAYER_ATTACK_WEAK, self.getSoundSource(), 1.0F, 1.0F);
			}
		}
	}

	public static void knockbackOnHit(Player self, Entity targetEntity, float knockBack) {
		if (PrideConfig.isRevertKnockback()) {
			
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
		if (PrideConfig.isRevertDamageLogic()) {
			self.causeFoodExhaustion(0.3F);
		} else {
			self.causeFoodExhaustion(0.1F);
		}
	}
	
}
