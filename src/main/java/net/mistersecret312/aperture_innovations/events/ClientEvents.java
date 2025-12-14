package net.mistersecret312.aperture_innovations.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	public static HashMap<UUID, ClientPortalLink> LINKS = new HashMap<>();

	public static final ResourceLocation TEXTURE_PRIMARY = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/block/portal/portal_blue.png");
	public static final ResourceLocation TEXTURE_PRIMARY_VORTEX = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"block/portal/portal_blue_vortex");

	public static final ResourceLocation TEXTURE_SECONDARY = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/block/portal/portal_orange.png");
	public static final ResourceLocation TEXTURE_SECONDARY_VORTEX = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"block/portal/portal_orange_vortex");

	@SubscribeEvent
	public static void renderPortals(RenderLevelStageEvent event)
	{
		if(event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS))
		{
			Camera camera = event.getCamera();
			PoseStack poseStack = event.getPoseStack();

			LINKS.forEach((linkID, link) -> {
				if(link.posPrimary() != null)
				{
					MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

					poseStack.pushPose();

					Vec3 pos = link.posPrimary().getCenter();

					poseStack.translate(-camera.getPosition().x+pos.x,
							-camera.getPosition().y+pos.y+0.5f,
							-camera.getPosition().z+pos.z);

					poseStack.mulPose(link.directionPrimary().getRotation());
					if(link.wallPrimary())
						poseStack.mulPose(Axis.XP.rotationDegrees(-90));
					else
					{
						poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
						poseStack.mulPose(Axis.ZP.rotationDegrees(180));
						poseStack.translate(0f, 0.5f, -0.5f);
						if(link.ceilingPrimary())
							poseStack.translate(0f, 0f, 1f);
					}

					poseStack.translate(0.5f, 0f, 0.51f);

					final TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															   .apply(TEXTURE_PRIMARY_VORTEX);

					renderPortal(TEXTURE_PRIMARY, buffer, poseStack);
					renderPortalVortex(sprite, buffer, poseStack);

					poseStack.popPose();
				}

				if(link.posSecondary() != null)
				{
					MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

					poseStack.pushPose();

					Vec3 pos = link.posSecondary().getCenter();

					poseStack.translate(-camera.getPosition().x+pos.x,
							-camera.getPosition().y+pos.y+0.5f,
							-camera.getPosition().z+pos.z);

					poseStack.mulPose(link.directionSecondary().getRotation());
					if(link.wallSecondary())
						poseStack.mulPose(Axis.XP.rotationDegrees(-90));
					else
					{
						poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingSecondary() ? 0 : 180));
						poseStack.mulPose(Axis.ZP.rotationDegrees(180));
						poseStack.translate(0f, 0.5f, -0.5f);
						if(link.ceilingSecondary())
							poseStack.translate(0f, 0f, 1f);
					}

					poseStack.translate(0.5f, 0f, 0.51f);

					final TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															   .apply(TEXTURE_SECONDARY_VORTEX);

					renderPortal(TEXTURE_SECONDARY, buffer, poseStack);
					renderPortalVortex(sprite, buffer, poseStack);

					poseStack.popPose();
				}
			});
		}
	}

	public static void renderPortal(ResourceLocation texture, MultiBufferSource buffer, PoseStack poseStack)
	{
		poseStack.pushPose();
		poseStack.scale(2f,2f,2f);

		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portal(texture));
		consumerA.vertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(0, 1)
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(1, 1)
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(1, 0).endVertex();
		consumerA.vertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(0, 0)
				 .endVertex();

		poseStack.popPose();
	}

	public static void renderPortalVortex(TextureAtlasSprite sprite, MultiBufferSource buffer, PoseStack poseStack)
	{
		poseStack.pushPose();
		poseStack.translate(-0.3125f, 0f, 0.005f);
		poseStack.scale(2f,2f,2f);

		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portalVortex(sprite.atlasLocation()));
		consumerA.vertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .color(FastColor.ABGR32.color(191, 255, 255, 255))
				 .uv(sprite.getU0(), sprite.getV1())
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .color(FastColor.ABGR32.color(191, 255, 255, 255))
				 .uv(sprite.getU1(), sprite.getV1())
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .color(FastColor.ABGR32.color(191, 255, 255, 255))
				 .uv(sprite.getU1(), sprite.getV0())
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .color(FastColor.ABGR32.color(191, 255, 255, 255))
				 .uv(sprite.getU0(), sprite.getV0())
				 .endVertex();

		poseStack.popPose();
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
		if(event.phase == TickEvent.Phase.END)
		{
			while(ApertureInnovations.ClientModEvents.RESET_PORTAL_GUN.get().consumeClick())
			{
				NetworkInit.INSTANCE.sendToServer(new ServerboundResetPortalLinkPacket());
			}
		}
	}
}
