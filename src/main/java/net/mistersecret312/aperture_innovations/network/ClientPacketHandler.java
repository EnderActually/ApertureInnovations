package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.init.AttachmentTypeInit;
import org.joml.Vector3f;

public class ClientPacketHandler
{
	public static void handleTeleportMomentumPacket(Vector3f speed)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			player.setDeltaMovement(new Vec3(speed));
			player.setOldPosAndRot();
		}
	}

	public static void handlePlayerCapabilityPacket(int frictionlessTime)
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
			return;

		ApertureCapability aperture = player.getData(AttachmentTypeInit.APERTURE);
		aperture.frictionlessTime = frictionlessTime;
		player.setData(AttachmentTypeInit.APERTURE, aperture);
	}
}
