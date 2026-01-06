package net.mistersecret312.aperture_innovations.client.overlay;

import ca.weblite.objc.Client;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.ColorUtil;
import net.mistersecret312.aperture_innovations.events.ClientEvents;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.LINKS;

public class CrosshairOverlay
{
	public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, screenWidth, screenHeight)
													   -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return;

			PoseStack poseStack = guiGraphics.pose();

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if (!hasPortalGun) return;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			ClientPortalLink link = LINKS.get(portalGun.getUUID(gunStack, false));
			if(link != null)
			{
				boolean hasPrimary = link.posPrimary() != null || link.moonshotPrimary();
				boolean hasSecondary = link.posSecondary() != null || link.moonshotSecondary();

				ColorUtil.RGBA primaryColor = ClientPortalUtilities.getPortalColor(link, true);
				ColorUtil.RGBA secondaryColor = ClientPortalUtilities.getPortalColor(link, false);


				RenderSystem.setShaderColor(primaryColor.red(), primaryColor.green(), primaryColor.blue(),
						primaryColor.alpha());

				poseStack.pushPose();
				poseStack.translate(hasPrimary ? -1 : -2, 0, 0);

				ResourceLocation primaryCrosshairTexture = ClientPortalUtilities.getCrosshairTexture(link, true);
				guiGraphics.blit(primaryCrosshairTexture, (screenWidth - 16) / 2, (screenHeight - 32) / 2,
						hasPrimary ? 0 : 16, 0,
						16, 32,
						32, 32);

				poseStack.popPose();

				RenderSystem.setShaderColor(secondaryColor.red(), secondaryColor.green(), secondaryColor.blue(),
						secondaryColor.alpha());

				poseStack.pushPose();
				poseStack.translate(hasSecondary ? 1 : 0, 0, 0);

				ResourceLocation secondaryCrosshairTexture = ClientPortalUtilities.getCrosshairTexture(link, false);
				guiGraphics.blit(secondaryCrosshairTexture, (screenWidth - 16) / 2, (screenHeight - 32) / 2,
						hasSecondary ? 0 : 16, 0,
						16, 32,
						32, 32);

				poseStack.popPose();

				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
				return;
			}

		});
}
