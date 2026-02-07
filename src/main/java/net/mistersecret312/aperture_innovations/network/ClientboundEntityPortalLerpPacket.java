package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

import org.joml.Vector3f;

import java.util.function.Supplier;


public class ClientboundEntityPortalLerpPacket
{
	public int id;
	public Vector3f position;
	public float xRot;
	public float yRot;

	public ClientboundEntityPortalLerpPacket(int id, Vector3f position, float xRot, float yRot)
	{
		this.id = id;
		this.position = position;
		this.xRot = xRot;
		this.yRot = yRot;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeInt(this.id);
		buffer.writeVector3f(position);
		buffer.writeFloat(xRot);
		buffer.writeFloat(yRot);
	}

	public static ClientboundEntityPortalLerpPacket decode(FriendlyByteBuf buffer)
	{
		return new ClientboundEntityPortalLerpPacket(buffer.readInt(), buffer.readVector3f(), buffer.readFloat(), buffer.readFloat());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.handleEntityPortalLerp(this.id, this.position, this.xRot, this.yRot);
			});
		return true;
	}
}
