package net.mistersecret312.aperture_innovations.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.init.ItemInit;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
					boolean isPrimary = i == 0;
					ResourceKey<Level> dimension = isPrimary ? link.getPrimaryPortal().getDimension() :
														   link.getSecondaryPortal().getDimension();
					if(level.dimension() != dimension)
						continue;

					poseStack.pushPose();

					float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(linkID, isPrimary);

					Vec3 portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();
					if(link.getPrimaryPortal().isInWorld() && link.getSecondaryPortal().isInWorld())
					{
						if(level.isLoaded(BlockPos.containing(portalPos)))
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

				{
					poseStack.pushPose();

					Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(Minecraft.getInstance().player);
					UUID uuid = portal.getFirst();
					boolean isPrimary = portal.getSecond();
					if(uuid == null) return;

					Level level = Minecraft.getInstance().level;

					Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
					Vec2 rotation = PortalUtilities.getPortalRotation(level, uuid, isPrimary);

					AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, rotation.x, rotation.y);
					AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, rotation.x, rotation.y);
					AABB placementBox = PortalUtilities.getPortalPlacementBox(portalPos, rotation.x, rotation.y);

					AABB floorBox = PortalUtilities.getPortalFloorBox(portalPos, rotation.x, rotation.y);

					List<VoxelShape> shapesIDK = PortalUtilities.getPortalVoxels(level, portalPos, rotation.x,
							rotation.y);

					poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

					AtomicReference<VoxelShape> placementShape = new AtomicReference<>(
							Shapes.create(placementBox.inflate(0.025)));
					AtomicReference<VoxelShape> bumpingShape = new AtomicReference<>(Shapes.create(placementBox));
					if(true)
					{
						BlockPos.betweenClosedStream(portalBox.inflate(0.025)).forEach(pos ->
							{
								BlockState state = level.getBlockState(pos);
								if(!state.isAir())
								{
									VoxelShape shape = state.getCollisionShape(level, pos)
															.move(pos.getX(), pos.getY(), pos.getZ());
									if(!placementShape.get().isEmpty()) placementShape.set(
											Shapes.join(placementShape.get(), shape, BooleanOp.ONLY_FIRST));
									if(!bumpingShape.get().isEmpty())
										bumpingShape.set(Shapes.join(bumpingShape.get(), shape, BooleanOp.ONLY_FIRST));
								}
							});

						//False - to have a look at it bumping with Air, True - to have a look at it bumping with VoxelShapes of blocks
						if(!placementShape.get().isEmpty() && false)
						{
							AABB placementAABB = placementBox.inflate(0.025);

							Direction direction = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
							boolean wall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);

							if(wall)
							{
								if(direction.getAxis().equals(Direction.Axis.X))
								{
									placementAABB = placementAABB.setMinX(placementShape.get().bounds().minX);
									placementAABB = placementAABB.setMaxX(placementShape.get().bounds().maxX);
								}

								if(direction.getAxis().equals(Direction.Axis.Z))
								{
									placementAABB = placementAABB.setMinZ(placementShape.get().bounds().minZ);
									placementAABB = placementAABB.setMaxZ(placementShape.get().bounds().maxZ);
								}
							} else
							{
								placementAABB = placementAABB.setMinY(placementShape.get().bounds().minY);
								placementAABB = placementAABB.setMaxY(placementShape.get().bounds().maxY);
							}

							bumpingShape.set(Shapes.join(Shapes.create(placementAABB), placementShape.get(),
									BooleanOp.ONLY_FIRST));
						}

						if(!placementShape.get().toAabbs().isEmpty())
						{
							AABB firstPart = placementShape.get().toAabbs().getFirst();

							Direction direction = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
							boolean wall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);
							boolean ceiling = PortalUtilities.isPortalOnCeiling(level, uuid, isPrimary);

							AABB placement = placementBox.inflate(0.025);
							boolean equal = false;
							if(!wall)
							{
								equal = firstPart.maxX == placement.maxX && firstPart.minX == placement.minX
												&& firstPart.maxZ == placement.maxZ && firstPart.minZ == placement.minZ;
							}
							if(wall)
							{
								if(direction.getAxis().equals(Direction.Axis.Z))
								{
									equal = firstPart.maxX == placement.maxX && firstPart.minX == placement.minX
													&& firstPart.maxY == placement.maxY && firstPart.minY == placement.minY;
								}
								if(direction.getAxis().equals(Direction.Axis.X))
								{
									equal = firstPart.maxY == placement.maxY && firstPart.minY == placement.minY
													&& firstPart.maxZ == placement.maxZ && firstPart.minZ == placement.minZ;
								}
							}
							if(!equal || placementShape.get().toAabbs().size() != 1)
							{
								System.out.println("Invalid Portal Placement! - Client");
							}
						}

						LevelRenderer.renderVoxelShape(poseStack, buffer.getBuffer(PortalRenderTypes.lines()),
								placementShape.get(), 0, 0, 0, 1f, 0.2f, 0.6f, 1f, false);

//						LevelRenderer.renderVoxelShape(poseStack, buffer.getBuffer(PortalRenderTypes.lines()),
//								bumpingShape.get(), 0, 0, 0, 0.25f, 1f, 0.5f, 1f, false);
					}
					for(VoxelShape voxelShape : shapesIDK)
					{
//						LevelRenderer.renderVoxelShape(poseStack, buffer.getBuffer(PortalRenderTypes.lines()),
//								voxelShape, 0, 0, 0, 1f, 1f, 0f, 1f, false);
					}
//					LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(PortalRenderTypes.lines()), portalBox, 0f,
//							1f, 1f, 1f);
//					LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(PortalRenderTypes.lines()), placementBox,
//							0.87f, 0.25f, 0.15f, 1f);
//					LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(PortalRenderTypes.lines()), teleportBox,
//							1f, 1f, 0f, 1f);
//					LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(PortalRenderTypes.lines()), floorBox, 1f,
//							0f, 0f, 1f);

					poseStack.popPose();
				}

				ResourceLocation texturePrimary = ClientPortalUtilities.getPortalVortexTexture(link, true);
				ResourceLocation textureSecondary = ClientPortalUtilities.getPortalVortexTexture(link, false);

				final TextureAtlasSprite primary = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															.apply(texturePrimary);
				final TextureAtlasSprite secondary = Minecraft.getInstance()
															  .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															  .apply(textureSecondary);

				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					ResourceKey<Level> dimension = isPrimary ? link.getPrimaryPortal().getDimension() : link.getSecondaryPortal().getDimension();
					if(Minecraft.getInstance().level.dimension() != dimension) continue;

					float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(link.linkID(), isPrimary);

					Vec3 portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();

					if(portalPos != null
							   && Minecraft.getInstance().level.isLoaded(BlockPos.containing(portalPos))
							   && event.getLevelRenderer().getFrustum().isVisible(new AABB(portalPos, portalPos).inflate(1)))
					{
						if(isPrimary)
							primaryRender(link, buffer, poseStack, camera, scale);
						else
							secondaryRender(link, buffer, poseStack, camera, scale);
					}

					if(isPrimary && link.getPrimaryPortal().isInWorld())
						renderPortalVortex(link, camera, primary, buffer, poseStack, true);
					else if(link.getSecondaryPortal().isInWorld())
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

		if(event.getOverlayType().equals(RenderBlockScreenEffectEvent.OverlayType.BLOCK))
		{
			Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(player);
			UUID uuid = portal.getFirst();
			boolean isPrimary = portal.getSecond();
			if(uuid == null)
				return;

			Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
			Vec2 rotation = PortalUtilities.getPortalRotation(level, uuid, isPrimary);

			AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, rotation.x, rotation.y);
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
		UUID uuid = gunItem.getUUID(gunItemStack, false);

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
						Vec3 portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();

						if(portalPos != null)
						{
							float progress = ClientPortalUtilities.getPortalOpeningAnimationProgress(linkID, isPrimary);
							if(progress < 1F)
							{
								progress += 0.25f;
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

}
