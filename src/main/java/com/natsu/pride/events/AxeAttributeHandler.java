package com.natsu.pride.events;

import java.util.UUID;

import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Réduit de 2 les dégâts de base de toute hache, via son modificateur d'attribut.
 * Passer par l'attribut (plutôt qu'au moment du coup) met à jour la valeur affichée
 * dans le tooltip en plus des dégâts réels.
 */
@Mod.EventBusSubscriber(modid = Pride.MODID)
public class AxeAttributeHandler {

	// Item.BASE_ATTACK_DAMAGE_UUID (protected) : UUID du modificateur "dégâts de base" d'une arme.
	private static final UUID BASE_ATTACK_DAMAGE = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

	@SubscribeEvent
	public static void onItemAttributes(ItemAttributeModifierEvent event) {
		if (!PrideFeature.active()) return;
		if (event.getSlotType() != EquipmentSlot.MAINHAND) return;
		if (!(event.getItemStack().getItem() instanceof AxeItem)) return;

		AttributeModifier base = null;
		for (AttributeModifier mod : event.getModifiers().get(Attributes.ATTACK_DAMAGE)) {
			if (mod.getId().equals(BASE_ATTACK_DAMAGE)) {
				base = mod;
				break;
			}
		}
		if (base != null) {
			event.removeModifier(Attributes.ATTACK_DAMAGE, base);
			event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
					base.getId(), base.getName(), base.getAmount() - 2.0D, base.getOperation()));
		}
	}
}
