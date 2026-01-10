package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;

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
