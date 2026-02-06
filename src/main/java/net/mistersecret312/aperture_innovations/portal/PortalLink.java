package net.mistersecret312.aperture_innovations.portal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.AdvancementInit;
import net.mistersecret312.aperture_innovations.init.StatisticsInit;
import net.mistersecret312.aperture_innovations.network.ClientboundEntityPortalLerpPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalAmbientSoundPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PortalLink
{
	public UUID linkID;

	private Portal primaryPortal;
	private Portal secondaryPortal;

	public ResourceLocation variantKey = null;

	public PortalLink(UUID linkID, ResourceLocation variantKey)
	{
		this.linkID = linkID;
		this.variantKey = variantKey;

		this.primaryPortal = new Portal();
		this.secondaryPortal = new Portal();
	}

	public PortalLink(UUID linkID, Portal primaryPortal, Portal secondaryPortal,
					  ResourceLocation variantKey)
	{
		this.linkID = linkID;

		this.primaryPortal = primaryPortal;
		this.secondaryPortal = secondaryPortal;

		this.variantKey = variantKey;
	}

	public void createPrimaryPortal(Level level, Vec3 pos, ResourceKey<Level> dimension, Direction direction, Direction facing)
	{
		float xRot = 0;
		float yRot = direction.toYRot();
		if(direction.equals(Direction.UP))
		{
			xRot = -90;
			yRot = facing.toYRot()+(facing.getAxis().equals(Direction.Axis.X) ? 0 : 180);
		}
		if(direction.equals(Direction.DOWN))
		{
			xRot = 90;
			yRot = facing.toYRot();
		}

		this.primaryPortal.setPosition(pos);
		this.primaryPortal.setYRotation(yRot);
		this.primaryPortal.setXRotation(xRot);
		this.primaryPortal.setDimension(dimension);
		this.primaryPortal.setMoonshot(false);

		ServerLevel portalLevel = level.getServer().getLevel(dimension);

		if(portalLevel != null)
		{
			int x = (int) pos.x;
			int z = (int) pos.z;
			PacketDistributor.sendToPlayersTrackingChunk(portalLevel, new ChunkPos(x, z),
					new ClientboundPortalSoundsPacket.OpenPortal(linkID, true));

		}

		PortalLinkData.get(level).setDirty(linkID, true);
	}

	public void createSecondaryPortal(Level level, Vec3 pos, ResourceKey<Level> dimension,
									  Direction direction, Direction facing)
	{
		float xRot = 0;
		float yRot = direction.toYRot();
		if(direction.equals(Direction.UP))
		{
			xRot = -90;
			yRot = facing.toYRot()+(facing.getAxis().equals(Direction.Axis.X) ? 0 : 180);
		}
		if(direction.equals(Direction.DOWN))
		{
			xRot = 90;
			yRot = facing.toYRot();
		}

		this.secondaryPortal.setPosition(pos);
		this.secondaryPortal.setYRotation(yRot);
		this.secondaryPortal.setXRotation(xRot);
		this.secondaryPortal.setDimension(dimension);
		this.secondaryPortal.setMoonshot(false);

		ServerLevel portalLevel = level.getServer().getLevel(dimension);

		if(portalLevel != null)
		{
			int x = (int) pos.x;
			int z = (int) pos.z;
			PacketDistributor.sendToPlayersTrackingChunk(portalLevel, new ChunkPos(x, z),
					new ClientboundPortalSoundsPacket.OpenPortal(linkID, false));
		}

		PortalLinkData.get(level).setDirty(linkID, false);
	}

	public boolean checkForValidity(Level level, Vec3 position, float xRot, float yRot, Direction direction, UUID id, boolean isPrimary)
	{
		AABB portalBox = PortalUtilities.getPortalBoundingBox(position, xRot, yRot).inflate(0.025);
		AABB placementBox = PortalUtilities.getPortalPlacementBox(position, xRot, yRot).inflate(0.025);

		Portal self;
		if(level.isClientSide())
		{
			ClientPortalLink link =  PortalUtilities.getPortalLinks().get(id);
			if(isPrimary)
				self = link.getPrimaryPortal();
			else self = link.getSecondaryPortal();
		}
		else
		{
			PortalLink link = PortalUtilities.getPortalLinks(level).get(id);
			if(isPrimary)
				self = link.getPrimaryPortal();
			else self = link.getSecondaryPortal();
		}
		Pair<UUID, Boolean> closestPortalPair;
		if(self.getPosition() == null)
			closestPortalPair = PortalUtilities.getClosestPortal(level, position, isPrimary);

		else closestPortalPair = PortalUtilities.getClosestPortal(level, self);
		Portal closestPortal;
		if(level.isClientSide() && closestPortalPair.getFirst() != null)
		{
			ClientPortalLink link = PortalUtilities.getPortalLinks().get(closestPortalPair.getFirst());
			if(closestPortalPair.getSecond())
				closestPortal = link.getPrimaryPortal();
			else closestPortal = link.getSecondaryPortal();
		}
		else if(closestPortalPair.getFirst() != null)
		{
			PortalLink link = PortalUtilities.getPortalLinks(level).get(closestPortalPair.getFirst());
			if(closestPortalPair.getSecond())
				closestPortal = link.getPrimaryPortal();
			else closestPortal = link.getSecondaryPortal();
		}
		else
			closestPortal = null;

		if(self.equals(closestPortal))
			closestPortal = null;

		VoxelShape placementShape = Shapes.create(placementBox);

		AABB portalBoxCopy = PortalUtilities.getPortalPlacementBox(position, xRot, yRot).inflate(0.025).deflate(0.025)
											.move(Vec3.directionFromRotation(xRot+(direction.getAxis().isVertical() ? 180 : 0), yRot+180)
													  .multiply(0.15, 0.15, 0.15));

		AtomicBoolean hasAir = new AtomicBoolean();
		BlockPos.betweenClosedStream(portalBoxCopy).forEach(pos ->
			{
				BlockState state = level.getBlockState(pos);
				if(state.isAir())
					hasAir.set(true);
			});

		if(hasAir.get())
			return false;

		AtomicReference<VoxelShape> placementReference = new AtomicReference<>(placementShape);
		Portal finalClosestPortal = closestPortal;
		BlockPos.betweenClosedStream(portalBox).forEach(pos ->
			{
				BlockState state = level.getBlockState(pos);
				if(finalClosestPortal != null)
				{
					VoxelShape shape = Shapes.create(PortalUtilities.getPortalPlacementBox(finalClosestPortal.getPosition(),
							finalClosestPortal.getXRotation(), finalClosestPortal.getYRotation()).inflate(0.05));

					if(!placementReference.get().isEmpty())
						placementReference.set(Shapes.join(placementReference.get(), shape, BooleanOp.ONLY_FIRST));
				}
				if(!state.isAir())
				{
					VoxelShape shape = state.getCollisionShape(level, pos)
											   .move(pos.getX(), pos.getY(), pos.getZ());

					if(state.is(ApertureInnovations.IMPORTALABLE))
						shape = Shapes.create(shape.bounds().inflate(0.025));

					if(!placementReference.get().isEmpty())
						placementReference.set(Shapes.join(placementReference.get(), shape, BooleanOp.ONLY_FIRST));
				}
			});

		placementShape = placementReference.get();
		List<AABB> shapeList = placementShape.toAabbs();

		if(!shapeList.isEmpty())
		{
			AABB firstPart = shapeList.getFirst();

			boolean wall = direction.getAxis().isHorizontal();

			boolean equal = false;
			if(!wall)
			{
				equal = firstPart.maxX == placementBox.maxX && firstPart.minX == placementBox.minX
						&& firstPart.maxZ == placementBox.maxZ && firstPart.minZ == placementBox.minZ;
			}
			if(wall)
			{
				if(direction.getAxis().equals(Direction.Axis.Z))
				{
					equal = firstPart.maxX == placementBox.maxX && firstPart.minX == placementBox.minX
							&& firstPart.maxY == placementBox.maxY && firstPart.minY == placementBox.minY;

				}
				if(direction.getAxis().equals(Direction.Axis.X))
				{
					equal = firstPart.maxY == placementBox.maxY && firstPart.minY == placementBox.minY
							&& firstPart.maxZ == placementBox.maxZ && firstPart.minZ == placementBox.minZ;
				}
			}

			return equal && shapeList.size() == 1;
		}

		return false;
	}

	public void updateColors(Level level, int primaryPortalColor, int secondaryPortalColor)
	{
		if(this.primaryPortal.getColor() != primaryPortalColor)
		{
			this.primaryPortal.setColor(primaryPortalColor);
			PortalLinkData.get(level).setDirty(linkID, true);
		}

		if(this.secondaryPortal.getColor() != secondaryPortalColor)
		{
			this.secondaryPortal.setColor(secondaryPortalColor);
			PortalLinkData.get(level).setDirty(linkID, false);
		}
	}

	public void updateVariant(Level level, ResourceLocation variantKey)
	{
		if(this.variantKey != variantKey)
		{
			this.variantKey = variantKey;
			PortalLinkData.get(level).setDirty(linkID, true);
			PortalLinkData.get(level).setDirty(linkID, false);
		}
	}

	public void reset(Level level)
	{
		if(primaryPortal.isOpen())
			resetPrimary(level);

		if(secondaryPortal.isOpen())
			resetSecondary(level);
	}

	public void resetPrimary(Level level)
	{
		if(primaryPortal.isInWorld())
		{
			ServerLevel portalLevel = level.getServer().getLevel(primaryPortal.getDimension());
			if(portalLevel != null)
			{
				int x = (int) primaryPortal.getPosition().x;
				int z = (int) primaryPortal.getPosition().z;
				PacketDistributor.sendToPlayersTrackingChunk(portalLevel, new ChunkPos(x, z),
						new ClientboundPortalSoundsPacket.FizzlePortal(linkID, true));
			}
		}
		int color = primaryPortal.getColor();

		this.primaryPortal = new Portal();
		this.primaryPortal.setColor(color);

		PortalLinkData.get(level).setDirty(linkID, true);
	}

	public void resetSecondary(Level level)
	{
		if(secondaryPortal.isInWorld())
		{
			ServerLevel portalLevel = level.getServer().getLevel(secondaryPortal.getDimension());
			if(portalLevel != null)
			{
				int x = (int) secondaryPortal.getPosition().x;
				int z = (int) secondaryPortal.getPosition().z;
				PacketDistributor.sendToPlayersTrackingChunk(portalLevel, new ChunkPos(x, z),
						new ClientboundPortalSoundsPacket.FizzlePortal(linkID, false));

			}
		}
		int color = secondaryPortal.getColor();

		this.secondaryPortal = new Portal();
		this.secondaryPortal.setColor(color);

		PortalLinkData.get(level).setDirty(linkID, false);
	}

	public void setMoonshot(boolean isPrimary, boolean moonshot, Level level)
	{
		if(isPrimary)
		{
			primaryPortal.setPosition(null);
			primaryPortal.setMoonshot(moonshot);
		}
		else
		{
			secondaryPortal.setPosition(null);
			secondaryPortal.setMoonshot(moonshot);
		}

		PortalLinkData.get(level).setDirty(linkID, isPrimary);
	}

	public boolean isOpen()
	{
		return primaryPortal.isOpen() && secondaryPortal.isOpen();
	}

	public boolean isInWorld()
	{
		return primaryPortal.isInWorld() && secondaryPortal.isInWorld();
	}

	public boolean isInterdimensionalLink()
	{
		if(isOpen())
		{
			return !primaryPortal.getDimension().equals(secondaryPortal.getDimension());
		}
		return false;
	}

	public boolean teleportLogic(Entity entity, ApertureCapability aperture,
									 PortalLink link, Level level, boolean isPrimary)
	{
		UUID linkID = link.linkID;
		if(level.isClientSide())
			return false;
		if(aperture.ignorePortalsTime != 0)
			return false;

		Vec3 currentPos = entity.position().add(0, entity.getBbHeight() / 2f, 0);

		Vec3 speed = entity.getDeltaMovement();
		Vec3 nextPos = currentPos.add(speed.multiply(2f, 2f, 2f));

		AABB movementBox = entity.getBoundingBox().expandTowards(speed);
		if(link == null || !link.isOpen())
			return false;

		Portal portal = isPrimary ? link.getPrimaryPortal() : link.getSecondaryPortal();
		Portal otherPortal = isPrimary ? link.getSecondaryPortal() : link.getPrimaryPortal();

		double distance = portal.getPosition().distanceTo(entity.position());
		if(distance < 6 && otherPortal.isMoonshot())
		{
			Direction direction = PortalUtilities.getPortalDirection(level, linkID, isPrimary);

			Vec3 portalPos = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
					portal.getYRotation()).getCenter();
			portalPos = portalPos.add(direction.getOpposite().getStepX() * entity.getBbWidth() / 2f,
					direction.getOpposite().getStepY() * entity.getBbHeight() / 1.25f,
					direction.getOpposite().getStepZ() * entity.getBbWidth() / 2f);

			Vec3 pushVector = portalPos.subtract(entity.position()).multiply(0.08, 0.08, 0.08);
			entity.push(pushVector);
			if(entity instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player,
					new ClientboundTeleportMomentumPacket(player.getDeltaMovement().toVector3f()));
		}

		AABB teleportBox = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
				portal.getYRotation());

		if(movementBox.intersects(teleportBox))
		{
			Direction direction = PortalUtilities.getPortalDirection(level, linkID, isPrimary);
			Direction otherDirection = PortalUtilities.getPortalDirection(level, linkID, !isPrimary);
			Vector3f normal = direction.step();

			Vec3 portalPos = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
					portal.getYRotation()).getCenter();
			portalPos = portalPos.add(direction.getOpposite().getStepX() * entity.getBbWidth() / 2f,
					direction.getOpposite().getStepY() * entity.getBbHeight() / 1.25f,
					direction.getOpposite().getStepZ() * entity.getBbWidth() / 2f);

			Vec3 offsetFromPortal = currentPos.subtract(portalPos);
			Vec3 offsetPortalPlace = currentPos.subtract(portal.getPosition());
			Vec3 nextOffsetFromPortal = nextPos.subtract(portalPos);

			double relativePos = offsetFromPortal.dot(new Vec3(normal));
			double nextRelativePos = nextOffsetFromPortal.dot(new Vec3(normal));

			boolean slow = portalPos.closerThan(currentPos, 0.4f) && relativePos > 0;
			boolean fast = relativePos > 0 && nextRelativePos <= 0;

			if(slow || fast)
			{
				Quaternionf portalQ = new Quaternionf().rotationYXZ((float) (Math.PI - Math.toRadians(
								portal.getYRotation() + (direction.getAxis().isHorizontal() ? 180 : 0))),
						(float) Math.toRadians(-portal.getXRotation() - 90), 0);

				Quaternionf otherPortalQ = new Quaternionf().rotationYXZ((float) (Math.PI - Math.toRadians(
								otherPortal.getYRotation() + (direction.getAxis().isHorizontal() ? 180 : 0))),
						(float) Math.toRadians(-otherPortal.getXRotation() - 90), 0);

				if(direction.getAxis().isHorizontal())
					aperture.setIgnorePortalsTime(5);
				if(link.isInWorld() && link.isInterdimensionalLink())
					aperture.setIgnorePortalsTime(20);

				Vector3f newSpeed = portalQ.invert(new Quaternionf()).transform(speed.toVector3f());
				otherPortalQ.rotateZ((float) Math.toRadians(180), new Quaternionf()).transform(newSpeed);

				if(otherPortal.getXRotation() == -90 && newSpeed.length() < 0.5) newSpeed.add(0, 0.05f, 0);
				if(portal.getXRotation() == 0 && otherPortal.getXRotation() == -90) newSpeed.add(0f, 0.5f, 0f);

				Vector3f rotatedOffset = portalQ.invert(new Quaternionf()).transform(offsetPortalPlace.toVector3f());
				otherPortalQ.rotateZ((float) Math.toRadians(180), new Quaternionf()).transform(rotatedOffset);

				Vec3 targetPos;
				if(otherPortal.isMoonshot()) targetPos = portalPos.add(0, 1000, 0);
				else targetPos = otherPortal.getPosition().subtract(0, entity.getBbHeight() / 2, 0);

				targetPos = targetPos.add(otherDirection.getStepX() * 0.05, otherDirection.getStepY() * 0.05,
						otherDirection.getStepZ() * 0.05);

				entity.setDeltaMovement(new Vec3(newSpeed));
				entity.hasImpulse = true;
				entity.resetFallDistance();

				Quaternionf rot = new Quaternionf().rotationYXZ((180 - entity.getYRot()) * Mth.DEG_TO_RAD,
														   -entity.getXRot() * Mth.DEG_TO_RAD, 0).premul(portalQ.invert(new Quaternionf()))
												   .premul(otherPortalQ.rotateZ((float) Math.toRadians(180),
														   new Quaternionf())).conjugate();

				ServerLevel targetLevel;
				if(otherPortal.isMoonshot()) targetLevel = (ServerLevel) level;
				else targetLevel = level.getServer().getLevel(otherPortal.getDimension());
				if(targetLevel == null)
					return false;

				float yaw = (float) Math.atan2(-(rot.x * rot.z + rot.y * rot.w) * 2,
						2 * (rot.y * rot.y + rot.z * rot.z) - 1);
				entity.setPos(targetPos);

				Vec2 portalRot = PortalUtilities.getPortalRotation(level, linkID, isPrimary);
				Vec2 otherPortalRot = PortalUtilities.getPortalRotation(level, linkID, !isPrimary);

				if(portalRot.y - otherPortalRot.y == 180 && portalRot.x == -90) yaw += (float) Math.toRadians(180);

				PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level,
						new ChunkPos(BlockPos.containing(portal.getPosition())),
						new ClientboundPortalSoundsPacket.EnterPortal(link.linkID, isPrimary));

				entity.teleportTo(targetLevel, targetPos.x, targetPos.y, targetPos.z, Set.of(),
						(float) Math.toDegrees(yaw) + (direction.getAxis().isVertical() ? 180 : 0), entity.getXRot());
				entity.setOldPosAndRot();
//				((ServerLevel) level).sendParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y, targetPos.z, 100,
//						0.5, 0.5, 0.5, 1d);

				PacketDistributor.sendToPlayersTrackingChunk(targetLevel,
						new ChunkPos(BlockPos.containing(otherPortal.getPosition())),
						new ClientboundPortalSoundsPacket.EnterPortal(link.linkID, !isPrimary));
				if(entity instanceof ServerPlayer player)
				{
					player.awardStat(StatisticsInit.TIMES_USED_PORTALS.get(), 1);
					PacketDistributor.sendToPlayer(player, new ClientboundTeleportMomentumPacket(newSpeed));

					aperture.setPortal(Pair.of(linkID, isPrimary));
					aperture.updateDistance();
					aperture.setFrictionlessTime(100 * 20);
					AdvancementInit.PORTAL_TRAVEL.get().trigger(player, portal.getDimension().location(),
							targetLevel.dimension().location(), portal.getPosition().distanceToSqr(targetPos),
							aperture.verticalDistance, aperture.horizontalDistance, otherPortal.isMoonshot());
				}
				else
				{
					PacketDistributor.sendToPlayersTrackingEntity(entity,
							new ClientboundEntityPortalLerpPacket(entity.getId(), targetPos.toVector3f(),
									(float) Math.toDegrees(yaw) + (direction.getAxis().isVertical() ? 180 : 0),
									entity.getXRot()));
				}

				return true;
			}
		}
		return false;
	}

	public static PortalLink load(CompoundTag tag)
	{
		UUID linkID = tag.getUUID("link");
		Portal primaryPortal = new Portal();
		Portal secondaryPortal = new Portal();

		primaryPortal.load(tag.getCompound("primaryPortal"));
		secondaryPortal.load(tag.getCompound("secondaryPortal"));

		ResourceLocation variantKey = ResourceLocation.parse(tag.getString("variantKey"));

		PortalLink link = new PortalLink(linkID, variantKey);
		link.setPrimaryPortal(primaryPortal);
		link.setSecondaryPortal(secondaryPortal);

		return link;
	}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();

		tag.putUUID("link", linkID);

		tag.put("primaryPortal", this.primaryPortal.save());
		tag.put("secondaryPortal", this.secondaryPortal.save());

		if(variantKey == null)
			variantKey = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "chell");
		tag.putString("variantKey", variantKey.toString());

		return tag;
	}

	public PortalGunVariant getGunVariant(Level level)
	{
		Registry<PortalGunVariant> registry =  level.getServer().registryAccess()
													.registryOrThrow(PortalGunVariant.REGISTRY_KEY);
		return registry.get(variantKey);
	}

	public Portal getSecondaryPortal()
	{
		return secondaryPortal;
	}

	public void setSecondaryPortal(Portal secondaryPortal)
	{
		this.secondaryPortal = secondaryPortal;
	}

	public Portal getPrimaryPortal()
	{
		return primaryPortal;
	}

	public void setPrimaryPortal(Portal primaryPortal)
	{
		this.primaryPortal = primaryPortal;
	}
}
