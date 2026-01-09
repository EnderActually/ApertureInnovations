package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;

import java.util.function.Supplier;

public class ClientboundApertureCapabilityPacket
{
	public int frictionlessTime;

	public ClientboundApertureCapabilityPacket(int frictionlessTime)
	{
		this.frictionlessTime = frictionlessTime;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeInt(this.frictionlessTime);
	}

	public static ClientboundApertureCapabilityPacket decode(FriendlyByteBuf buffer)
	{
		return new ClientboundApertureCapabilityPacket(buffer.readInt());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				Player player = Minecraft.getInstance().player;
				if(player == null)
					return;

				player.getCapability(CapabilityInit.APERTURE).ifPresent(cap -> {
					cap.frictionlessTime = this.frictionlessTime;
				});
			});
		return true;
	}
}
