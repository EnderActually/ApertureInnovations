package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.events.ClientEvents;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLink;

import java.util.*;
import java.util.function.Supplier;

public class ClientBoundPortalLinkSyncPacket
{
	public HashMap<UUID, PortalLink> links;
	public HashMap<UUID, ClientPortalLink> clientLinks = new HashMap<>();

	public ClientBoundPortalLinkSyncPacket(HashMap<UUID, PortalLink> links, HashMap<UUID, ClientPortalLink> clientLinks)
	{
		this.links = links;
		this.clientLinks = clientLinks;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeCollection(this.links.entrySet(), (writer, entry) -> {
			PortalLink link = entry.getValue();

			writer.writeUUID(link.linkID);

			if(link.posPrimary != null)
			{
				writer.writeBoolean(true);

				writer.writeBlockPos(link.posPrimary);
				writer.writeBoolean(link.wallPrimary);
				writer.writeBoolean(link.ceilingPrimary);
				writer.writeResourceKey(link.dimensionPrimary);
				writer.writeEnum(link.directionPrimary);
			}
			else writer.writeBoolean(false);

			if(link.posSecondary != null)
			{
				writer.writeBoolean(true);

				writer.writeBlockPos(link.posSecondary);
				writer.writeBoolean(link.wallSecondary);
				writer.writeBoolean(link.ceilingSecondary);
				writer.writeResourceKey(link.dimensionSecondary);
				writer.writeEnum(link.directionSecondary);
			}
			else writer.writeBoolean(false);
		});
	}

	public static ClientBoundPortalLinkSyncPacket decode(FriendlyByteBuf buffer)
	{
		ArrayList<Map.Entry<UUID, ClientPortalLink>> links = buffer.readCollection(i -> new ArrayList<>(), reader -> {
			UUID linkID = reader.readUUID();

			boolean hasPrimary = reader.readBoolean();

			BlockPos posPrimary = null;
			boolean wallPrimary = false;
			boolean ceilingPrimary = false;
			ResourceKey<Level> dimensionPrimary = null;
			Direction directionPrimary = null;

			if(hasPrimary)
			{
				posPrimary = reader.readBlockPos();
				wallPrimary = reader.readBoolean();
				ceilingPrimary = reader.readBoolean();
				dimensionPrimary = reader.readResourceKey(Registries.DIMENSION);
				directionPrimary = reader.readEnum(Direction.class);
			}

			boolean hasSecondary = reader.readBoolean();

			BlockPos posSecondary = null;
			boolean wallSecondary = false;
			boolean ceilingSecondary = false;
			ResourceKey<Level> dimensionSecondary = null;
			Direction directionSecondary = null;

			if(hasSecondary)
			{
				posSecondary = reader.readBlockPos();
				wallSecondary = reader.readBoolean();
				ceilingSecondary = reader.readBoolean();
				dimensionSecondary = reader.readResourceKey(Registries.DIMENSION);
				directionSecondary = reader.readEnum(Direction.class);
			}

			ClientPortalLink link = new ClientPortalLink(linkID,
					posPrimary, posSecondary,
					wallPrimary, wallSecondary,
					ceilingPrimary, ceilingSecondary,
					dimensionPrimary, dimensionSecondary,
					directionPrimary, directionSecondary);

			return Map.entry(linkID, link);
		});

		HashMap<UUID, ClientPortalLink> linkMap = new HashMap<>();
		for(Map.Entry<UUID, ClientPortalLink> link : links)
		{
			linkMap.put(link.getKey(), link.getValue());
		}

		return new ClientBoundPortalLinkSyncPacket(new HashMap<>(), linkMap);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> PortalRenderer.LINKS = clientLinks);
		return true;
	}
}
