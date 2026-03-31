package com.natsu.pride.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

	public static final ForgeConfigSpec SPEC;
	
	public static final ForgeConfigSpec.BooleanValue REVERT_DAMAGE_LOGIC;
	public static final ForgeConfigSpec.BooleanValue REVERT_KNOCKBACK;
	public static final ForgeConfigSpec.BooleanValue ALLOW_SWORD_BLOCKING;
	public static final ForgeConfigSpec.DoubleValue BLOCKING_DAMAGE_REDUCTION;
	public static final ForgeConfigSpec.BooleanValue DISABLE_SWORD_ATTACK_COOLDOWN;
	public static final ForgeConfigSpec.BooleanValue DISABLE_SWEEPING_ATTACKS;
	public static final ForgeConfigSpec.BooleanValue DISABLE_AXE_ATTACK_COOLDOWN;
	public static final ForgeConfigSpec.BooleanValue REVERT_FISHING_ROD;
	public static final ForgeConfigSpec.BooleanValue SHIELDS_ONLY_BLOCK_PROJECTILES;
	public static final ForgeConfigSpec.BooleanValue PLAY_CRIT_SOUNDS;
	public static final ForgeConfigSpec.BooleanValue PLAY_STRONG_HIT_SOUNDS;
	public static final ForgeConfigSpec.BooleanValue PLAY_WEAK_HIT_SOUNDS;

	public static final ForgeConfigSpec.BooleanValue CHANGE_BODY_RENDER;
	
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("# Enchanting Table Stages");
		REVERT_DAMAGE_LOGIC = builder
				.comment("Revert the damage calculation logic to 1.8.9")
				.define("revertDamageLogic", true);
		REVERT_KNOCKBACK = builder
				.comment("Revert the knockback logic to 1.8.9")
				.define("revertKnockback", true);
		ALLOW_SWORD_BLOCKING = builder
				.comment("Allow the players to block damage by right clicking with a sword")
				.define("allowSwordBlocking", true);
		BLOCKING_DAMAGE_REDUCTION = builder
				.comment("The damage reduction (in percentage) of the damage reduction when blocking damage with a sword.")
				.defineInRange("blockingDamageReduction", 0.5f, 0.0f, 1.0f);
		DISABLE_SWORD_ATTACK_COOLDOWN = builder
				.comment("Disable the attack cooldown for swords.")
				.define("disableSwordCooldown", true);
		DISABLE_SWEEPING_ATTACKS = builder
				.comment("Disable the sweeping attacks")
				.define("disableSweepingAttacks", true);
		DISABLE_AXE_ATTACK_COOLDOWN = builder
				.comment("Disable the attack cooldown for axes.")
				.define("disableAxesCooldown", true);
		REVERT_FISHING_ROD = builder
				.comment("Revert the fishing rod to the 1.8.9 physics and logic.")
				.define("disableSwordCooldown", true);
		SHIELDS_ONLY_BLOCK_PROJECTILES = builder
				.comment("Disable the attack cooldown for swords.")
				.define("disableSwordCooldown", false);
		PLAY_CRIT_SOUNDS = builder
				.comment("Disable the sound for critical hits.")
				.define("disableCriticalHitSounds", true);
		PLAY_STRONG_HIT_SOUNDS = builder
				.comment("Disable the sound for strong hits.")
				.define("disableStrongHitSounds", true);
		PLAY_WEAK_HIT_SOUNDS = builder
				.comment("Disable the sound for weak hits.")
				.define("disableWeakHitSounds", true);
		
		CHANGE_BODY_RENDER = builder
				.comment("Change the player body rotation to match that of 1.8.9")
				.define("changeBodyRender", true);

		builder.pop();
		SPEC = builder.build();
	}

	
	
}
