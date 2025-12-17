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
import net.minecraft.core.Direction;
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
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	static int tickCounter = 0;

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
	public static void renderPortals(RenderLevelStageEvent event) {
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		Camera camera = event.getCamera();
		PoseStack poseStack = event.getPoseStack();

		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
		{
			LINKS.forEach((linkID, link) -> {
				poseStack.pushPose();

				if(link.posPrimary() != null && link.posSecondary() != null)
				{
					renderPortal(buffer, camera, poseStack, link, true);
					renderPortal(buffer, camera, poseStack, link, false);

					renderPortalNonSee(buffer, poseStack, camera, link, true);
					renderPortalNonSee(buffer, poseStack, camera, link, false);
				}

				if(link.posPrimary() != null)
					primaryRender(link, buffer, poseStack, camera);
				if(link.posSecondary() != null)
					secondaryRender(link, buffer, poseStack, camera);

				poseStack.popPose();
			});

		}

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

				if(link.posPrimary() != null)
					renderPortalVortex(link, camera, primary, buffer, poseStack, true);
				if(link.posSecondary() != null)
					renderPortalVortex(link, camera, secondary, buffer, poseStack, false);

				poseStack.popPose();
			}
		}

		buffer.endBatch();
	}

	public static void primaryRender(ClientPortalLink link, MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera) {
		if (link.posPrimary() != null) {
			poseStack.pushPose();
			Vec3 pos = link.posPrimary().getCenter();
			poseStack.translate(-camera.getPosition().x + pos.x,
					-camera.getPosition().y + pos.y + 0.5f,
					-camera.getPosition().z + pos.z);
			poseStack.mulPose(link.directionPrimary().getRotation());
			if (link.wallPrimary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingPrimary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.51f);
			renderPortalFrame(TEXTURE_PRIMARY, buffer, poseStack);

			poseStack.popPose();
		}
	}

	public static void secondaryRender(ClientPortalLink link, MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera) {
		if (link.posSecondary() != null) {
			poseStack.pushPose();
			Vec3 pos = link.posSecondary().getCenter();
			poseStack.translate(-camera.getPosition().x + pos.x,
					-camera.getPosition().y + pos.y + 0.5f,
					-camera.getPosition().z + pos.z);
			poseStack.mulPose(link.directionSecondary().getRotation());
			if (link.wallSecondary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingSecondary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingSecondary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.51f);
			renderPortalFrame(TEXTURE_SECONDARY, buffer, poseStack);

			poseStack.popPose();
		}
	}

	public static void renderPortalNonSee(MultiBufferSource buffer, PoseStack poseStack, Camera camera, ClientPortalLink link, boolean isPrimary)
	{
		poseStack.pushPose();
		Vec3 pos = isPrimary ? link.posPrimary().getCenter() : link.posSecondary().getCenter();
		Direction direction = isPrimary ? link.directionPrimary() : link.directionSecondary();
		poseStack.translate(pos.x-camera.getPosition().x,
				 pos.y-camera.getPosition().y+0.5,
				pos.z-camera.getPosition().z);
		poseStack.mulPose(direction.getRotation());

		if(isPrimary)
		{
			if (link.wallPrimary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingPrimary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.52f);
		}
		else
		{
			if (link.wallSecondary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingSecondary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingSecondary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.52f);
		}

		poseStack.scale(1f, 2f, 1f);
		poseStack.translate(-0.5,0f,0f);

		VertexConsumer consumerA = buffer.getBuffer(
				PortalRenderTypes.portal(new ResourceLocation(ApertureInnovations.MODID,
						"textures/block/portal/portal_mask.png")));

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

	public static void renderPortal(MultiBufferSource buffer, Camera camera, PoseStack poseStack, ClientPortalLink link, boolean isPrimary) {
		poseStack.pushPose();
		Vec3 pos = isPrimary ? link.posPrimary().getCenter() : link.posSecondary().getCenter();
		Direction direction = isPrimary ? link.directionPrimary() : link.directionSecondary();
		poseStack.translate(pos.x-camera.getPosition().x,
				pos.y-camera.getPosition().y+0.5,
				pos.z-camera.getPosition().z);
		poseStack.mulPose(direction.getRotation());

		if(isPrimary)
		{
			if (link.wallPrimary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingPrimary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.52f);
		}
		else
		{
			if (link.wallSecondary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingSecondary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingSecondary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.52f);
		}

		poseStack.scale(1f,2f,1f);
		poseStack.translate(-0.5, 0f, 0f);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		Matrix4f matrix = poseStack.last().pose();

		RenderSystem.colorMask(false, false, false, false);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, new ResourceLocation(ApertureInnovations.MODID, "textures/block/portal/portal_mask.png"));

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		builder.vertex(matrix, -0.5f, -0.5f, 0).uv(0f, 0f).endVertex();
		builder.vertex(matrix,  0.5f, -0.5f, 0).uv(1f, 0f).endVertex();
		builder.vertex(matrix,  0.5f,  0.5f, 0).uv(1f, 1f).endVertex();
		builder.vertex(matrix, -0.5f,  0.5f, 0).uv(0f, 1f).endVertex();

		BufferUploader.drawWithShader(builder.end());

		RenderSystem.colorMask(true, true, true, true);

		RenderSystem.depthFunc(GL11.GL_EQUAL);

		Pair<RenderTarget, RenderTarget> pair = PortalRenderer.FRAMEBUFFERS.get(link.linkID());

		RenderSystem.setShaderTexture(0, isPrimary ? pair.getSecond().getColorTextureId() : pair.getFirst().getColorTextureId());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		builder.vertex(matrix, -0.5f, -0.5f, 0).uv(0.0f, 0.0f).endVertex();
		builder.vertex(matrix,  0.5f, -0.5f, 0).uv(1f, 0.0f).endVertex();
		builder.vertex(matrix,  0.5f,  0.5f, 0).uv(1f, 1.0f).endVertex();
		builder.vertex(matrix, -0.5f,  0.5f, 0).uv(0.0f, 1.0f).endVertex();

		BufferUploader.drawWithShader(builder.end());

		RenderSystem.depthFunc(GL11.GL_LEQUAL);

		poseStack.popPose();
	}

	public static void renderPortalFrame(ResourceLocation texture, MultiBufferSource buffer, PoseStack poseStack) {
		poseStack.pushPose();
		poseStack.scale(2f, 2f, 2f);

		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portalFrame(texture));
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

	public static void renderPortalVortex(ClientPortalLink link, Camera camera,
										  TextureAtlasSprite sprite, MultiBufferSource buffer,
										  PoseStack poseStack, boolean isPrimary) {
		poseStack.pushPose();
		Vec3 pos = isPrimary ? link.posPrimary().getCenter() : link.posSecondary().getCenter();
		Direction direction = isPrimary ? link.directionPrimary() : link.directionSecondary();
		poseStack.translate(pos.x-camera.getPosition().x,
				pos.y-camera.getPosition().y+0.5,
				pos.z-camera.getPosition().z);
		poseStack.mulPose(direction.getRotation());

		if(isPrimary)
		{
			if (link.wallPrimary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingPrimary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.52f);
		}
		else
		{
			if (link.wallSecondary())
				poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			else {
				poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingSecondary() ? 0 : 180));
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				poseStack.translate(0f, 0.5f, -0.5f);
				if (link.ceilingSecondary())
					poseStack.translate(0f, 0f, 1f);
			}
			poseStack.translate(0.5f, 0f, 0.52f);
		}

		poseStack.translate(-0.3125f, 0f, 0.005f);
		poseStack.scale(2f, 2f, 2f);

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
		if (event.phase == TickEvent.Phase.END)
		{
			Minecraft mc = Minecraft.getInstance();
			if(mc.level != null && mc.player != null)
			{
				ClientEvents.LINKS.forEach((linkID, link) ->
					{
						if(link.posPrimary() != null && link.posSecondary() != null)
							mc.execute(() ->
							{
								PortalRenderer.requestPortalUpdate(linkID, true);
								PortalRenderer.requestPortalUpdate(linkID, false);
								//PortalRenderer.updateTextures(linkID);
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
