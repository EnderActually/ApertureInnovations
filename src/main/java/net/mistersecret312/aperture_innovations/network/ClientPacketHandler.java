package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import org.joml.Vector3f;

public class ClientPacketHandler
{
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
