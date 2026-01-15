package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;
import java.util.function.Supplier;

public record ServerboundResetPortalLinkPacket() implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ServerboundResetPortalLinkPacket> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "c2s_reset_portal"));

	public static final StreamCodec<ByteBuf, ServerboundResetPortalLinkPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ServerboundResetPortalLinkPacket decode(ByteBuf buffer)
		{
			return new ServerboundResetPortalLinkPacket();
		}

		@Override
		public void encode(ByteBuf buffer, ServerboundResetPortalLinkPacket value)
		{

		}
	};

	@Override
	public CustomPacketPayload.Type<ServerboundResetPortalLinkPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerboundResetPortalLinkPacket packet, IPayloadContext ctx)
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

				int dualityState = portalGun.getDualityState(gunStack);

				UUID linkID = portalGun.getUUID(gunStack, true);

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				PortalLink pairLink = null;

				UUID pairID = portalGun.getPair(gunStack);
				if(pairID != null)
					pairLink = linkData.getLink(pairID);

				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}
				if((link.posPrimary == null && !link.moonshotPrimary)
						   && (link.posSecondary == null) && !link.moonshotSecondary)
					return;
				if(pairLink != null && dualityState != 2)
				{
					if(dualityState == 1 && (pairLink.posSecondary == null && !pairLink.moonshotSecondary))
						return;
					if(dualityState == 0 && (pairLink.posPrimary == null && !pairLink.moonshotPrimary))
						return;
				}

				if(dualityState == 2)
					link.reset(level);
				if(dualityState == 1)
					link.resetSecondary(level);
				if(dualityState == 0)
					link.resetPrimary(level);

				portalGun.setLastShotPortal(gunStack, -1);
				PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(player.blockPosition()),
						new ClientboundPortalSoundsPacket.ResetPortal(linkID, player.blockPosition()));

				portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "reset");
			});
	}

}
