package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.HoldEntityCapability;
import net.mistersecret312.aperture_innovations.init.AttachmentTypeInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundPickUpEntityPacket() implements CustomPacketPayload
{
	public static final Type<ServerboundPickUpEntityPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "c2s_pick_entity"));

	public static final StreamCodec<ByteBuf, ServerboundPickUpEntityPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ServerboundPickUpEntityPacket decode(ByteBuf buffer)
		{
			return new ServerboundPickUpEntityPacket();
		}

		@Override
		public void encode(ByteBuf buffer, ServerboundPickUpEntityPacket value)
		{

		}
	};

	@Override
	public Type<ServerboundPickUpEntityPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerboundPickUpEntityPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ServerPlayer player = (ServerPlayer) ctx.player();
				Level level = player.level();

				ItemStack main = player.getMainHandItem();
				ItemStack off = player.getOffhandItem();
				boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
				if(!hasPortalGun)
					return;

				ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
				PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

				if(portalGun.getHeldEntity(gunStack) != null)
				{
					portalGun.setHeldEntity(gunStack, null);
					return;
				}
				Vec3 startPos = player.getEyePosition(1F);
				Vec3 endPos = startPos.add(player.getViewVector(1F).multiply(3f, 3f, 3f));

				AABB box = new AABB(startPos, endPos);

				EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, startPos, endPos, box, entity -> true);
				if(result == null)
				{
					portalGun.setHeldEntity(gunStack, null);
					return;
				}

				if(result.getType().equals(HitResult.Type.ENTITY))
				{
					Entity entity = result.getEntity();
					if(entity.getBoundingBox().getSize() >= player.getBoundingBox().getSize())
						return;

					System.out.println("Setting entity! " + entity.getName().getString());
					HoldEntityCapability capability = entity.getData(AttachmentTypeInit.HOLD_ENTITY);
					capability.isHeld = true;
					portalGun.setHeldEntity(gunStack, entity);
					entity.setData(AttachmentTypeInit.HOLD_ENTITY.get(), capability);
				}
				else portalGun.setHeldEntity(gunStack, null);
			});
	}
}
