package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerboundResetPortalLinkPacket
{
	public ServerboundResetPortalLinkPacket()
	{}

	public ServerboundResetPortalLinkPacket(FriendlyByteBuf buffer)
	{}

	public void encode(FriendlyByteBuf buffer)
	{}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ServerPlayer player = ctx.get().getSender();
				Level level = player.level();

				ItemStack main = player.getMainHandItem();
				ItemStack off = player.getOffhandItem();
				boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
				if(!hasPortalGun)
					return;

				ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
				PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

				UUID linkID = portalGun.getUUID(gunStack);

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}
				if((link.posPrimary == null && !link.moonshotPrimary)
						   && (link.posSecondary == null) && !link.moonshotSecondary)
					return;

				link.reset(level);

				level.playSound(null, player.getOnPos().above(), SoundInit.PORTAL_GUN_RESET.get(), SoundSource.PLAYERS,
						0.7f, 1f);
				portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "reset");
			});
		return true;
	}
}
