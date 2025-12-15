package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.events.ClientEvents;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;

import static net.minecraft.client.Minecraft.ON_OSX;

public class PortalRenderer {
	private static final Map<UUID, Pair<DynamicTexture, DynamicTexture>> TEXTURES = new HashMap<>();
	private static final Map<UUID, RenderTarget> FRAMEBUFFERS = new HashMap<>();
	private static final Map<UUID, Pair<Camera, Camera>> DUMMIES = new HashMap<>();
	private static final int TEXTURE_SIZE = 64;
	private static final int LARGER_SIZE = 128;
	public static final int UPDATE_INTERVAL = 20;

	public static int recursion = 0;

	public static Pair<DynamicTexture, DynamicTexture> getTexture(UUID uuid) {
		TEXTURES.computeIfAbsent(uuid, k -> Pair.of(new DynamicTexture(TEXTURE_SIZE, LARGER_SIZE, true),
				new DynamicTexture(TEXTURE_SIZE, LARGER_SIZE, true)));
		FRAMEBUFFERS.computeIfAbsent(uuid, k -> {
			TextureTarget fb = new TextureTarget(TEXTURE_SIZE, LARGER_SIZE, true, ON_OSX);
			fb.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
			return fb;
		});
		DUMMIES.computeIfAbsent(uuid, k -> Pair.of(new Camera(), new Camera()));
		return TEXTURES.get(uuid);
	}
	public static void updateTextures(UUID uuid) {
		if (TEXTURES.isEmpty()) return;

		if (recursion >= 5) return;

		Minecraft mc = Minecraft.getInstance();
		GameRenderer gameRenderer = mc.gameRenderer;
		LightTexture lightTexture = gameRenderer.lightTexture();

		float partialTick = mc.getFrameTime();
		long finishTimeNano = System.nanoTime() + 500000000L;

		RenderTarget originalTarget = mc.getMainRenderTarget();
		Matrix4f originalProjection = RenderSystem.getProjectionMatrix();

		Pair<DynamicTexture, DynamicTexture> pair = TEXTURES.get(uuid);

		if (recursion < 5) {
			recursion++;
			try {
				DynamicTexture texture = pair.getFirst();
				RenderTarget fb = FRAMEBUFFERS.get(uuid);
				ClientPortalLink link = ClientEvents.LINKS.get(uuid);

				RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
				RenderSystem.enableDepthTest();
				RenderSystem.depthMask(true);

				setupSceneCamera(link);

				double fov = 70;
				Matrix4f projectionMatrix = gameRenderer.getProjectionMatrix(fov);

				RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
				fb.bindWrite(true);
				RenderSystem.clear(16640, ON_OSX);
				FogRenderer.setupNoFog();
				RenderSystem.enableCull();

				lightTexture.updateLightTexture(partialTick);

				gameRenderer.checkEntityPostEffect(DUMMIES.get(link.linkID()).getFirst().getEntity());

				renderLevel(mc, fb, DUMMIES.get(link.linkID()).getFirst(), (float) fov, partialTick, finishTimeNano);

				mc.renderBuffers().bufferSource().endBatch();

				NativeImage image = new NativeImage(NativeImage.Format.RGBA, TEXTURE_SIZE, LARGER_SIZE, false);
				RenderSystem.bindTexture(fb.getColorTextureId());
				image.downloadTexture(0, false);
				image.flipY();
				texture.setPixels(image);
				texture.upload();
				image.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				recursion--;
			}
		}

		if (recursion < 5) {
			recursion++;
			try {
				DynamicTexture texture = pair.getSecond();
				RenderTarget fb = FRAMEBUFFERS.get(uuid);
				ClientPortalLink link = ClientEvents.LINKS.get(uuid);

				RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
				RenderSystem.enableDepthTest();
				RenderSystem.depthMask(true);

				setupSceneCamera(link);

				double fov = 70;
				Matrix4f projectionMatrix = gameRenderer.getProjectionMatrix(fov);

				RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
				fb.bindWrite(true);
				RenderSystem.clear(16640, ON_OSX);
				FogRenderer.setupNoFog();
				RenderSystem.enableCull();

				lightTexture.updateLightTexture(partialTick);

				gameRenderer.checkEntityPostEffect(DUMMIES.get(link.linkID()).getSecond().getEntity());
				renderLevel(mc, fb, DUMMIES.get(link.linkID()).getSecond(), (float) fov, partialTick, finishTimeNano);
				mc.renderBuffers().bufferSource().endBatch();

				NativeImage image = new NativeImage(NativeImage.Format.RGBA, TEXTURE_SIZE, LARGER_SIZE, false);
				RenderSystem.bindTexture(fb.getColorTextureId());
				image.downloadTexture(0, false);
				image.flipY();
				texture.setPixels(image);
				texture.upload();
				image.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				recursion--;
			}
		}

		RenderSystem.setProjectionMatrix(originalProjection, VertexSorting.DISTANCE_TO_ORIGIN);
		originalTarget.bindWrite(true);
	}
	private static void renderLevel(Minecraft mc, RenderTarget target, Camera camera, float fov, float partialTick, long finishTimeNano)
	{
		GameRenderer gr = mc.gameRenderer;
		LevelRenderer lr = mc.levelRenderer;
		Matrix4f projMatrix = createProjectionMatrix(gr, target, fov);
		PoseStack poseStack = new PoseStack();

		Quaternionf cameraRotation = camera.rotation().conjugate(new Quaternionf());
		Matrix4f cameraMatrix = (new Matrix4f()).rotation(cameraRotation);
		poseStack.mulPoseMatrix(cameraMatrix);
		gr.resetProjectionMatrix(projMatrix);

		lr.prepareCullFrustum(poseStack, camera.getPosition(), projMatrix);
		lr.renderLevel(poseStack, partialTick, finishTimeNano, false, camera, gr,
				gr.lightTexture(), projMatrix);
	}
	@SuppressWarnings("ConstantConditions")
	private static void setupSceneCamera(ClientPortalLink link) {
		Pair<Camera, Camera> camera = DUMMIES.get(link.linkID());
		for(int i = 0; i < 2; i++)
		{
			Camera dummy = new Camera();
			boolean isPrimary = true;
			if(i == 0)
			{
				dummy = camera.getFirst();
				isPrimary = true;
			}
			if(i == 1)
			{
				dummy = camera.getSecond();
				isPrimary = false;
			}

			if (dummy.entity == null) {
				dummy.entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, Minecraft.getInstance().level);
			}

			Entity dummyCameraEntity = dummy.getEntity();
			Vec3 pos = isPrimary ? link.posPrimary().getCenter().add(0, 0.5, -0.5)
							   : link.posSecondary().getCenter().add(0, 0.5, 0.5);
			dummyCameraEntity.setPos(pos);
			float xRot = isPrimary ?  link.wallPrimary() ? 0f : link.ceilingPrimary() ? -90 : 90
								 : link.wallSecondary() ? 0f : link.ceilingSecondary() ? -90 : 90;
			float yRot = isPrimary ? link.directionPrimary().getOpposite().toYRot()
								 : link.directionSecondary().getOpposite().toYRot();
			dummyCameraEntity.setXRot(xRot);
			dummyCameraEntity.setYRot(yRot + 180);
			dummy.setPosition(pos);
			dummy.setRotation(yRot, xRot);
		}

	}
	private static Matrix4f createProjectionMatrix(GameRenderer gr, RenderTarget target, float fov) {
		Matrix4f matrix4f = new Matrix4f();
		float zoom = 1;
		if (zoom != 1.0F) {
			float zoomX = 0;
			float zoomY = 0;
			matrix4f.translate(zoomX, -zoomY, 0.0F);
			matrix4f.scale(zoom, zoom, 1.0F);
		}
		float depthFar = gr.getDepthFar();
		return matrix4f.perspective(fov * Mth.DEG_TO_RAD,
				(float) target.width / (float) target.height, 0.05F, depthFar);
	}
}