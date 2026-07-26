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
 * Réduit de 2 les dégâts de base de toute hache, via son modificateur d'attribut.
 * Passer par l'attribut (plutôt qu'au moment du coup) met à jour la valeur affichée
 * dans le tooltip en plus des dégâts réels.
 *
 * <p>1.21 : les modificateurs d'items sont le composant {@link ItemAttributeModifiers} ; l'entrée
 * "dégâts de base" est identifiée par {@link Item#BASE_ATTACK_DAMAGE_ID} (une ResourceLocation).
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
