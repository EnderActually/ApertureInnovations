package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import net.mistersecret312.aperture_innovations.portal.PortalPlacement;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerboundOpenPortalPacket
{
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

			BlockHitResult result = PortalGunItem.rayTrace(player.level(), player, 100);
			if(!result.getType().equals(HitResult.Type.MISS))
			{
				UUID linkID = portalGun.getUUID(gunStack);

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}

				PortalPlacement.Result placement = PortalPlacement.getBestPlacement(level, result, player);
				if(placement != null)
				{
					portalGun.stopTriggeredAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "shoot");

					if(isPrimary) {
						link.createPrimaryPortal(level, placement.bottomPos, level.dimension(), placement.facing, placement.rotation);
						level.playSound(null, player.getOnPos().above(), SoundInit.PORTAL_GUN_FIRE_PRIMARY.get(), SoundSource.PLAYERS, 0.5f, 1f);
					}
					else {
						link.createSecondaryPortal(level, placement.bottomPos, level.dimension(), placement.facing, placement.rotation);
						level.playSound(null, player.getOnPos().above(), SoundInit.PORTAL_GUN_FIRE_SECONDARY.get(), SoundSource.PLAYERS, 0.5f, 1f);
					}
				}
			}

		});
		return true;
	}
}
