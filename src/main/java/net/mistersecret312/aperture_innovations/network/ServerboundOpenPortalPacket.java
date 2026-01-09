package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import net.mistersecret312.aperture_innovations.portal.PortalPlacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerboundOpenPortalPacket
{
	private static final Logger log = LoggerFactory.getLogger(ServerboundOpenPortalPacket.class);
	boolean isPrimary;
	public ServerboundOpenPortalPacket(boolean isPrimary)
	{
		this.isPrimary = isPrimary;
	}

	public ServerboundOpenPortalPacket(FriendlyByteBuf buffer)
	{
		this(buffer.readBoolean());
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(isPrimary);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			Level level = player.level();

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if (!hasPortalGun) return;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			boolean moonshot = portalGun.isLookingAtMoon(player, level);
			if(moonshot)
			{
				UUID linkID = portalGun.getUUID(gunStack, true);

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}
				portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
				portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

				if(isPrimary)
				{
					portalGun.setLastShotPortal(gunStack, 0);
					link.setMoonshot(isPrimary, true, level);
				}
				else
				{
					portalGun.setLastShotPortal(gunStack, 1);
					link.setMoonshot(isPrimary, true, level);
				}

				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
						new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
				return;
			}

			BlockHitResult result = PortalGunItem.rayTrace(player.level(), player, 256);
			if(!result.getType().equals(HitResult.Type.MISS))
			{
				UUID linkID = portalGun.getUUID(gunStack, false);

				if(linkID != null && !level.getBlockState(result.getBlockPos()).is(ApertureInnovations.SHOOT_THROUGH) && (level.getBlockState(result.getBlockPos()).is(ApertureInnovations.IMPORTALABLE)
				|| !level.getFluidState(result.getBlockPos()).isEmpty()))
				{
					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

					NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
							new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
					NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
							new ClientboundPortalSoundsPacket.InvalidSurface(linkID, player.blockPosition(), isPrimary));
					return;
				}

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}

				PortalPlacement.Result placement = PortalPlacement.getBestPlacement(level, result, player, linkID, isPrimary);
				if(placement != null)
				{
					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

					if(isPrimary)
					{
						portalGun.setLastShotPortal(gunStack, 0);
						link.createPrimaryPortal(level, placement.bottomPos, level.dimension(), placement.facing, placement.rotation);

						NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
								new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
					}
					else
					{
						portalGun.setLastShotPortal(gunStack, 1);
						link.createSecondaryPortal(level, placement.bottomPos, level.dimension(), placement.facing, placement.rotation);

						NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
								new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));					}
				}
				else
				{
					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

					NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
							new ClientboundPortalSoundsPacket.ShootPortal(linkID, player.blockPosition(), isPrimary));
					NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
							new ClientboundPortalSoundsPacket.InvalidSurface(linkID, player.blockPosition(), isPrimary));
				}
			}

		});
		return true;
	}
}
