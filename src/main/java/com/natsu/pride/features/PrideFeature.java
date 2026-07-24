package com.natsu.pride.features;

import java.util.EnumSet;
import java.util.Set;

import com.natsu.pride.config.ServerConfig;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Source de vérité unique des features, consultée par tous les mixins.
 * Trois modes :
 * - STANDALONE : solo ou serveur Forge avec Pride, la config serveur (synchronisée) décide ;
 * - PILOTED : serveur non-Forge qui pilote le mod via plugin messaging (protocole à venir) ;
 * - OFF : serveur sans Pride, tout est désactivé (comportement vanilla).
 */
public enum PrideFeature {

	// SERVER = effet autoritaire côté serveur (dégâts, knockback, entités, sons du flux d'attaque) :
	//          en mode piloté c'est le plugin qui le fait, le client ne l'applique PAS (sinon desync).
	// CLIENT = rendu / animation / mouvement-input local : le client doit le faire.
	// BOTH   = partie client (pose/anim) + partie serveur (ex. réduction de dégâts en blocage).
	REVERT_DAMAGE_LOGIC("revertDamageLogic", Side.SERVER, ServerConfig.REVERT_DAMAGE_LOGIC),
	REVERT_KNOCKBACK("revertKnockback", Side.SERVER, ServerConfig.REVERT_KNOCKBACK),
	ALLOW_SWORD_BLOCKING("allowSwordBlocking", Side.BOTH, ServerConfig.ALLOW_SWORD_BLOCKING),
	DISABLE_SWORD_ATTACK_COOLDOWN("disableSwordCooldown", Side.SERVER, ServerConfig.DISABLE_SWORD_ATTACK_COOLDOWN),
	DISABLE_SWEEPING_ATTACKS("disableSweepingAttacks", Side.SERVER, ServerConfig.DISABLE_SWEEPING_ATTACKS),
	DISABLE_AXE_ATTACK_COOLDOWN("disableAxesCooldown", Side.SERVER, ServerConfig.DISABLE_AXE_ATTACK_COOLDOWN),
	REMOVE_ARROW_DISPERSION("removeArrowDispersion", Side.SERVER, ServerConfig.REMOVE_ARROW_DISPERSION),
	REVERT_BOW("revertBow", Side.SERVER, ServerConfig.REVERT_BOW),
	REVERT_FISHING_ROD("revertFishingRod", Side.SERVER, ServerConfig.REVERT_FISHING_ROD),
	SHIELDS_ONLY_BLOCK_PROJECTILES("shieldsOnlyBlockProjectiles", Side.SERVER, ServerConfig.SHIELDS_ONLY_BLOCK_PROJECTILES),
	DISABLE_SWIMMING("disableSwimming", Side.CLIENT, ServerConfig.DISABLE_SWIMMING),
	SLOW_WHILE_USING_ITEM("slowWhileUsingItem", Side.CLIENT, ServerConfig.SLOW_WHILE_USING_ITEM),
	// Placement de bloc = autoritaire serveur (le plugin s'en charge en mode piloté).
	WATERLOG_ONLY_WHEN_SNEAKING("waterlogOnlyWhenSneaking", Side.SERVER, ServerConfig.WATERLOG_ONLY_WHEN_SNEAKING),
	PLAY_CRIT_SOUNDS("playCriticalHitSounds", Side.SERVER, ServerConfig.PLAY_CRIT_SOUNDS),
	PLAY_STRONG_HIT_SOUNDS("playStrongHitSounds", Side.SERVER, ServerConfig.PLAY_STRONG_HIT_SOUNDS),
	PLAY_WEAK_HIT_SOUNDS("playWeakHitSounds", Side.SERVER, ServerConfig.PLAY_WEAK_HIT_SOUNDS),
	CHANGE_BODY_RENDER("changeBodyRender", Side.CLIENT, ServerConfig.CHANGE_BODY_RENDER),
	REMOVE_BUCKET_ANIMATION("removeBucketAnimation", Side.CLIENT, ServerConfig.REMOVE_BUCKET_ANIMATION),
	REMOVE_DROP_SWING("removeDropSwingAnimation", Side.CLIENT, ServerConfig.REMOVE_DROP_SWING);

	public enum Side { CLIENT, SERVER, BOTH }

	/** Clé stable utilisée sur le réseau (= clé de config) : découple le protocole de l'ordre de l'enum. */
	private final String key;
	private final Side side;
	private final ModConfigSpec.BooleanValue standaloneValue;

	PrideFeature(String key, Side side, ModConfigSpec.BooleanValue standaloneValue) {
		this.key = key;
		this.side = side;
		this.standaloneValue = standaloneValue;
	}

	public String key() {
		return key;
	}

	public Side side() {
		return side;
	}

	/** Une feature est pilotable par un serveur distant si elle a une partie client (CLIENT ou BOTH). */
	public boolean pilotable() {
		return side != Side.SERVER;
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
			// En piloté, on n'applique jamais les features serveur : c'est le plugin qui les gère.
			case PILOTED: return pilotable() && piloted.contains(this);
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
