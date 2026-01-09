package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
