package net.mistersecret312.aperture_innovations.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import net.mistersecret312.aperture_innovations.sounds.PortalSoundWrapper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.UUID;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.*;

@EventBusSubscriber(modid = ApertureInnovations.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	@SubscribeEvent
	public static void renderPortals(RenderLevelStageEvent event) {
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		Camera camera = event.getCamera();
		PoseStack poseStack = event.getPoseStack();
		Matrix4f matrix4f = event.getModelViewMatrix();

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
							renderPortalNonSee(buffer, event.getProjectionMatrix(), poseStack, camera, link, i == 0, scale);
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
				final TextureAtlasSprite secondary = Minecraft.getInstance()
															  .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															  .apply(textureSecondary);

				for(int i = 0; i < 2; i++)
				{
					ResourceKey<Level> dimension = i == 0 ? link.dimensionPrimary() : link.dimensionSecondary();
					if(Minecraft.getInstance().level.dimension() != dimension) continue;

					float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(link.linkID(), i == 0);

					BlockPos portalPos = i == 0 ? link.posPrimary() : link.posSecondary();
					BlockPos otherPortalPos = i == 0 ? link.posSecondary() : link.posPrimary();

					if(portalPos != null && Minecraft.getInstance().level.isLoaded(
							portalPos) && event.getLevelRenderer().getFrustum()
											   .isVisible(new AABB(portalPos).inflate(1)))
					{
						if(i == 0)
						{
							primaryRender(link, buffer, poseStack, camera, scale);
						} else secondaryRender(link, buffer, poseStack, camera, scale);
					}


					if(i == 0 && link.posPrimary() != null)
						renderPortalVortex(link, camera, primary, buffer, poseStack, true);
					else if(link.posSecondary() != null)
						renderPortalVortex(link, camera, secondary, buffer, poseStack, false);
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

		ItemStack gunItemStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
		PortalGunItem gunItem = (PortalGunItem) gunItemStack.getItem();

		int dualityState = gunItem.getDualityState(gunItemStack);

		if(!player.isShiftKeyDown() && event.getButton() == 0 &&
				   event.getAction() == 1 && (dualityState == 2 || dualityState == 0))
		{
			PacketDistributor.sendToServer(new ServerboundOpenPortalPacket(true));
			event.setCanceled(true);
		}
		else if(!player.isShiftKeyDown() && event.getButton() == 1 &&
						event.getAction() == 1 && (dualityState == 2 || dualityState == 1))
		{
			PacketDistributor.sendToServer(new ServerboundOpenPortalPacket(false));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent.Pre event)
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
							PortalSoundWrapper.PortalAmbient ambient = ClientPortalUtilities.getAmbientSound(linkID,
									isPrimary);
							if(ambient != null) ambient.stopSound();
						}
					}
				});
		}

		while(ApertureInnovations.ClientModEvents.RESET_PORTAL_GUN.get().consumeClick())
			PacketDistributor.sendToServer(new ServerboundResetPortalLinkPacket());
	}

	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
		if (event.getEntity() instanceof Player player) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (boots.getItem() instanceof LongFallBootsItem) {
				if (event.getRenderer().getModel() instanceof HumanoidModel<?> model) {
					model.leftLeg.yScale = 0.625F;
					model.rightLeg.yScale = 0.625F;

					model.leftArm.yRot = (float) Math.toRadians(45);
					model.leftArm.yScale = 2F;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
		if (event.getEntity() instanceof Player player) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (!(boots.getItem() instanceof LongFallBootsItem)) {
				if (event.getRenderer().getModel() instanceof HumanoidModel<?> model) {
					model.leftLeg.yScale = 1F;
					model.rightLeg.yScale = 1F;
				}
			}
		}
	}

}
