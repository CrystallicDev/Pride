package com.natsu.pride.network;

import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

/**
 * The server-driven channel (Paper plugin / proxy). The mod only LISTENS: a server sends the list
 * of features to enable client-side on {@code pride:features}, which flips {@link PrideFeature}
 * into PILOTED mode. The channel is OPTIONAL (acceptMissingOr): connecting to a server without it
 * (vanilla, bare Paper, Forge without Pride) still works, with features off.
 *
 * <p>Payload format (clientbound), versioned so third-party plugins stay compatible:
 * <pre>
 *   byte    version           (currently 1)
 *   float   blockingReduction (sword-blocking damage reduction, 0..1)
 *   varInt  count
 *   count × Utf(featureKey)   keys of the ENABLED features (see PrideFeature#key, e.g. "revertKnockback")
 * </pre>
 * An unknown key is ignored (forward compat); an unknown version makes the whole message get dropped.
 */
public final class PrideNetwork {

	private static final Logger LOGGER = LogUtils.getLogger();

	public static final ResourceLocation CHANNEL_ID = new ResourceLocation(Pride.MODID, "features");
	private static final String PROTOCOL_VERSION = "1";
	private static final byte PAYLOAD_VERSION = 1;

	private static EventNetworkChannel channel;

	private PrideNetwork() {}

	public static void register() {
		channel = NetworkRegistry.newEventChannel(
				CHANNEL_ID,
				() -> PROTOCOL_VERSION,
				NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
				NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION));
		channel.addListener(PrideNetwork::onPayload);
	}

	private static void onPayload(NetworkEvent event) {
		NetworkEvent.Context ctx = event.getSource().get();
		ctx.setPacketHandled(true);
		// getSender() != null => received server-side (from a client): we only listen client-side.
		if (ctx.getSender() != null) return;

		FriendlyByteBuf buf = event.getPayload();
		if (buf == null) return;
		// grab the bytes while we're still on the network thread (the buffer gets freed afterwards).
		Set<PrideFeature> features = EnumSet.noneOf(PrideFeature.class);
		double blockingReduction;
		try {
			byte version = buf.readByte();
			if (version != PAYLOAD_VERSION) {
				LOGGER.warn("[Pride] version de payload inconnue ({}), message ignoré", version);
				return;
			}
			blockingReduction = buf.readFloat();
			int count = buf.readVarInt();
			for (int i = 0; i < count; i++) {
				String key = buf.readUtf();
				PrideFeature f = PrideFeature.byKey(key);
				if (f == null) continue;
				// a server feature (damage, knockback...) is the plugin's job: the client doesn't
				// apply it, otherwise it predicts a state the server never confirms.
				if (!f.pilotable()) {
					LOGGER.warn("[Pride] feature serveur '{}' reçue mais ignorée (à gérer côté plugin)", key);
					continue;
				}
				features.add(f);
			}
		} catch (Exception e) {
			LOGGER.warn("[Pride] payload de pilotage illisible, message ignoré", e);
			return;
		}

		ctx.enqueueWork(() -> PrideFeature.setPiloted(features, blockingReduction));
	}
}
