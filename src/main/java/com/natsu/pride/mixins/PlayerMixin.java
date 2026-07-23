package com.natsu.pride.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

// Stage 1 (port NeoForge) : seules les méthodes CLIENT/blocage. La réécriture complète de
// `attack` (combat 1.8.9) est différée au Stage 2 (voir port_stage2/, à re-porter depuis la 1.20.1).
@Mixin(value = Player.class, remap = false)
public abstract class PlayerMixin {

	private boolean isReducingParryDamage = false;

	// pas de swing en droppant depuis l'inventaire
	@WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;swing(Lnet/minecraft/world/InteractionHand;)V"))
	private void removeInventoryDropSwing(Player instance, InteractionHand hand, Operation<Void> original) {
		if (PrideFeature.REMOVE_DROP_SWING.enabled()) return;
		original.call(instance, hand);
	}

	// pas de nage (1.8.9)
	@Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
	private void preventSwimming(CallbackInfo ci) {
		if (!PrideFeature.DISABLE_SWIMMING.enabled()) return;
		((Player) (Object) this).setSwimming(false);
		ci.cancel();
	}

	/**
	 * Armes lourdes : hache, masse (1.21) et trident. Elles sont équilibrées AUTOUR du délai de
	 * coup (dégâts réduits si on frappe trop tôt), contrairement à l'épée qui doit pouvoir spammer
	 * comme en 1.8.9 — d'où deux options de config distinctes.
	 */
	@Unique
	private static boolean pride$isHeavyWeapon(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof AxeItem || item instanceof MaceItem || item instanceof TridentItem;
	}

	@Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
	public void getAttackStrengthScale(float f, CallbackInfoReturnable<Float> cir) {
		if (!PrideFeature.active()) return;
		Player self = (Player) (Object) this;
		boolean heavy = pride$isHeavyWeapon(self.getMainHandItem());
		if ((PrideFeature.DISABLE_AXE_ATTACK_COOLDOWN.enabled() && heavy) ||
			(PrideFeature.DISABLE_SWORD_ATTACK_COOLDOWN.enabled() && !heavy)) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
	private void reduceParryDamage(DamageSource source, float amount, CallbackInfo ci) {
		if (isReducingParryDamage) return;
		if (!PrideFeature.ALLOW_SWORD_BLOCKING.enabled()) return;

		Player self = (Player) (Object) this;

		// pas de isBlocking() : Forge/NeoForge le réserve aux items avec l'ability SHIELD_BLOCK
		if (!self.isUsingItem()) return;
		if (!(self.getUseItem().getItem() instanceof SwordItem)) return;
		if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) return;
		if (source.getEntity() == null) return;

		amount = (1.0F + amount) * (1 - (float) PrideFeature.blockingDamageReduction());

		isReducingParryDamage = true;
		((PlayerAccessor) (Object) self).invokeActuallyHurt(source, amount);
		isReducingParryDamage = false;

		ci.cancel();
	}

}
