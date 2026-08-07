package com.natsu.pride.events;

import java.util.UUID;

import com.natsu.pride.Pride;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraftforge.event.GatherComponentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Réduit de 2 les dégâts de base de toute hache, via son composant de modificateurs par défaut.
 * Passer par l'attribut (plutôt qu'au moment du coup) met à jour la valeur affichée
 * dans le tooltip en plus des dégâts réels.
 *
 * <p>1.20.6 : {@code ItemAttributeModifierEvent} n'existe plus côté Forge (les modificateurs sont le
 * composant {@link ItemAttributeModifiers}). On patche {@link DataComponents#ATTRIBUTE_MODIFIERS} à
 * l'enregistrement de l'item via {@link GatherComponentsEvent.Item} (bus MOD).
 */
// GatherComponentsEvent est diffusé sur le bus FORGE (jeu), pas MOD ; il est posté "lazily"
// au premier accès aux composants d'un item (après le chargement), donc les items vanilla sont bien vus.
@Mod.EventBusSubscriber(modid = Pride.MODID)
public class AxeAttributeHandler {

	// UUID vanilla du modificateur "dégâts de base" d'une arme (Item.BASE_ATTACK_DAMAGE_UUID, protected).
	private static final UUID BASE_ATTACK_DAMAGE = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

	@SubscribeEvent
	public static void onGatherComponents(GatherComponentsEvent.Item event) {
		if (!(event.getOwner() instanceof AxeItem)) return;
		ItemAttributeModifiers current = event.getDataComponentMap().get(DataComponents.ATTRIBUTE_MODIFIERS);
		if (current == null) return;

		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
			AttributeModifier mod = entry.modifier();
			if (entry.attribute().equals(Attributes.ATTACK_DAMAGE) && mod.id().equals(BASE_ATTACK_DAMAGE)) {
				mod = new AttributeModifier(mod.id(), mod.name(), mod.amount() - 2.0D, mod.operation());
			}
			builder.add(entry.attribute(), mod, entry.slot());
		}
		event.register(DataComponents.ATTRIBUTE_MODIFIERS, builder.build().withTooltip(true));
	}
}
