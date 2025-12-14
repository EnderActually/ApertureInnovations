package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class PortalRenderer {
	private final Minecraft mc = Minecraft.getInstance();
	private final RenderTarget fbo;
	private final ArmorStand dummyEntity;

	public int recursion = 0;

	public UUID uuid;
	boolean primary;

	public PortalRenderer(int width, int height, UUID uuid, boolean primary) {
		fbo = new TextureTarget(width, height, true, Minecraft.ON_OSX);
		fbo.setClearColor(0.0F, 0.0F, 0.0F, 1.0F); // Initial black clear

		uuid = uuid;
		primary = primary;

		// Dummy entity for camera (client-side only, invisible, no physics)
		dummyEntity = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
		dummyEntity.noPhysics = true;
		dummyEntity.setNoGravity(true);
		dummyEntity.setInvisible(true);
	}

	/**
	 * Gets the texture ID to bind to your plane (replaces your pitch-black texture).
	 */
	public int getTextureId() {
		return fbo.getColorTextureId();
	}

	/**
	 * Renders the world view from the remote position/rotation to the texture.
	 * Call this sparingly (e.g., every 5-10 ticks) for efficiency.
	 * @param pos Remote camera position.
	 * @param yaw Remote camera yaw (horizontal rotation).
	 * @param pitch Remote camera pitch (vertical rotation).
	 * @param partialTicks Partial tick for smooth interpolation.
	 */
	public void renderView(Vector3d pos, float yaw, float pitch, float partialTicks) {
		if (mc.level == null || mc.player == null || recursion > 0)
		{
			recursion = 0;
			return;
		}

		// Update dummy entity to remote view
		dummyEntity.setPos(pos.x, pos.y, pos.z);
		dummyEntity.noPhysics = true;
		dummyEntity.setNoGravity(true);

		dummyEntity.setDeltaMovement(new Vec3(0, 0, 0));

		dummyEntity.xo = pos.x;
		dummyEntity.yo = pos.y;
		dummyEntity.zo = pos.z;

		dummyEntity.setXRot(pitch);
		dummyEntity.setYRot(yaw);
		dummyEntity.yRotO = yaw;
		dummyEntity.xRotO = pitch;

		dummyEntity.yHeadRotO = yaw;
		dummyEntity.yBodyRotO = yaw;

		dummyEntity.yHeadRot = yaw;
		dummyEntity.yBodyRot = yaw;

		Entity oldCameraEntity = mc.getCameraEntity();
		RenderTarget oldFbo = mc.getMainRenderTarget();

		try {
			// Temporarily override camera entity
			mc.setCameraEntity(dummyEntity);

			// Bind and prepare custom FBO
			fbo.bindWrite(true); // Sets viewport automatically
			RenderSystem.clear(16640, Minecraft.ON_OSX); // Clear color + depth

			// Render world to FBO (clips via frustum/depth)
			GameRenderer gameRenderer = mc.gameRenderer;

			Camera newCamera = new Camera();
			newCamera.setup(mc.level, dummyEntity, false, false, partialTicks);

			PoseStack poseStack = new PoseStack();

			poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
			poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

			mc.renderBuffers().bufferSource().endBatch();
			//gameRenderer.renderLevel(partialTicks, System.nanoTime(), new PoseStack());
			recursion++;

			mc.levelRenderer.renderLevel(poseStack, partialTicks, System.nanoTime(), false, newCamera,
					gameRenderer, gameRenderer.lightTexture(), gameRenderer.getProjectionMatrix(70));
		} catch (Exception e) {
			// Safety: Log error but don't crash
			e.printStackTrace();
		} finally {
			// Restore states
			mc.setCameraEntity(oldCameraEntity);
			oldFbo.bindWrite(true);
		}
	}

	/**
	 * Cleanup (call when no longer needed, e.g., on mod unload).
	 */
	public void cleanup() {
		fbo.destroyBuffers();
	}
}
