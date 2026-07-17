package com.natsu.pride.features;

import java.util.EnumSet;
import java.util.Set;

import com.natsu.pride.config.ServerConfig;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Source de vérité unique des features, consultée par tous les mixins.
 * Trois modes :
 * - STANDALONE : solo ou serveur Forge avec Pride, la config serveur (synchronisée) décide ;
 * - PILOTED : serveur non-Forge qui pilote le mod via plugin messaging (protocole à venir) ;
 * - OFF : serveur sans Pride, tout est désactivé (comportement vanilla).
 */
public enum PrideFeature {

	REVERT_DAMAGE_LOGIC("revertDamageLogic", ServerConfig.REVERT_DAMAGE_LOGIC),
	REVERT_KNOCKBACK("revertKnockback", ServerConfig.REVERT_KNOCKBACK),
	ALLOW_SWORD_BLOCKING("allowSwordBlocking", ServerConfig.ALLOW_SWORD_BLOCKING),
	DISABLE_SWORD_ATTACK_COOLDOWN("disableSwordCooldown", ServerConfig.DISABLE_SWORD_ATTACK_COOLDOWN),
	DISABLE_SWEEPING_ATTACKS("disableSweepingAttacks", ServerConfig.DISABLE_SWEEPING_ATTACKS),
	DISABLE_AXE_ATTACK_COOLDOWN("disableAxesCooldown", ServerConfig.DISABLE_AXE_ATTACK_COOLDOWN),
	REMOVE_ARROW_DISPERSION("removeArrowDispersion", ServerConfig.REMOVE_ARROW_DISPERSION),
	REVERT_BOW("revertBow", ServerConfig.REVERT_BOW),
	REVERT_FISHING_ROD("revertFishingRod", ServerConfig.REVERT_FISHING_ROD),
	SHIELDS_ONLY_BLOCK_PROJECTILES("shieldsOnlyBlockProjectiles", ServerConfig.SHIELDS_ONLY_BLOCK_PROJECTILES),
	DISABLE_SWIMMING("disableSwimming", ServerConfig.DISABLE_SWIMMING),
	SLOW_WHILE_USING_ITEM("slowWhileUsingItem", ServerConfig.SLOW_WHILE_USING_ITEM),
	PLAY_CRIT_SOUNDS("playCriticalHitSounds", ServerConfig.PLAY_CRIT_SOUNDS),
	PLAY_STRONG_HIT_SOUNDS("playStrongHitSounds", ServerConfig.PLAY_STRONG_HIT_SOUNDS),
	PLAY_WEAK_HIT_SOUNDS("playWeakHitSounds", ServerConfig.PLAY_WEAK_HIT_SOUNDS),
	CHANGE_BODY_RENDER("changeBodyRender", ServerConfig.CHANGE_BODY_RENDER),
	REMOVE_BUCKET_ANIMATION("removeBucketAnimation", ServerConfig.REMOVE_BUCKET_ANIMATION),
	REMOVE_DROP_SWING("removeDropSwingAnimation", ServerConfig.REMOVE_DROP_SWING);

	/** Clé stable utilisée sur le réseau (= clé de config) : découple le protocole de l'ordre de l'enum. */
	private final String key;
	private final ForgeConfigSpec.BooleanValue standaloneValue;

	PrideFeature(String key, ForgeConfigSpec.BooleanValue standaloneValue) {
		this.key = key;
		this.standaloneValue = standaloneValue;
	}

	public String key() {
		return key;
	}

	public static PrideFeature byKey(String key) {
		for (PrideFeature f : values()) {
			if (f.key.equals(key)) return f;
		}
		return null;
	}

	public boolean enabled() {
		switch (mode()) {
			case STANDALONE: return standaloneValue.get();
			case PILOTED: return piloted.contains(this);
			default: return false;
		}
	}

	// --- État global ---

	public enum Mode { OFF, STANDALONE, PILOTED }

	private static final Set<PrideFeature> piloted = EnumSet.noneOf(PrideFeature.class);
	private static volatile boolean pilotedActive = false;
	private static volatile double pilotedBlockingReduction = 0.5d;

	public static Mode mode() {
		if (pilotedActive) return Mode.PILOTED;
		if (ServerConfig.loaded()) return Mode.STANDALONE;
		return Mode.OFF;
	}

	/** Vrai dès qu'un mode est actif (config chargée ou pilotage serveur). */
	public static boolean active() {
		return mode() != Mode.OFF;
	}

	public static double blockingDamageReduction() {
		return mode() == Mode.STANDALONE ? ServerConfig.BLOCKING_DAMAGE_REDUCTION.get() : pilotedBlockingReduction;
	}

	// --- Pilotage par un serveur non-Forge (rempli par le futur protocole plugin) ---

	public static void setPiloted(Set<PrideFeature> features, double blockingReduction) {
		piloted.clear();
		piloted.addAll(features);
		pilotedBlockingReduction = blockingReduction;
		pilotedActive = true;
	}

	public static void clearPiloted() {
		pilotedActive = false;
		piloted.clear();
	}

}
