
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
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.Portal;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientBoundPortalSyncPacket(UUID linkID, boolean isPrimary, Portal portal, ResourceLocation variant) implements CustomPacketPayload
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
			Portal portal = Portal.decode(buffer);
			ResourceLocation variant = ResourceLocation.parse(Utf8String.read(buffer, 32767));

			return new ClientBoundPortalSyncPacket(uuid, isPrimary, portal, variant);
		}

		@Override
		public void encode(ByteBuf buffer, ClientBoundPortalSyncPacket packet)
		{
			FriendlyByteBuf.writeUUID(buffer, packet.linkID);
			buffer.writeBoolean(packet.isPrimary);
			packet.portal.encode(buffer);
			Utf8String.write(buffer, packet.variant.toString(), 32767);
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
					if(packet.portal.getPosition() != null && !packet.portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
						ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, packet.linkID, true);
					link.setPrimaryPortal(packet.portal);
				}
				else
				{
					if(packet.portal.getPosition() != null && !packet.portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
						ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, packet.linkID, false);
					link.setSecondaryPortal(packet.portal);
				}
				PortalRenderer.LINKS.put(packet.linkID, link);
			});
	}
}