package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.mistersecret312.aperture_innovations.events.ClientEvents;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import org.joml.*;

import java.lang.Math;
import java.lang.reflect.Field;
import java.util.*;

import static net.minecraft.client.Minecraft.ON_OSX;
import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.LINKS;

public class PortalViewingRenderer
{
	public static final Map<UUID, Pair<RenderTarget, RenderTarget>> FRAMEBUFFERS = new HashMap<>();
	private static final Map<UUID, Pair<Camera, Camera>> DUMMIES = new HashMap<>();

	//TODO : Config-based resolution
	private static final int TEXTURE_SIZE = 512;
	private static final int LARGER_SIZE = 1024;

	public static RenderTarget PORTAL_BEING_RENDERED = null;
	public static boolean rendering = false;

	public static void requestPortalUpdate(UUID uuid, boolean isPrimary)
	{
		FRAMEBUFFERS.computeIfAbsent(uuid, k -> {
			TextureTarget fb1 = new TextureTarget(TEXTURE_SIZE, LARGER_SIZE, true, ON_OSX);
			TextureTarget fb2 = new TextureTarget(TEXTURE_SIZE, LARGER_SIZE, true, ON_OSX);

			return Pair.of(fb1, fb2);
		});
		DUMMIES.computeIfAbsent(uuid, k -> Pair.of(new Camera(), new Camera()));

		Minecraft mc = Minecraft.getInstance();

		if(mc.level == null)
			return;

		RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
		float partialTick = mc.getPartialTick();

		Pair<RenderTarget, RenderTarget> pair = FRAMEBUFFERS.get(uuid);
		RenderTarget fb = isPrimary ? pair.getFirst() : pair.getSecond();
		Camera dummy = isPrimary ?  DUMMIES.get(uuid).getFirst() : DUMMIES.get(uuid).getSecond();

		setupPortalCam(LINKS.get(uuid));

		fb.bindWrite(true);
		PortalViewingRenderer.PORTAL_BEING_RENDERED = fb;

		RenderSystem.clear(16640, ON_OSX);
		FogRenderer.setupNoFog();
		RenderSystem.enableCull();

		ClientPortalLink link = LINKS.get(uuid);

		Vec3 portalPos = PortalUtilities.getPortalPos(mc.level, uuid, !isPrimary);
		Direction portalDirection = PortalUtilities.getPortalDirection(mc.level, uuid, !isPrimary);

		BlockPos pos = isPrimary ? link.posPrimary() : link.posSecondary();

		boolean isVisible = Minecraft.getInstance().levelRenderer.getFrustum().isVisible(
				new AABB(pos).inflate(2f, 2f, 2f));
		boolean isClose = Minecraft.getInstance().player
								  .distanceToSqr((pos).getCenter())
								  < Math.pow(Minecraft.getInstance().gameRenderer.getRenderDistance(), 2);
		boolean isVeryClose = Minecraft.getInstance().player
								  .distanceToSqr((pos).getCenter())
								  < Math.pow(32, 2);

		if(isClose && !rendering)
		{
			if(isVisible || isVeryClose)
				renderLevel(mc, fb, dummy, 70, partialTick, System.nanoTime());
		}

		PortalViewingRenderer.PORTAL_BEING_RENDERED = null;
		mainTarget.bindWrite(true);
		rendering = false;

		RenderSystem.clear(16640, ON_OSX);
	}

	private static void renderLevel(Minecraft mc, RenderTarget target, Camera camera,
									float fov, float partialTick, long finishTimeNano)
	{
		GameRenderer gr = mc.gameRenderer;
		LevelRenderer lr = mc.levelRenderer;

		Matrix4f projMatrix = createProjectionMatrix(gr, target, fov);
		PoseStack poseStack = new PoseStack();

		Quaternionf cameraRotation = camera.rotation().conjugate(new Quaternionf());
		Matrix4f cameraMatrix = (new Matrix4f()).rotation(cameraRotation);
		poseStack.mulPoseMatrix(cameraMatrix);
		gr.resetProjectionMatrix(projMatrix);

		rendering = true;

		lr.prepareCullFrustum(poseStack, camera.getPosition(), projMatrix);
		lr.renderLevel(poseStack, partialTick, finishTimeNano, false, camera, gr,
				gr.lightTexture(), projMatrix);

		ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_LEVEL, lr, poseStack,
				projMatrix, lr.getTicks(), camera, lr.getFrustum());
	}

	private static void setupPortalCam(ClientPortalLink link) {
		Pair<Camera, Camera> camera = DUMMIES.get(link.linkID());
		for(int i = 0; i < 2; i++)
		{
			Camera dummy = new Camera();
			Vec3 portalPos = new Vec3(0f, 0f, 0f);
			Vec3 cameraPos = new Vec3(0f, 0f, 0f);

			cameraPos = PortalUtilities.getPortalPos(Minecraft.getInstance().level, link.linkID(), i == 0);

			Vec3 offsetPrimary = Vec3.atLowerCornerOf(link.directionPrimary().getNormal()).multiply(0.5f, 0.5f, 0.5f);
			Vec3 offsetSecondary = Vec3.atLowerCornerOf(link.directionSecondary().getNormal()).multiply(0.5f, 0.5f, 0.5f);

			boolean isPrimary = true;
			if(i == 0)
			{
				dummy = camera.getFirst();
				portalPos = link.posSecondary().getCenter().add(0f, 0.5f, 0f).add(offsetSecondary);
				cameraPos = link.posPrimary().getCenter().add(0f, 0.5f, 0f).add(offsetPrimary);

				isPrimary = true;
			}
			if(i == 1)
			{
				dummy = camera.getSecond();
				portalPos = link.posPrimary().getCenter().add(0f, 0.5f, 0f).add(offsetPrimary);
				cameraPos = link.posSecondary().getCenter().add(0f, 0.5f, 0f).add(offsetSecondary);
				isPrimary = false;
			}

			if (dummy.entity == null) {
				dummy.entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, Minecraft.getInstance().level);
			}

			Player player = Minecraft.getInstance().player;
			float yRotDiff = link.directionSecondary().toYRot()-link.directionPrimary().toYRot()+180f;

			Vec3 playerPos = player.getBoundingBox().getCenter();
			Vec3 playerPortalVec = playerPos.subtract(portalPos);

			cameraPos = cameraPos.subtract(new Vec3(playerPortalVec.x, -playerPortalVec.y, playerPortalVec.z));

			playerPortalVec = portalPos.subtract(playerPos);

			double r = Math.sqrt(playerPortalVec.x * playerPortalVec.x + playerPortalVec.y * playerPortalVec.y + playerPortalVec.z * playerPortalVec.z);
			float xRot = (float) (Math.toDegrees(Math.acos(playerPortalVec.y/r))-90);
			float yRot = (float) (Math.toDegrees(Math.atan2(playerPortalVec.z, playerPortalVec.x))+270);

			yRot = Mth.wrapDegrees(yRot + (isPrimary ? yRotDiff : -yRotDiff));

//			float xRot = isPrimary ? link.wallPrimary() ? 0 : link.ceilingPrimary() ? -90 : 90 : link.wallSecondary() ? 0 : link.ceilingSecondary() ? -90 : 90;

			if(link.directionPrimary().getOpposite() == link.directionSecondary())
				yRot -= isPrimary ? 180 : 0;
			if(link.directionSecondary() == link.directionPrimary())
				yRot += isPrimary ? 180 : 0;
			if(link.directionSecondary().getAxis() != link.directionPrimary().getAxis() && !link.ceilingPrimary() && !link.ceilingSecondary())
				yRot += isPrimary ? 0 : 180;

			yRot += isPrimary ? link.wallPrimary() ? 180 : 0
			: link.wallSecondary() ? 0 : 180;

			if(isPrimary ? !link.wallSecondary() : !link.wallPrimary())
			{
				Direction direction = Direction.fromYRot(yRot);
				if(yRot > direction.toYRot()-90)
					yRot -= direction.toYRot()+90;

				cameraPos = cameraPos.add(Vec3.atLowerCornerOf(
													  isPrimary ? link.directionPrimary().getNormal() : link.directionSecondary().getNormal())
											  .multiply(-0.5f, -0.5f, -0.5f));
			}
			//portalPos = portalPos.subtract(playerPortalVec.xRot((float) Math.toRadians(xRot)).yRot((float) Math.toRadians(yRot-90)));

			Entity dummyCameraEntity = dummy.getEntity();

			cameraPos = PortalUtilities.getPortalPos(Minecraft.getInstance().level, link.linkID(), isPrimary);

			//cameraPos = cameraPos.add(Vec3.directionFromRotation(xRot, yRot).scale(-r));
			xRot = -xRot;

//			Minecraft.getInstance().level.addParticle(ParticleTypes.DRAGON_BREATH, cameraPos.x, cameraPos.y, cameraPos.z,
//					0d,0d,0d);
//			Vec3 cameraDirectionPos = cameraPos.add(Vec3.directionFromRotation(xRot, yRot).scale(2));
//			Minecraft.getInstance().level.addParticle(ParticleTypes.FLAME, cameraDirectionPos.x, cameraDirectionPos.y, cameraDirectionPos.z,
//					0d, 0d, 0d);

			dummyCameraEntity.setPos(cameraPos);

			dummyCameraEntity.setXRot(xRot);
			dummyCameraEntity.setYRot(yRot + 180);

			dummy.setPosition(cameraPos);
			dummy.setRotation(yRot+180, xRot);
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
		return matrix4f.setPerspective((float) (fov * Math.PI / 180F),
				(float) target.width / (float) target.height, 0.05f, depthFar);
	}
}