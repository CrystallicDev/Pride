package com.natsu.pride.network;

import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.natsu.pride.Pride;
import com.natsu.pride.features.PrideFeature;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;

/**
 * Canal de pilotage par le serveur (plugin Paper / proxy). Le mod ECOUTE seulement : un serveur
 * envoie sur {@code pride:features} la liste des features à activer côté client, ce qui bascule
 * {@link PrideFeature} en mode PILOTED. Canal OPTIONNEL : la connexion à un serveur sans ce canal
 * (vanilla, Paper nu, Forge sans Pride) reste possible, features à off.
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

	public static final ResourceLocation CHANNEL_ID = new ResourceLocation(Pride.MODID, "features");
	private static final int PROTOCOL_VERSION = 1;
	private static final byte PAYLOAD_VERSION = 1;

	private static EventNetworkChannel channel;

	private PrideNetwork() {}

	public static void register() {
		// 1.20.6 : NetworkRegistry supprimé → ChannelBuilder. optional() = canal accepté même absent
		// des deux côtés. ATTENTION à l'ordre : optional() fige un test exact(version) courant comme
		// repli, donc networkProtocolVersion() DOIT être appelé AVANT optional() (sinon exact(0) ≠ 1).
		channel = ChannelBuilder.named(CHANNEL_ID)
				.networkProtocolVersion(PROTOCOL_VERSION)
				.optional()
				.eventNetworkChannel();
		channel.addListener(PrideNetwork::onPayload);
	}

	private static void onPayload(CustomPayloadEvent event) {
		CustomPayloadEvent.Context ctx = event.getSource();
		ctx.setPacketHandled(true);
		// getSender() != null => reçu côté serveur (venant d'un client) : on n'écoute que côté client.
		if (ctx.getSender() != null) return;

		FriendlyByteBuf buf = event.getPayload();
		if (buf == null) return;
		// Copie des octets tant qu'on est sur le thread réseau (le buffer est libéré après).
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
				// Une feature serveur (dégâts, knockback...) est du ressort du plugin : le client
				// ne l'applique pas, sinon il prédit un état que le serveur ne confirme pas.
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
