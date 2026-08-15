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
	public static final ForgeConfigSpec.BooleanValue DISABLE_TRIDENT_ATTACK_COOLDOWN;
	public static final ForgeConfigSpec.BooleanValue REMOVE_ARROW_DISPERSION;
	public static final ForgeConfigSpec.BooleanValue REVERT_BOW;
	public static final ForgeConfigSpec.BooleanValue REVERT_FISHING_ROD;
	public static final ForgeConfigSpec.BooleanValue SHIELDS_ONLY_BLOCK_PROJECTILES;
	public static final ForgeConfigSpec.BooleanValue DISABLE_SWIMMING;
	public static final ForgeConfigSpec.BooleanValue SLOW_WHILE_USING_ITEM;
	public static final ForgeConfigSpec.BooleanValue WATERLOG_ONLY_WHEN_SNEAKING;
	public static final ForgeConfigSpec.BooleanValue PLAY_CRIT_SOUNDS;
	public static final ForgeConfigSpec.BooleanValue PLAY_STRONG_HIT_SOUNDS;
	public static final ForgeConfigSpec.BooleanValue PLAY_WEAK_HIT_SOUNDS;

	public static final ForgeConfigSpec.BooleanValue CHANGE_BODY_RENDER;
	public static final ForgeConfigSpec.BooleanValue REMOVE_BUCKET_ANIMATION;
	public static final ForgeConfigSpec.BooleanValue REMOVE_DROP_SWING;

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("combat");
		REVERT_DAMAGE_LOGIC = builder
				.comment("Revert the damage calculation logic to 1.8.9")
				.define("revertDamageLogic", true);
		REVERT_KNOCKBACK = builder
				.comment("Revert the knockback logic to 1.8.9")
				.define("revertKnockback", true);
		ALLOW_SWORD_BLOCKING = builder
				.comment("Allow the players to block damage by holding right click with a sword\n"
						+ "when their offhand is empty.")
				.define("allowSwordBlocking", true);
		BLOCKING_DAMAGE_REDUCTION = builder
				.comment("The damage reduction (in percentage) applied when blocking damage with a sword.")
				.defineInRange("blockingDamageReduction", 0.5d, 0.0d, 1.0d);
		DISABLE_SWORD_ATTACK_COOLDOWN = builder
				.comment("Disable the attack cooldown for swords, and any item without its own toggle below.")
				.define("disableSwordCooldown", true);
		DISABLE_SWEEPING_ATTACKS = builder
				.comment("Disable the sweeping attacks, their particles and sounds. This also\n"
						+ "disables the Sweeping Edge enchantment.")
				.define("disableSweepingAttacks", true);
		DISABLE_AXE_ATTACK_COOLDOWN = builder
				.comment("Disable the attack cooldown for axes. To keep them balanced, any axe's\n"
						+ "base damage is reduced by 2 when this is enabled.")
				.define("disableAxeCooldown", true);
		DISABLE_TRIDENT_ATTACK_COOLDOWN = builder
				.comment("Disable the attack cooldown for tridents.")
				.define("disableTridentCooldown", true);
		REMOVE_ARROW_DISPERSION = builder
				.comment("Remove the arrow dispersion (inaccuracy) of bows, like in 1.8.9.")
				.define("removeArrowDispersion", true);
		REVERT_BOW = builder
				.comment("Revert arrows to 1.8.9: they no longer inherit the shooter's velocity,\n"
						+ "and can hit their own shooter after a short delay (enables bow boosting).")
				.define("revertBow", true);
		REVERT_FISHING_ROD = builder
				.comment("Revert the fishing rod to the 1.8.9 physics and logic.")
				.define("revertFishingRod", true);
		SHIELDS_ONLY_BLOCK_PROJECTILES = builder
				.comment("Make shields only block projectiles : melee hits go through.")
				.define("shieldsOnlyBlockProjectiles", false);
		DISABLE_SWIMMING = builder
				.comment("Revert swimming to 1.8.9: no horizontal fast-swim pose, and a constant\n"
						+ "water speed (no sprint boost when submerged).")
				.define("disableSwimming", true);
		SLOW_WHILE_USING_ITEM = builder
				.comment("Cancel sprint while using an item (eating, drinking), like in 1.8.9, so the\n"
						+ "player can't keep moving at sprint speed while eating.")
				.define("slowWhileUsingItem", true);
		WATERLOG_ONLY_WHEN_SNEAKING = builder
				.comment("Only waterlog a block (slab, stairs...) when the player is sneaking. Without sneaking,"
						+ " the water is placed as a real source on the clicked face, like in 1.8.9 where"
						+ " waterlogging did not exist - this is what makes MLG clutches work.")
				.define("waterlogOnlyWhenSneaking", true);
		builder.pop();

		builder.push("sounds");
		PLAY_CRIT_SOUNDS = builder
				.comment("Play the vanilla 1.9+ critical hit sound.")
				.define("playCriticalHitSounds", false);
		PLAY_STRONG_HIT_SOUNDS = builder
				.comment("Play the vanilla 1.9+ strong hit sound.")
				.define("playStrongHitSounds", false);
		PLAY_WEAK_HIT_SOUNDS = builder
				.comment("Play the vanilla 1.9+ weak / no-damage hit sounds.")
				.define("playWeakHitSounds", false);
		builder.pop();

		builder.push("render");
		CHANGE_BODY_RENDER = builder
				.comment("Change the player body rotation to match that of 1.8.9")
				.define("changeBodyRender", true);
		REMOVE_BUCKET_ANIMATION = builder
				.comment("Remove the re-equip animation (item going down) when a bucket is filled\n"
						+ "or emptied, like in 1.8.9.")
				.define("removeBucketAnimation", true);
		REMOVE_DROP_SWING = builder
				.comment("Remove the arm swing animation when dropping an item, like in 1.8.9.")
				.define("removeDropSwingAnimation", true);
		builder.pop();

		SPEC = builder.build();
	}

	/** The SERVER config isn't loaded in the menus, so check this before any .get() in a mixin. */
	public static boolean loaded() {
		return SPEC.isLoaded();
	}

}
