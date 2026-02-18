package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;


public record ClientboundAntlineUpdatePacket(BlockPos pos) implements CustomPacketPayload
{
	public static final Type<ClientboundAntlineUpdatePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "s2c_antline_update"));

	public static final StreamCodec<ByteBuf, ClientboundAntlineUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ClientboundAntlineUpdatePacket::pos,
			ClientboundAntlineUpdatePacket::new
	);

	@Override
	public Type<ClientboundAntlineUpdatePacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientboundAntlineUpdatePacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.handleAntlineUpdate(packet.pos);
			});
	}
}
