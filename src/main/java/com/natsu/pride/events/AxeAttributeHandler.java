package com.natsu.pride.events;

import java.util.List;

import com.google.common.collect.Multimap;
import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Knocks 2 off the base damage of every axe through its attribute modifier.
 * Doing it on the attribute (rather than at hit time) also keeps the tooltip number
 * in sync with the real damage.
 *
 * <p>The event's modifiers are a {@code Multimap<Holder<Attribute>, AttributeModifier>};
 * the "base damage" entry is the one whose UUID is {@link Item#BASE_ATTACK_DAMAGE_UUID}.
 */
@EventBusSubscriber(modid = Pride.MODID)
public class AxeAttributeHandler {

	@SubscribeEvent
	public static void onItemAttributes(ItemAttributeModifierEvent event) {
		if (!PrideFeature.active()) return;
		if (!(event.getItemStack().getItem() instanceof AxeItem)) return;

		Multimap<Holder<Attribute>, AttributeModifier> modifiers = event.getModifiers();
		// defensive copy: removeModifier/addModifier mutate the multimap while we're iterating
		for (AttributeModifier modifier : List.copyOf(modifiers.get(Attributes.ATTACK_DAMAGE))) {
			if (modifier.id().equals(Item.BASE_ATTACK_DAMAGE_UUID)) {
				event.removeModifier(Attributes.ATTACK_DAMAGE, modifier);
				event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
						Item.BASE_ATTACK_DAMAGE_UUID, modifier.name(), modifier.amount() - 2.0D, modifier.operation()));
				break;
			}
		}
	}
}
