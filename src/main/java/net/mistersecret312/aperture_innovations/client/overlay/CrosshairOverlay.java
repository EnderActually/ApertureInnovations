package net.mistersecret312.aperture_innovations.client.overlay;

import ca.weblite.objc.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.events.ClientEvents;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.LINKS;

public class CrosshairOverlay
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(ApertureInnovations.MODID,
			"textures/gui/portal_crosshair.png");

	public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, screenWidth, screenHeight)
													   -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return;

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if (!hasPortalGun) return;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			ClientPortalLink link = LINKS.get(portalGun.getUUID(gunStack));
			if(link != null)
			{
				boolean hasPrimary = link.posPrimary() != null || link.moonshotPrimary();
				boolean hasSecondary = link.posSecondary() != null || link.moonshotSecondary();

				if(hasPrimary && hasSecondary)
				{
					guiGraphics.blit(TEXTURE, (screenWidth-17)/2, (screenHeight-33)/2,
							0,0, 17, 33, 68, 33);
					return;
				}

				if(hasPrimary)
				{
					guiGraphics.blit(TEXTURE, (screenWidth-17)/2, (screenHeight-33)/2,
							17,0, 17, 33, 68, 33);
					return;
				}

				if(hasSecondary)
				{
					guiGraphics.blit(TEXTURE, (screenWidth-17)/2, (screenHeight-33)/2,
							34,0, 17, 33, 68, 33);
					return;
				}

				guiGraphics.blit(TEXTURE, (screenWidth - 17) / 2, (screenHeight - 33) / 2, 51, 0, 17, 33, 68, 33);
				return;

			}

		});
}
