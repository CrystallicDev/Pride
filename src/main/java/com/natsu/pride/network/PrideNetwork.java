package com.natsu.pride.network;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * The server-driven channel (Paper plugin / proxy). The mod only LISTENS (playToClient): a server
 * sends the list of features to enable client-side on {@code pride:features}, which flips
 * {@link PrideFeature} into PILOTED mode. The channel is OPTIONAL ({@code .optional()}): connecting
 * to a server without it (vanilla, bare Paper...) still works, with features off.
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
	private static final byte PAYLOAD_VERSION = 1;

	private PrideNetwork() {}

	public static void register(IEventBus modEventBus) {
		modEventBus.addListener(PrideNetwork::onRegister);
	}

	private static void onRegister(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1").optional();
		registrar.playToClient(FeaturesPayload.TYPE, FeaturesPayload.STREAM_CODEC, PrideNetwork::handle);
	}

	private static void handle(FeaturesPayload payload, IPayloadContext ctx) {
		if (payload.version() != PAYLOAD_VERSION) {
			LOGGER.warn("[Pride] version de payload inconnue ({}), message ignoré", payload.version());
			return;
		}
		Set<PrideFeature> features = EnumSet.noneOf(PrideFeature.class);
		for (String key : payload.keys()) {
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
		ctx.enqueueWork(() -> PrideFeature.setPiloted(features, payload.blockingReduction()));
	}

	/** Payload clientbound du canal de pilotage. */
	public record FeaturesPayload(byte version, float blockingReduction, List<String> keys) implements CustomPacketPayload {

		public static final CustomPacketPayload.Type<FeaturesPayload> TYPE =
				new CustomPacketPayload.Type<>(new ResourceLocation(Pride.MODID, "features"));

		public static final StreamCodec<FriendlyByteBuf, FeaturesPayload> STREAM_CODEC = StreamCodec.of(
				(buf, p) -> {
					buf.writeByte(p.version);
					buf.writeFloat(p.blockingReduction);
					buf.writeVarInt(p.keys.size());
					for (String k : p.keys) buf.writeUtf(k);
				},
				buf -> {
					byte version = buf.readByte();
					float reduction = buf.readFloat();
					int count = buf.readVarInt();
					List<String> keys = new ArrayList<>(count);
					for (int i = 0; i < count; i++) keys.add(buf.readUtf());
					return new FeaturesPayload(version, reduction, keys);
				});

		@Override
		public CustomPacketPayload.Type<FeaturesPayload> type() {
			return TYPE;
		}
	}
}
