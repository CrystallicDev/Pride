package fr.natsu.pride.network;

import java.util.function.Supplier;

import fr.natsu.pride.common.capability.ParryCapability;
import fr.natsu.pride.common.capability.ParryCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ServerBoundParryPacket {

	private final boolean parry;
	
	public ServerBoundParryPacket(boolean parry) {
		this.parry = parry;
	}
	
	public ServerBoundParryPacket(FriendlyByteBuf buffer) {
		this.parry = buffer.readBoolean();
	}
	
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(parry);
	}
	
	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(
				() -> {
					ServerPlayer player = context.get().getSender();
					if (player == null) return;
					
					player.getCapability(ParryCapabilityProvider.PARRY_CAP).ifPresent(cap -> {
						if (parry && cap.getCooldown() <= 0) {
							cap.setParrying(true);
						} else {
							if (cap.isParrying()) {
								cap.setCooldown(ParryCapability.PARRY_COOLDOWN_TICKS);
							}
							cap.setParrying(false);
						}
					});
				});
		context.get().setPacketHandled(true);
	}
	
}
