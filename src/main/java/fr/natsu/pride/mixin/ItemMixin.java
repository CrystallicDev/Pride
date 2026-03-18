package fr.natsu.pride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
        method = "use",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUse(Level level, Player player, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof SwordItem)) return;
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof ShieldItem) return;

        player.startUsingItem(hand);
        cir.setReturnValue(InteractionResultHolder.consume(stack));
    }
    
    @Overwrite
	/**
     * Allow the sword blocking to go for an infinite amount of time
     * @reason Required
     * @author Natsu91
     * {@link ShieldItem}
     * */
    public int getUseDuration(ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItem)) {
        	if (stack.getItem().isEdible()) {
                return stack.getFoodProperties(null).isFastFood() ? 16 : 32;
             } else {
                return 0;
             }
        }
        return 72000; // comme en 1.8, infini
    }
	
	
    @Overwrite
    /**
     * Allow the sword to block like in pre 1.9
     * @reason Required
     * @author Natsu91
     * */
    public UseAnim getUseAnimation(ItemStack stack) {
        if (!(stack.getItem() instanceof SwordItem)) return stack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
        return UseAnim.BLOCK;
    }
}