
package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.Portal;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientBoundPortalSyncPacket
{
	public UUID linkID;
	public boolean isPrimary;
	public Portal portal;
	public ResourceLocation variant;
	
	public ClientBoundPortalSyncPacket(UUID linkID, boolean isPrimary, Portal portal, ResourceLocation variant)
	{
		this.linkID = linkID;
		this.isPrimary = isPrimary;
		this.portal = portal;
		this.variant = variant;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeUUID(this.linkID);
		buffer.writeBoolean(isPrimary);
		portal.encode(buffer);
		buffer.writeUtf(variant.toString());
	}

	public static ClientBoundPortalSyncPacket decode(FriendlyByteBuf buffer)
	{
		UUID uuid = buffer.readUUID();
		boolean isPrimary = buffer.readBoolean();
		Portal portal = Portal.decode(buffer);
		
		ResourceLocation variant = ResourceLocation.parse(buffer.readUtf());
		
		return new ClientBoundPortalSyncPacket(uuid, isPrimary, portal, variant);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPortalLink link = PortalRenderer.LINKS.getOrDefault(linkID, new ClientPortalLink());
				link.variantKey = variant;
				link.linkID = linkID;
				if(isPrimary)
				{
					if(portal.getPosition() != null && !portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
						ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, true);
					link.setPrimaryPortal(portal);
				}
				else
				{
					if(portal.getPosition() != null && !portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
						ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, false);
					link.setSecondaryPortal(portal);
				}
				PortalRenderer.LINKS.put(linkID, link);
			});
		return true;
	}
}