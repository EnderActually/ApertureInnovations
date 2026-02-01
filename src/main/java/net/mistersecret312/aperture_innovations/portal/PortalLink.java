package net.mistersecret312.aperture_innovations.portal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

	public boolean checkForValidity(Level level, Vec3 position, float xRot, float yRot, Direction direction, boolean isPrimary)
	{
		AABB portalBox = PortalUtilities.getPortalBoundingBox(position, xRot, yRot).inflate(0.025);
		AABB placementBox = PortalUtilities.getPortalPlacementBox(position, xRot, yRot).inflate(0.025);

		Pair<UUID, Boolean> closestPortalPair = PortalUtilities.getClosestPortal(level, position);
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

		VoxelShape placementShape = Shapes.create(placementBox);

		AtomicReference<VoxelShape> placementReference = new AtomicReference<>(placementShape);
		BlockPos.betweenClosedStream(portalBox).forEach(pos ->
			{
				BlockState state = level.getBlockState(pos);
				if(closestPortal != null)
				{
					VoxelShape shape = Shapes.create(PortalUtilities.getPortalPlacementBox(closestPortal.getPosition(),
							closestPortal.getXRotation(), closestPortal.getYRotation()));

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

	public boolean isInterdimensionalLink()
	{
		if(isOpen())
		{
			return !primaryPortal.getDimension().equals(secondaryPortal.getDimension());
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
