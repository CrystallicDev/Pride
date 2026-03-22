package com.natsu.pride.client.events;

import com.natsu.pride.network.NetworkEvents;
import com.natsu.pride.network.ServerBoundParryPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientSetupEvent {

	//@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.screen == null) return;
			
			ItemStack hand = mc.player.getMainHandItem();
			boolean isRightClick = mc.options.keyUse.isDown();
			boolean isParry = (hand != null ? (hand.getItem() instanceof SwordItem ? isRightClick : false): false);
			NetworkEvents.NETWORK.sendToServer(new ServerBoundParryPacket(isParry));
		}
	}
	
}
