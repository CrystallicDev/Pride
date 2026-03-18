package fr.natsu.pride.mixin;

import java.lang.System.Logger.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;

@Mixin(SwordItem.class)
public class SwordItemMixin {

	
	
    
    
    //@Overwrite
    /**
     * Slow down the player when blocking
     * @reason Required
     * @author Natsu91
     * */
    /*public float getUsingSpeedModifier(ItemStack stack) {
        return 0.2F;
    }*/
}