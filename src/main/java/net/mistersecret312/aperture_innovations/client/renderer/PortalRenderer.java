package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.client.ColorUtil;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.ClientPortalUtilities;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.UUID;


public class PortalRenderer
{
	public static HashMap<UUID, ClientPortalLink> LINKS = new HashMap<>();

	public static void primaryRender(ClientPortalLink link, MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera, float scale) {
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

			poseStack.scale(scale, scale, 1);
			poseStack.translate(0.5f, 0f, 0.51f);

			renderPortalFrame(ClientPortalUtilities.getPortalClosedTexture(link, true), ClientPortalUtilities.getPortalColor(link, true), buffer, poseStack);

			poseStack.popPose();
		}
	}

	public static void secondaryRender(ClientPortalLink link, MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera, float scale) {
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
			poseStack.scale(scale, scale, 1f);
			poseStack.translate(0.5f, 0f, 0.51f);

			renderPortalFrame(ClientPortalUtilities.getPortalClosedTexture(link, false), ClientPortalUtilities.getPortalColor(link, false),
					buffer, poseStack);

			poseStack.popPose();
		}
	}

	public static void renderPortalNonSee(MultiBufferSource buffer, PoseStack poseStack, Camera camera, ClientPortalLink link, boolean isPrimary, float scale)
	{
		poseStack.pushPose();
		Vec3 pos = isPrimary ? link.posPrimary().getCenter() : link.posSecondary().getCenter();
		Direction direction = isPrimary ? link.directionPrimary() : link.directionSecondary();


		poseStack.translate(pos.x - camera.getPosition().x, pos.y - camera.getPosition().y + 0.5,
				pos.z - camera.getPosition().z);
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
		poseStack.scale(scale, scale, scale);

		VertexConsumer consumerA = buffer.getBuffer(
				PortalRenderTypes.portal(isPrimary ? link.getVariant().primaryPortal.getMaskTexture()
												 : link.getVariant().secondaryPortal.getMaskTexture()));

		consumerA.addVertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .setUv(0, 1)
				 .setColor(FastColor.ABGR32.color(255, 255, 255, 255));
		consumerA.addVertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .setUv(1, 1)
				 .setColor(FastColor.ABGR32.color(255, 255, 255, 255));
		consumerA.addVertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .setUv(1, 0)
				 .setColor(FastColor.ABGR32.color(255, 255, 255, 255));
		consumerA.addVertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .setUv(0, 0)
				 .setColor(FastColor.ABGR32.color(255, 255, 255, 255));
		poseStack.popPose();
	}

	public static void renderPortalFrame(ResourceLocation texture, ColorUtil.RGBA color, MultiBufferSource buffer, PoseStack poseStack) {
		poseStack.pushPose();
		poseStack.scale(2f, 2f, 2f);
		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portalFrame(texture));
		consumerA.addVertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .setUv(0, 1)
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());
		consumerA.addVertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .setUv(1, 1)
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());
		consumerA.addVertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .setUv(1, 0)
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());
		consumerA.addVertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .setUv(0, 0)
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());

		poseStack.popPose();
	}

	public static void renderPortalHighlight(MultiBufferSource buffer, PoseStack poseStack,
											 ResourceLocation texture, ColorUtil.RGBA color, boolean isPrimary) {
		poseStack.pushPose();
		Tesselator tesselator = Tesselator.getInstance();
		Matrix4f matrix = poseStack.last().pose();

		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_GREATER);

		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShaderColor(color.red(), color.green(), color.blue(), color.alpha());

		BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		builder.addVertex(matrix, -0.5f, -0.5f, 0).setUv(0.0f, 1.0f);
		builder.addVertex(matrix,  0.5f, -0.5f, 0).setUv(1f, 1.0f);
		builder.addVertex(matrix,  0.5f,  0.5f, 0).setUv(1f, 0.0f);
		builder.addVertex(matrix, -0.5f,  0.5f, 0).setUv(0.0f, 0.0f);

		BufferUploader.drawWithShader(builder.buildOrThrow());
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

		GL11.glDisable(GL11.GL_STENCIL_TEST);
		if(!isPrimary)
			RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);

		RenderSystem.disableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);

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
		float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(link.linkID(), isPrimary);
		poseStack.translate(-0.3125f, 0f, 0.005f);

		poseStack.scale(scale, scale, scale);

		poseStack.scale(2f, 2f, 2f);

		ColorUtil.RGBA color = ClientPortalUtilities.getPortalColor(link, isPrimary);

		VertexConsumer consumerA = buffer.getBuffer(PortalRenderTypes.portalVortex(sprite.atlasLocation()));
		consumerA.addVertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .setUv(sprite.getU0(), sprite.getV1())
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());
		consumerA.addVertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .setUv(sprite.getU1(), sprite.getV1())
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());
		consumerA.addVertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .setUv(sprite.getU1(), sprite.getV0())
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());
		consumerA.addVertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .setUv(sprite.getU0(), sprite.getV0())
				 .setColor(color.red(), color.green(), color.blue(), color.alpha());

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
		if (hasPortalGun)
		{
			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			UUID linkID = portalGun.getUUID(gunStack, false);
			if(linkID != null && linkID.equals(link.linkID()))
			{
				ResourceLocation texture = ClientPortalUtilities.getPortalHighlightTexture(link, isPrimary);

				poseStack.pushPose();
				poseStack.translate(0.15625f, 0f, 0f);
				renderPortalHighlight(buffer, poseStack, texture, color, isPrimary);
				poseStack.popPose();

				poseStack.pushPose();
				poseStack.mulPose(Axis.YP.rotationDegrees(180));
				poseStack.translate(0.3125f, 0f, 0f);
				renderPortalHighlight(buffer, poseStack, texture, color, isPrimary);
				poseStack.popPose();
			}
		}

		poseStack.popPose();
	}

}
