package fr.natsu.pride.config;

public class PrideConfig {

	public static boolean revertDamageLogic = false;
	public static boolean revertKnockback = false;
	public static boolean allowSwordBlocking = false;
	public static float swordBlockingDamageReduction = 0.5f;
	public static boolean disableAttackCooldownForSword = false;
	public static boolean noSweeping = false;
	public static boolean disableAttackCooldownForAxe = false;
	public static boolean revertFishingRod = false;
	public static boolean shieldsOnlyBlockProjectiles = false;
	public static boolean isPlayCritSounds = false;
	public static boolean isPlayStrongHitsSounds = false;
	public static boolean isPlayWeakHitsSounds = false;
	
	
	public static boolean isPlayCritSounds() {
		return isPlayCritSounds;
	}
	public static void setPlayCritSounds(boolean isPlayCritSounds) {
		PrideConfig.isPlayCritSounds = isPlayCritSounds;
	}
	public static boolean isPlayStrongHitsSounds() {
		return isPlayStrongHitsSounds;
	}
	public static void setPlayStrongHitsSounds(boolean isPlayStrongHitsSounds) {
		PrideConfig.isPlayStrongHitsSounds = isPlayStrongHitsSounds;
	}
	public static boolean isPlayWeakHitsSounds() {
		return isPlayWeakHitsSounds;
	}
	public static void setPlayWeakHitsSounds(boolean isPlayWeakHitsSounds) {
		PrideConfig.isPlayWeakHitsSounds = isPlayWeakHitsSounds;
	}
	public static boolean isRevertKnockback() {
		return revertKnockback;
	}
	public static void setRevertKnockback(boolean revertKnockback) {
		PrideConfig.revertKnockback = revertKnockback;
	}
	public static boolean isNoSweeping() {
		return noSweeping;
	}
	public static void setNoSweeping(boolean noSweeping) {
		PrideConfig.noSweeping = noSweeping;
	}
	public static boolean isRevertDamageLogic() {
		return revertDamageLogic;
	}
	public static void setRevertDamageLogic(boolean revertDamageLogic) {
		PrideConfig.revertDamageLogic = revertDamageLogic;
	}
	public static boolean isAllowSwordBlocking() {
		return allowSwordBlocking;
	}
	public static void setAllowSwordBlocking(boolean allowSwordBlocking) {
		PrideConfig.allowSwordBlocking = allowSwordBlocking;
	}
	public static float getSwordBlockingDamageReduction() {
		return swordBlockingDamageReduction;
	}
	public static void setSwordBlockingDamageReduction(float swordBlockingDamageReduction) {
		PrideConfig.swordBlockingDamageReduction = swordBlockingDamageReduction;
	}
	public static boolean isDisableAttackCooldownForSword() {
		return disableAttackCooldownForSword;
	}
	public static void setDisableAttackCooldownForSword(boolean disableAttackCooldownForSword) {
		PrideConfig.disableAttackCooldownForSword = disableAttackCooldownForSword;
	}
	public static boolean isDisableAttackCooldownForAxe() {
		return disableAttackCooldownForAxe;
	}
	public static void setDisableAttackCooldownForAxe(boolean disableAttackCooldownForAxe) {
		PrideConfig.disableAttackCooldownForAxe = disableAttackCooldownForAxe;
	}
	public static boolean isRevertFishingRod() {
		return revertFishingRod;
	}
	public static void setRevertFishingRod(boolean revertFishingRod) {
		PrideConfig.revertFishingRod = revertFishingRod;
	}
	public static boolean isShieldsOnlyBlockProjectiles() {
		return shieldsOnlyBlockProjectiles;
	}
	public static void setShieldsOnlyBlockProjectiles(boolean shieldsOnlyBlockProjectiles) {
		PrideConfig.shieldsOnlyBlockProjectiles = shieldsOnlyBlockProjectiles;
	}
	
	
}
