package net.mistersecret312.aperture_innovations.events;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.renderer.PortalViewingRenderer;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.*;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	public static final ResourceLocation TEXTURE_PRIMARY_VORTEX = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"block/portal/portal_blue_vortex");

	public static final ResourceLocation TEXTURE_SECONDARY_VORTEX = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"block/portal/portal_orange_vortex");

	@SubscribeEvent
	public static void renderPortals(RenderLevelStageEvent event) {
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		Camera camera = event.getCamera();
		PoseStack poseStack = event.getPoseStack();

		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
		{
			for(Map.Entry<UUID, ClientPortalLink> linkEntry : LINKS.entrySet())
			{
				ClientPortalLink link = linkEntry.getValue();

				poseStack.pushPose();

				final TextureAtlasSprite primary = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															.apply(TEXTURE_PRIMARY_VORTEX);
				final TextureAtlasSprite secondary = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															  .apply(TEXTURE_SECONDARY_VORTEX);

				if(link.posPrimary() != null && !PortalViewingRenderer.rendering)
					renderPortalVortex(link, camera, primary, buffer, poseStack, true);
				if(link.posSecondary() != null && !PortalViewingRenderer.rendering)
					renderPortalVortex(link, camera, secondary, buffer, poseStack, false);

				poseStack.popPose();
			}
		}

		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
		{
			LINKS.forEach((linkID, link) -> {
				poseStack.pushPose();

				if(link.posPrimary() != null && link.posSecondary() != null)
				{
					if(!PortalViewingRenderer.rendering)
					{
						if(Minecraft.getInstance().level.isLoaded(link.posSecondary()))
							renderPortal(buffer, camera, poseStack, link, true);

						if(Minecraft.getInstance().level.isLoaded(link.posPrimary()))
							renderPortal(buffer, camera, poseStack, link, false);
					}

					if(Minecraft.getInstance().level.isLoaded(link.posPrimary()))
						renderPortalNonSee(buffer, poseStack, camera, link, true);

					if(Minecraft.getInstance().level.isLoaded(link.posSecondary()))
						renderPortalNonSee(buffer, poseStack, camera, link, false);
				}

				if(link.posPrimary() != null && Minecraft.getInstance().level.isLoaded(link.posPrimary())
						   && event.getLevelRenderer().getFrustum().isVisible(new AABB(link.posPrimary()).inflate(1)))
					primaryRender(link, buffer, poseStack, camera);

				if(link.posSecondary() != null && Minecraft.getInstance().level.isLoaded(link.posSecondary())
						   && event.getLevelRenderer().getFrustum().isVisible(new AABB(link.posSecondary()).inflate(1)))
					secondaryRender(link, buffer, poseStack, camera);

				poseStack.popPose();
			});

		}

		buffer.endBatch();
	}

	@SubscribeEvent
	public static void renderBlockOverlay(RenderBlockScreenEffectEvent event)
	{
		Player player = event.getPlayer();
		Level level = player.level();
		BlockPos pos = event.getBlockPos();

		if(event.getOverlayType().equals(RenderBlockScreenEffectEvent.OverlayType.BLOCK))
		{
			Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(player);
			UUID uuid = portal.getFirst();
			boolean isPrimary = portal.getSecond();
			if(uuid == null)
				return;

			Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
			Direction portalDirection = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
			boolean isOnWall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);

			AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, portalDirection, isOnWall);
			if(portalBox.contains(player.getEyePosition()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void mouseClicks(InputEvent.MouseButton.Pre event)
	{
		Level level = Minecraft.getInstance().level;
		Player player = Minecraft.getInstance().player;
		if(level == null || player == null || Minecraft.getInstance().screen != null)
			return;

		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
		if (!hasPortalGun)
			return;

		if(event.getButton() == 0 && event.getAction() == 1)
			NetworkInit.INSTANCE.sendToServer(new ServerboundOpenPortalPacket(true));
		else if(event.getButton() == 1 && event.getAction() == 1)
			NetworkInit.INSTANCE.sendToServer(new ServerboundOpenPortalPacket(false));

		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			Minecraft mc = Minecraft.getInstance();
			if(mc.level != null && mc.player != null)
			{
				LINKS.forEach((linkID, link) ->
					{
						if(link.posPrimary() != null && link.posSecondary() != null)
							mc.execute(() ->
							{
								PortalViewingRenderer.requestPortalUpdate(linkID, true);
								PortalViewingRenderer.requestPortalUpdate(linkID, false);
							});
					});
			}
		}

		if(event.phase == TickEvent.Phase.END)
		{
			while(ApertureInnovations.ClientModEvents.RESET_PORTAL_GUN.get().consumeClick())
			{

				NetworkInit.INSTANCE.sendToServer(new ServerboundResetPortalLinkPacket());
			}
		}
	}

}
