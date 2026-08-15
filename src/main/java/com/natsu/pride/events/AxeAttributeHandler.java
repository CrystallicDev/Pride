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
 * Knocks 2 off the base damage of every axe through its default attribute-modifiers component.
 * Doing it on the attribute (rather than at hit time) also keeps the tooltip number
 * in sync with the real damage.
 *
 * <p>Weapon modifiers live in the {@link ItemAttributeModifiers} component, so we patch
 * {@link DataComponents#ATTRIBUTE_MODIFIERS} when the item is registered, through
 * {@link GatherComponentsEvent.Item}.
 */
// GatherComponentsEvent fires on the FORGE (game) bus, not MOD; it's posted lazily on the first
// access to an item's components (after load), so vanilla items get seen too.
@Mod.EventBusSubscriber(modid = Pride.MODID)
public class AxeAttributeHandler {

	// vanilla UUID of a weapon's "base damage" modifier (Item.BASE_ATTACK_DAMAGE_UUID, protected).
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
