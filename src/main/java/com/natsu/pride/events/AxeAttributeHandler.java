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
 * Réduit de 2 les dégâts de base de toute hache, via son modificateur d'attribut.
 * Passer par l'attribut (plutôt qu'au moment du coup) met à jour la valeur affichée
 * dans le tooltip en plus des dégâts réels.
 *
 * <p>1.20.6 : les modificateurs de l'event sont un {@code Multimap<Holder<Attribute>, AttributeModifier>} ;
 * l'entrée "dégâts de base" est identifiée par l'UUID {@link Item#BASE_ATTACK_DAMAGE_UUID}.
 */
@EventBusSubscriber(modid = Pride.MODID)
public class AxeAttributeHandler {

	@SubscribeEvent
	public static void onItemAttributes(ItemAttributeModifierEvent event) {
		if (!PrideFeature.active()) return;
		if (!(event.getItemStack().getItem() instanceof AxeItem)) return;

		Multimap<Holder<Attribute>, AttributeModifier> modifiers = event.getModifiers();
		// copie défensive : removeModifier/addModifier modifient la multimap pendant l'itération
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
