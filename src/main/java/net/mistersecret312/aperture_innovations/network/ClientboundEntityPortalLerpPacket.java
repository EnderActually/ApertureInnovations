package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;


public record ClientboundEntityPortalLerpPacket(int id, Vector3f position, Vector3f delta, float xRot, float yRot) implements CustomPacketPayload
{
	public static final Type<ClientboundEntityPortalLerpPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "s2c_portal_entity_lerp"));

	public static final StreamCodec<ByteBuf, ClientboundEntityPortalLerpPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ClientboundEntityPortalLerpPacket::id,
			ByteBufCodecs.VECTOR3F, ClientboundEntityPortalLerpPacket::position,
			ByteBufCodecs.VECTOR3F, ClientboundEntityPortalLerpPacket::delta,
			ByteBufCodecs.FLOAT, ClientboundEntityPortalLerpPacket::xRot,
			ByteBufCodecs.FLOAT, ClientboundEntityPortalLerpPacket::yRot,
			ClientboundEntityPortalLerpPacket::new
	);

	@Override
	public Type<ClientboundEntityPortalLerpPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientboundEntityPortalLerpPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.handleEntityPortalLerp(packet.id, packet.position, packet.delta, packet.xRot, packet.yRot);
			});
	}
}
