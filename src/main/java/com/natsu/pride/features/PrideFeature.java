package com.natsu.pride.features;

import java.util.EnumSet;
import java.util.Set;

import com.natsu.pride.config.ServerConfig;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The single source of truth for features, every mixin asks it.
 * Three modes:
 * - STANDALONE: singleplayer or a Forge server with Pride, the (synced) server config decides;
 * - PILOTED: a non-Forge server driving the mod through plugin messaging (protocol coming later);
 * - OFF: a server without Pride, everything is off (vanilla behaviour).
 */
public enum PrideFeature {

	// SERVER = server-authoritative effect (damage, knockback, entities, attack-flow sounds):
	//          when piloted the plugin does it, the client does NOT apply it (otherwise desync).
	// CLIENT = local rendering / animation / movement-input: the client has to do it.
	// BOTH   = a client part (pose/anim) plus a server part (e.g. blocking damage reduction).
	REVERT_DAMAGE_LOGIC("revertDamageLogic", Side.SERVER, ServerConfig.REVERT_DAMAGE_LOGIC),
	REVERT_KNOCKBACK("revertKnockback", Side.SERVER, ServerConfig.REVERT_KNOCKBACK),
	ALLOW_SWORD_BLOCKING("allowSwordBlocking", Side.BOTH, ServerConfig.ALLOW_SWORD_BLOCKING),
	DISABLE_SWORD_ATTACK_COOLDOWN("disableSwordCooldown", Side.SERVER, ServerConfig.DISABLE_SWORD_ATTACK_COOLDOWN),
	DISABLE_SWEEPING_ATTACKS("disableSweepingAttacks", Side.SERVER, ServerConfig.DISABLE_SWEEPING_ATTACKS),
	DISABLE_AXE_ATTACK_COOLDOWN("disableAxeCooldown", Side.SERVER, ServerConfig.DISABLE_AXE_ATTACK_COOLDOWN),
	DISABLE_MACE_ATTACK_COOLDOWN("disableMaceCooldown", Side.SERVER, ServerConfig.DISABLE_MACE_ATTACK_COOLDOWN),
	DISABLE_TRIDENT_ATTACK_COOLDOWN("disableTridentCooldown", Side.SERVER, ServerConfig.DISABLE_TRIDENT_ATTACK_COOLDOWN),
	DISABLE_SPEAR_ATTACK_COOLDOWN("disableSpearCooldown", Side.SERVER, ServerConfig.DISABLE_SPEAR_ATTACK_COOLDOWN),
	REMOVE_ARROW_DISPERSION("removeArrowDispersion", Side.SERVER, ServerConfig.REMOVE_ARROW_DISPERSION),
	REVERT_BOW("revertBow", Side.SERVER, ServerConfig.REVERT_BOW),
	REVERT_FISHING_ROD("revertFishingRod", Side.SERVER, ServerConfig.REVERT_FISHING_ROD),
	SHIELDS_ONLY_BLOCK_PROJECTILES("shieldsOnlyBlockProjectiles", Side.SERVER, ServerConfig.SHIELDS_ONLY_BLOCK_PROJECTILES),
	DISABLE_SWIMMING("disableSwimming", Side.CLIENT, ServerConfig.DISABLE_SWIMMING),
	SLOW_WHILE_USING_ITEM("slowWhileUsingItem", Side.CLIENT, ServerConfig.SLOW_WHILE_USING_ITEM),
	// block placement is server-authoritative (the plugin handles it when piloted).
	WATERLOG_ONLY_WHEN_SNEAKING("waterlogOnlyWhenSneaking", Side.SERVER, ServerConfig.WATERLOG_ONLY_WHEN_SNEAKING),
	PLAY_CRIT_SOUNDS("playCriticalHitSounds", Side.SERVER, ServerConfig.PLAY_CRIT_SOUNDS),
	PLAY_STRONG_HIT_SOUNDS("playStrongHitSounds", Side.SERVER, ServerConfig.PLAY_STRONG_HIT_SOUNDS),
	PLAY_WEAK_HIT_SOUNDS("playWeakHitSounds", Side.SERVER, ServerConfig.PLAY_WEAK_HIT_SOUNDS),
	CHANGE_BODY_RENDER("changeBodyRender", Side.CLIENT, ServerConfig.CHANGE_BODY_RENDER),
	REMOVE_BUCKET_ANIMATION("removeBucketAnimation", Side.CLIENT, ServerConfig.REMOVE_BUCKET_ANIMATION),
	REMOVE_DROP_SWING("removeDropSwingAnimation", Side.CLIENT, ServerConfig.REMOVE_DROP_SWING);

	public enum Side { CLIENT, SERVER, BOTH }

	/** Stable key used on the wire (= the config key): keeps the protocol independent of enum order. */
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

	/** A feature can be driven by a remote server if it has a client part (CLIENT or BOTH). */
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
			// when piloted we never apply server features, the plugin takes care of those.
			case PILOTED: return pilotable() && piloted.contains(this);
			default: return false;
		}
	}

	// --- global state ---

	public enum Mode { OFF, STANDALONE, PILOTED }

	private static final Set<PrideFeature> piloted = EnumSet.noneOf(PrideFeature.class);
	private static volatile boolean pilotedActive = false;
	private static volatile double pilotedBlockingReduction = 0.5d;

	public static Mode mode() {
		if (pilotedActive) return Mode.PILOTED;
		if (ServerConfig.loaded()) return Mode.STANDALONE;
		return Mode.OFF;
	}

	/** True as soon as a mode is active (config loaded or being piloted by a server). */
	public static boolean active() {
		return mode() != Mode.OFF;
	}

	public static double blockingDamageReduction() {
		return mode() == Mode.STANDALONE ? ServerConfig.BLOCKING_DAMAGE_REDUCTION.get() : pilotedBlockingReduction;
	}

	// --- driven by a non-Forge server (filled in by the upcoming plugin protocol) ---

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
