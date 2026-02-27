package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.sounds.SoundAccess;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientboundGunZapSoundPacket(UUID player, boolean stop) implements CustomPacketPayload
{
	public static final Type<ClientboundGunZapSoundPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "s2c_zap_sound"));

	public static final StreamCodec<ByteBuf, ClientboundGunZapSoundPacket> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ClientboundGunZapSoundPacket::player,
			ByteBufCodecs.BOOL, ClientboundGunZapSoundPacket::stop,
			ClientboundGunZapSoundPacket::new
	);

	@Override
	public Type<ClientboundGunZapSoundPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientboundGunZapSoundPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				SoundAccess.playGunZap(packet.player, packet.stop);
			});
	}
}
