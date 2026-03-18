package fr.natsu.pride.network;

import fr.natsu.pride.Pride;
import fr.natsu.pride.common.capability.IParryCapability;
import fr.natsu.pride.common.capability.ParryCapability;
import fr.natsu.pride.common.capability.ParryCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

//@Mod.EventBusSubscriber
public class NetworkEvents {

	public static SimpleChannel NETWORK;
	
	public static void init() {
		NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(Pride.MODID, "main"), () -> "1", s -> true, s -> true);
		NETWORK.registerMessage(0, ServerBoundParryPacket.class, ServerBoundParryPacket::encode, ServerBoundParryPacket::new, ServerBoundParryPacket::handle);
	}
	
	//@SubscribeEvent
	public static void onRegisterCaps(RegisterCapabilitiesEvent event) {
		event.register(IParryCapability.class);
	}
	
	//@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(new ResourceLocation(Pride.MODID, "parry"), new ParryCapabilityProvider());
		}
	}
	
	//@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == Phase.END) {
			event.player.getCapability(ParryCapabilityProvider.PARRY_CAP).ifPresent(IParryCapability::tick);
		}
	}
	
}
