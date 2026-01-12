package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientBoundPortalSyncPacket(UUID linkID, boolean isPrimary, BlockPos portalPos,
										  Direction portalDirection, boolean portalWall,
										  boolean portalCeiling, ResourceKey<Level> portalDimension,
										  boolean moonshotPortal, ResourceLocation variant,
										  int color) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ClientBoundPortalSyncPacket> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "s2c_portal_sync"));

	public static final StreamCodec<ByteBuf, ClientBoundPortalSyncPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ClientBoundPortalSyncPacket decode(ByteBuf buffer)
		{
			UUID uuid = FriendlyByteBuf.readUUID(buffer);
			boolean isPrimary = buffer.readBoolean();
			boolean present = buffer.readBoolean();

			BlockPos pos = null;
			boolean wall = false;
			boolean ceiling = false;
			ResourceKey<Level> dimension = null;
			Direction direction = null;
			if(present)
			{
				pos = FriendlyByteBuf.readBlockPos(buffer);
				direction = Direction.from3DDataValue(buffer.readInt());
				wall = buffer.readBoolean();
				ceiling = buffer.readBoolean();
				dimension = ResourceKey.create(Registries.DIMENSION,
						ResourceLocation.parse(Utf8String.read(buffer, 32767)));
			}
			boolean moonshot = buffer.readBoolean();
			ResourceLocation variant = ResourceLocation.parse(Utf8String.read(buffer, 32767));
			int color = buffer.readInt();

			return new ClientBoundPortalSyncPacket(uuid, isPrimary, pos, direction, wall, ceiling,
					dimension, moonshot, variant, color);
		}

		@Override
		public void encode(ByteBuf buffer, ClientBoundPortalSyncPacket packet)
		{
			FriendlyByteBuf.writeUUID(buffer, packet.linkID);
			buffer.writeBoolean(packet.isPrimary);
			if(packet.portalPos != null)
			{
				buffer.writeBoolean(true);
				FriendlyByteBuf.writeBlockPos(buffer, packet.portalPos);
				buffer.writeInt(packet.portalDirection.get3DDataValue());
				buffer.writeBoolean(packet.portalWall);
				buffer.writeBoolean(packet.portalCeiling);
				Utf8String.write(buffer, packet.portalDimension.location().toString(), 32767);
			}
			else buffer.writeBoolean(false);

			buffer.writeBoolean(packet.moonshotPortal);
			Utf8String.write(buffer, packet.variant.toString(), 32767);
			buffer.writeInt(packet.color);
		}
	};

	@Override
	public Type<ClientBoundPortalSyncPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundPortalSyncPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPortalLink link = PortalRenderer.LINKS.getOrDefault(packet.linkID, new ClientPortalLink());
				link.variantKey = packet.variant;
				link.linkID = packet.linkID;
				if(packet.isPrimary)
				{
					link.posPrimary = packet.portalPos;
					link.wallPrimary = packet.portalWall;
					link.directionPrimary = packet.portalDirection;
					link.ceilingPrimary = packet.portalCeiling;
					link.dimensionPrimary = packet.portalDimension;
					link.moonshotPrimary = packet.moonshotPortal;
					link.primaryPortalColor = packet.color;
				}
				else
				{
					link.posSecondary = packet.portalPos;
					link.wallSecondary = packet.portalWall;
					link.directionSecondary = packet.portalDirection;
					link.ceilingSecondary = packet.portalCeiling;
					link.dimensionSecondary = packet.portalDimension;
					link.moonshotSecondary = packet.moonshotPortal;
					link.secondaryPortalColor = packet.color;
				}
				PortalRenderer.LINKS.put(packet.linkID, link);
			});
	}
}
