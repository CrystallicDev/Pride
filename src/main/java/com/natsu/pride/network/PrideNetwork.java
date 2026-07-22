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
 * Canal de pilotage par le serveur (plugin Paper / proxy). Le mod ECOUTE seulement (playToClient) :
 * un serveur envoie sur {@code pride:features} la liste des features à activer côté client, ce qui
 * bascule {@link PrideFeature} en mode PILOTED. Canal OPTIONNEL ({@code .optional()}) : la connexion
 * à un serveur sans ce canal (vanilla, Paper nu…) reste possible, features à off.
 *
 * <p>Format du payload (clientbound), versionné pour rester compatible avec des plugins tiers :
 * <pre>
 *   byte    version           (actuellement 1)
 *   float   blockingReduction (réduction de dégâts en blocage épée, 0..1)
 *   varInt  count
 *   count × Utf(featureKey)   clés des features ACTIVÉES (cf. PrideFeature#key, ex. "revertKnockback")
 * </pre>
 * Une clé inconnue est ignorée (compat ascendante) ; une version inconnue fait ignorer le message.
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
			// Une feature serveur (dégâts, knockback...) est du ressort du plugin : le client
			// ne l'applique pas, sinon il prédit un état que le serveur ne confirme pas.
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
				new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Pride.MODID, "features"));

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
