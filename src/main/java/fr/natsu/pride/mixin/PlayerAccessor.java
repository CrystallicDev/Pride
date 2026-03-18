package fr.natsu.pride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public interface PlayerAccessor {
	@Accessor
	int getAttackStrengthTicker();
}