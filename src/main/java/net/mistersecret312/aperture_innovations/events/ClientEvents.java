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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
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
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import net.mistersecret312.aperture_innovations.sounds.PortalSoundWrapper;
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

		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
		{
			Level level = Minecraft.getInstance().level;
			LINKS.forEach((linkID, link) -> {
				poseStack.pushPose();

				for(int i = 0; i < 2; i++)
				{
					ResourceKey<Level> dimension = i == 0 ? link.dimensionPrimary() : link.dimensionSecondary();
					if(level.dimension() != dimension)
						continue;

					poseStack.pushPose();

					float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(linkID, i == 0);

					BlockPos portalPos = i == 0 ? link.posPrimary() : link.posSecondary();
					BlockPos otherPortalPos = i == 0 ? link.posSecondary() : link.posPrimary();

					boolean moonshot = i == 0 ? link.moonshotPrimary() : link.moonshotSecondary();
					boolean otherMoonshot = i == 0 ? link.moonshotSecondary() : link.moonshotPrimary();

					if((portalPos != null || moonshot) && (otherPortalPos != null || otherMoonshot))
					{
						if(level.isLoaded(portalPos))
						{
							renderPortalNonSee(buffer, poseStack, camera, link, i == 0, scale);
						}
					}
					poseStack.popPose();
					buffer.endBatch();
				}
				poseStack.popPose();
			});
		}

		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
		{
			for(Map.Entry<UUID, ClientPortalLink> linkEntry : LINKS.entrySet())
			{
				ClientPortalLink link = linkEntry.getValue();

				poseStack.pushPose();

				ResourceLocation texturePrimary = ClientPortalUtilities.getPortalVortexTexture(link, true);
				ResourceLocation textureSecondary = ClientPortalUtilities.getPortalVortexTexture(link, false);

				final TextureAtlasSprite primary = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															.apply(texturePrimary);
				final TextureAtlasSprite secondary = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															  .apply(textureSecondary);

				if(!PortalViewingRenderer.rendering)
				{
					for(int i = 0; i < 2; i++)
					{
						ResourceKey<Level> dimension = i == 0 ? link.dimensionPrimary() : link.dimensionSecondary();
						if(Minecraft.getInstance().level.dimension() != dimension)
							continue;

						poseStack.pushPose();

						float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(link.linkID(), i == 0);

						BlockPos portalPos = i == 0 ? link.posPrimary() : link.posSecondary();
						BlockPos otherPortalPos = i == 0 ? link.posSecondary() : link.posPrimary();

						if(portalPos != null && otherPortalPos != null)
						{
//							if(!PortalViewingRenderer.rendering)
//							{
//								if(Minecraft.getInstance().level.isLoaded(otherPortalPos))
//									renderPortal(buffer, camera, poseStack, link, i == 0, scale);
//							}
						}

						poseStack.popPose();

						if(portalPos != null && Minecraft.getInstance().level.isLoaded(portalPos)
								   && event.getLevelRenderer().getFrustum().isVisible(new AABB(portalPos).inflate(1)))
						{
							if(i == 0)
							{
								primaryRender(link, buffer, poseStack, camera, scale);
							}
							else secondaryRender(link, buffer, poseStack, camera, scale);
						}


						if(i == 0 && link.posPrimary() != null)
							renderPortalVortex(link, camera, primary, buffer, poseStack, true);
						else if(link.posSecondary() != null)
							renderPortalVortex(link, camera, secondary, buffer, poseStack, false);
					}
				}

				poseStack.popPose();
				buffer.endBatch();
			}
		}
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
			boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(level, uuid, isPrimary);

			AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, portalDirection, isOnWall, isOnCeiling);
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
						for(int i = 0; i < 2; i++)
						{
							boolean isPrimary = i == 0;
							BlockPos portalPos = isPrimary ? link.posPrimary() : link.posSecondary();

							if(portalPos != null)
							{
								float progress = ClientPortalUtilities.getPortalOpeningAnimationProgress(linkID, isPrimary);
								if(progress < 1F)
								{
									progress += 0.25F;
									ClientPortalUtilities.setPortalOpeningAnimationProgress(progress, linkID, isPrimary);
								}
							}

							if(!link.isOpen())
							{
								PortalSoundWrapper.PortalAmbient ambient = ClientPortalUtilities.getAmbientSound(linkID, isPrimary);
								if(ambient != null)
									ambient.stopSound();
							}
						}

						if(link.posPrimary() != null && link.posSecondary() != null && false)
							mc.execute(() ->
							{
								if(mc.level.dimension() == link.dimensionPrimary())
									PortalViewingRenderer.requestPortalUpdate(linkID, true);
								if(mc.level.dimension() == link.dimensionSecondary())
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
