package com.natsu.pride.events;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Knocks 2 off the base damage of every axe through its attribute modifier.
 * Doing it on the attribute (rather than at hit time) also keeps the tooltip number
 * in sync with the real damage.
 *
 * <p>Item modifiers live in the {@link ItemAttributeModifiers} component; the "base damage" entry
 * is the one identified by {@link Item#BASE_ATTACK_DAMAGE_ID} (a ResourceLocation).
 */
@EventBusSubscriber(modid = Pride.MODID)
public class AxeAttributeHandler {

	@SubscribeEvent
	public static void onItemAttributes(ItemAttributeModifierEvent event) {
		if (!PrideFeature.active()) return;
		if (!(event.getItemStack().getItem() instanceof AxeItem)) return;

		for (ItemAttributeModifiers.Entry entry : event.getModifiers()) {
			if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)
					&& entry.modifier().is(Item.BASE_ATTACK_DAMAGE_ID)) {
				AttributeModifier old = entry.modifier();
				event.replaceModifier(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, old.amount() - 2.0D, old.operation()),
						entry.slot());
				break;
			}
		}
	}
}
