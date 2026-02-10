package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.Portal;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import org.joml.Vector3f;

import java.util.UUID;

public class ClientPacketHandler
{
	public static void syncPortalData(UUID linkID, boolean isPrimary, Portal portal, ResourceLocation variant)
	{
		ClientPortalLink link = PortalRenderer.LINKS.getOrDefault(linkID, new ClientPortalLink());
		link.variantKey = variant;
		link.linkID = linkID;
		if(isPrimary)
		{
			if(portal.getPosition() != null && !portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
			{
				portal.setReplaceShapes(PortalUtilities.calculatePortalVoxels(Minecraft.getInstance().level,
						portal.getPosition(), portal.getXRotation(), portal.getYRotation()));
				ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, true);
			}
			link.setPrimaryPortal(portal);
		}
		else
		{
			if(portal.getPosition() != null && !portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
			{
				portal.setReplaceShapes(PortalUtilities.calculatePortalVoxels(Minecraft.getInstance().level,
						portal.getPosition(), portal.getXRotation(), portal.getYRotation()));
				ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, false);
			}
			link.setSecondaryPortal(portal);
		}
		PortalRenderer.LINKS.put(linkID, link);
	}

	public static void handleTeleportMomentumPacket(Vec3 speed)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			player.setDeltaMovement(speed);
		}
	}

	public static void handleEntityPortalLerp(int id, Vector3f pos, float xRot, float yRot)
	{
		Entity entity = Minecraft.getInstance().level.getEntity(id);
		if(entity != null)
		{
			entity.setPos(pos.x, pos.y, pos.z);

			entity.xOld = pos.x;
			entity.xo = pos.x;
			entity.xRotO = xRot;

			entity.yOld = pos.y;
			entity.yo = pos.y;
			entity.yRotO = yRot;

			entity.zOld = pos.z;
			entity.zo = pos.z;

			entity.setOldPosAndRot();
			entity.lerpTo(pos.x, pos.y, pos.z, yRot, xRot, 0, true);
		}
	}

	public static void handlePlayerCapabilityPacket(int frictionlessTime)
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
			return;

		player.getCapability(CapabilityInit.APERTURE).ifPresent(cap ->
			{
				cap.frictionlessTime = frictionlessTime;
			});
	}
}
