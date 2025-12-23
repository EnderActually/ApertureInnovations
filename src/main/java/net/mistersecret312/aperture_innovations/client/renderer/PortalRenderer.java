package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

public class PortalRenderer
{
	public static HashMap<UUID, ClientPortalLink> LINKS = new HashMap<>();

	public static final ResourceLocation TEXTURE_PRIMARY = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/block/portal/portal_blue_closed.png");
	public static final ResourceLocation TEXTURE_PRIMARY_VORTEX = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"block/portal/portal_blue_vortex");

	public static final ResourceLocation TEXTURE_HIGHLIGHT_PRIMARY = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/block/portal/portal_highlight_blue.png");
	public static final ResourceLocation TEXTURE_HIGHLIGHT_SECONDARY = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/block/portal/portal_highlight_orange.png");

	public static final ResourceLocation TEXTURE_SECONDARY = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/block/portal/portal_orange_closed.png");
	public static final ResourceLocation TEXTURE_SECONDARY_VORTEX = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"block/portal/portal_orange_vortex");

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

	public static void renderCameraPos(MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera, ClientPortalLink link, boolean isPimary)
	{
		poseStack.pushPose();
		PoseStack worldStack = new PoseStack();
		worldStack.pushPose();
		Vec3 pos = link.posPrimary().getCenter();
		poseStack.translate(-camera.getPosition().x + pos.x,
				-camera.getPosition().y + pos.y + 0.5f,
				-camera.getPosition().z + pos.z);
		worldStack.translate(pos.x, pos.y+0.5f, pos.z);

		worldStack.mulPose(link.directionPrimary().getRotation());
		poseStack.mulPose(link.directionPrimary().getRotation());

		if (link.wallPrimary())
		{
			poseStack.mulPose(Axis.XP.rotationDegrees(-90));
			worldStack.mulPose(Axis.XP.rotationDegrees(-90));
		}
		else {
			poseStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
			poseStack.mulPose(Axis.ZP.rotationDegrees(180));
			poseStack.translate(0f, 0.5f, -0.5f);
			if (link.ceilingPrimary())
				poseStack.translate(0f, 0f, 1f);

			worldStack.mulPose(Axis.XP.rotationDegrees(link.ceilingPrimary() ? 0 : 180));
			worldStack.mulPose(Axis.ZP.rotationDegrees(180));
			worldStack.translate(0f, 0.5f, -0.5f);
			if(link.ceilingPrimary())
				worldStack.translate(0f, 0f, 1f);
		}
		poseStack.translate(0.25f, 0f, 0.53f);
		worldStack.translate(0.25f, 0f, 0.53f);

		Vector3f camPos = new Vector3f().mul(worldStack.last().normal());
		camPos.mulProject(worldStack.last().pose());

		if(link.posSecondary() != null)
		{
			Vector3f newPos = camera.getPosition().toVector3f().sub(camPos);
		}

		PoseStack camStack = new PoseStack();

		camStack.pushPose();
		camStack.mulPose(Axis.YP.rotationDegrees(180));
		camStack.translate(10f, 0f, 0f);
		camStack.popPose();

		poseStack.translate(0f, 0f, 1f);
		poseStack.scale(1f, 1f, 1f);

		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portalFrame(TEXTURE_SECONDARY));
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
		worldStack.popPose();
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
		RenderSystem.enableDepthTest();

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
		RenderSystem.depthMask(false);

		if(!PortalViewingRenderer.FRAMEBUFFERS.containsKey(link.linkID()))
		{
			PortalViewingRenderer.requestPortalUpdate(link.linkID(), isPrimary);
		}
		Pair<RenderTarget, RenderTarget> pair = PortalViewingRenderer.FRAMEBUFFERS.get(link.linkID());

		RenderSystem.setShaderTexture(0, isPrimary ? pair.getSecond().getColorTextureId() : pair.getFirst().getColorTextureId());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		builder.vertex(matrix, -0.5f, -0.5f, 0).uv(0.0f, 0.0f).endVertex();
		builder.vertex(matrix,  0.5f, -0.5f, 0).uv(1f, 0.0f).endVertex();
		builder.vertex(matrix,  0.5f,  0.5f, 0).uv(1f, 1.0f).endVertex();
		builder.vertex(matrix, -0.5f,  0.5f, 0).uv(0.0f, 1.0f).endVertex();

		BufferUploader.drawWithShader(builder.end());

		RenderSystem.disableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.depthMask(true);

		poseStack.popPose();
	}

	public static void renderPortalFrame(ResourceLocation texture, MultiBufferSource buffer, PoseStack poseStack) {
		poseStack.pushPose();
		poseStack.scale(2f, 2f, 2f);

		float hue = 35;
		Color color = Color.getHSBColor(hue/360F, 0F, 1F);
		int argb = FastColor.ARGB32.color(255, color.getRed(), color.getGreen(), color.getBlue());
		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portalFrame(texture));
		consumerA.vertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .color(argb)
				 .uv(0, 1)
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .color(argb)
				 .uv(1, 1)
				 .endVertex();
		consumerA.vertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .color(argb)
				 .uv(1, 0).endVertex();
		consumerA.vertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .color(argb)
				 .uv(0, 0)
				 .endVertex();

		poseStack.popPose();
	}

	public static void renderPortalHighlight(MultiBufferSource buffer, PoseStack poseStack, boolean isPrimary) {
		poseStack.pushPose();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		Matrix4f matrix = poseStack.last().pose();

		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_GREATER);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, isPrimary ? TEXTURE_HIGHLIGHT_PRIMARY : TEXTURE_HIGHLIGHT_SECONDARY);

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		builder.vertex(matrix, -0.5f, -0.5f, 0).uv(0.0f, 1.0f).endVertex();
		builder.vertex(matrix,  0.5f, -0.5f, 0).uv(1f, 1.0f).endVertex();
		builder.vertex(matrix,  0.5f,  0.5f, 0).uv(1f, 0.0f).endVertex();
		builder.vertex(matrix, -0.5f,  0.5f, 0).uv(0.0f, 0.0f).endVertex();

		BufferUploader.drawWithShader(builder.end());

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.disableDepthTest();

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

		poseStack.pushPose();
		poseStack.translate(0.15625f, 0f, 0f);
		renderPortalHighlight(buffer, poseStack, isPrimary);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		poseStack.translate(0.3125f, 0f, 0f);
		renderPortalHighlight(buffer, poseStack, isPrimary);
		poseStack.popPose();

		poseStack.popPose();
	}

}
