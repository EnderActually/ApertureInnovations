package net.mistersecret312.aperture_innovations.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	static int tickCounter = 0;

	public static HashMap<UUID, ClientPortalLink> LINKS = new HashMap<>();
	public static HashMap<UUID, Pair<PortalRenderer, PortalRenderer>> PORTALS = new HashMap<>();

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
				if(!PORTALS.containsKey(linkID))
				{
					PORTALS.put(linkID, new Pair<>(new PortalRenderer(512, 512, linkID, true),
							new PortalRenderer(512, 512, linkID, false)));
				}

				if(link.posPrimary() != null)
				{

					MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

					poseStack.pushPose();

					Vec3 pos = link.posPrimary().getCenter();

					Pair<PortalRenderer, PortalRenderer> renderers = PORTALS.get(linkID);

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

					if(link.posSecondary() != null)
					{
						poseStack.pushPose();
						poseStack.translate(0f, 0f, 0.01f);
						renderPortal(buffer, poseStack, renderers.getFirst());
						poseStack.popPose();
					}
					renderPortalFrame(TEXTURE_PRIMARY, buffer, poseStack);
					renderPortalVortex(sprite, buffer, poseStack);

					poseStack.popPose();
				}

				if(link.posSecondary() != null)
				{
					MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

					poseStack.pushPose();

					Vec3 pos = link.posSecondary().getCenter();

					Pair<PortalRenderer, PortalRenderer> renderers = PORTALS.get(linkID);

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


					//renderPortalFrame(TEXTURE_SECONDARY, buffer, poseStack);
					if(link.posPrimary() != null)
					{
						poseStack.pushPose();
						poseStack.translate(0f, 0f, 0.01f);
						renderPortal(buffer, poseStack, renderers.getSecond());
						poseStack.popPose();
					}
					renderPortalFrame(TEXTURE_SECONDARY, buffer, poseStack);
					renderPortalVortex(sprite, buffer, poseStack);

					poseStack.popPose();
				}
			});
		}
	}

	public static void renderPortal(MultiBufferSource buffer, PoseStack poseStack, PortalRenderer renderer)
	{
		if (buffer instanceof MultiBufferSource.BufferSource) {
			((MultiBufferSource.BufferSource) buffer).endBatch();
		}

		poseStack.pushPose();
		poseStack.scale(2f, 2f, 2f);
		poseStack.translate(-0.25f, 0f, 0f);

		// Get the immediate renderer
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		Matrix4f matrix = poseStack.last().pose();

		// =============================================================
		// PASS 1: THE MASK (Write to Depth Buffer Only)
		// =============================================================

		// 1. Disable writing to Color (we don't want to see the black oval)
		RenderSystem.colorMask(false, false, false, false);

		// 2. Bind the Mask Texture
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, new ResourceLocation(ApertureInnovations.MODID, "textures/block/portal/portal_mask.png"));


		// 3. Draw the Mask Quad immediately
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		builder.vertex(matrix, -0.5f, -0.5f, 0).uv(0f, 0f).endVertex();
		builder.vertex(matrix,  0.5f, -0.5f, 0).uv(1f, 0f).endVertex();
		builder.vertex(matrix,  0.5f,  0.5f, 0).uv(1f, 1f).endVertex();
		builder.vertex(matrix, -0.5f,  0.5f, 0).uv(0f, 1f).endVertex();
		tesselator.end(); // Draws NOW

		// 4. Re-enable Color writing
		RenderSystem.colorMask(true, true, true, true);

		// =============================================================
		// PASS 2: THE VIEW (Draw FBO inside the Mask)
		// =============================================================

		// 1. Set Depth Function to EQUAL.
		// This means: "Only draw pixels if they land exactly where the Mask was drawn"
		RenderSystem.depthFunc(GL11.GL_EQUAL);

		// 2. Bind the FBO Texture (Your captured view)
		RenderSystem.setShaderTexture(0, renderer.getTextureId());

		// 3. Draw the View Quad immediately
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		// Note: We use white color so the texture shows naturally
		builder.vertex(matrix, -0.5f, -0.5f, 0).uv(0f, 0f).endVertex();
		builder.vertex(matrix,  0.5f, -0.5f, 0).uv(1f, 0f).endVertex();
		builder.vertex(matrix,  0.5f,  0.5f, 0).uv(1f, 1f).endVertex();
		builder.vertex(matrix, -0.5f,  0.5f, 0).uv(0f, 1f).endVertex();
		tesselator.end(); // Draws NOW

		// 4. Restore Depth Function to default (LEQUAL) so we don't break the rest of the game rendering
		RenderSystem.depthFunc(GL11.GL_LEQUAL);

		poseStack.popPose();
	}

	public static void renderPortalFrame(ResourceLocation texture, MultiBufferSource buffer, PoseStack poseStack)
	{
		poseStack.pushPose();
		poseStack.scale(2f,2f,2f);

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

			PORTALS.forEach((linkID, pair) -> {
				ClientPortalLink link = LINKS.get(linkID);
				if(link.posPrimary() != null && link.posSecondary() != null)
				{
					BlockPos posPrimary = link.posSecondary();
					BlockPos posSecondary = link.posPrimary();

					pair.getFirst().renderView(new Vector3d(posPrimary.getX(), posPrimary.getY()+1, posPrimary.getZ()),
							link.directionSecondary().getOpposite().toYRot(), 0, 0f);
					pair.getSecond().renderView(new Vector3d(posSecondary.getX(), posSecondary.getY()+1, posSecondary.getZ()),
							link.directionPrimary().getOpposite().toYRot(), 0, 0f);
				}
			});
		}
	}
}
