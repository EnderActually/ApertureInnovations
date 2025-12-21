package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.aperture_innovations.events.ClientEvents;

import java.util.function.Supplier;

public class ClientboundTeleportMomentumPacket
{
	public Vec3 speed;
	public Vec3 position;

	public ResourceKey<Level> dimension;
	public float yRot;

	public ClientboundTeleportMomentumPacket(Vec3 speed, Vec3 position, float yRot)
	{
		this.speed = speed;
		this.position = position;
		this.yRot = yRot;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeDouble(speed.x);
		buffer.writeDouble(speed.y);
		buffer.writeDouble(speed.z);

		buffer.writeDouble(position.x);
		buffer.writeDouble(position.y);
		buffer.writeDouble(position.z);

		buffer.writeFloat(yRot);
	}

	public static ClientboundTeleportMomentumPacket decode(FriendlyByteBuf buffer)
	{
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();

		Vec3 speed = new Vec3(x,y,z);

		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();

		Vec3 position = new Vec3(x,y,z);

		return new ClientboundTeleportMomentumPacket(speed, position, buffer.readFloat());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				Player player = Minecraft.getInstance().player;
				if(player != null)
				{
					player.setDeltaMovement(speed);
					//player.moveTo(position.x, position.y, position.z, yRot, player.getXRot());
				}
			});
		return true;
	}
}
