package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.init.SoundInit;

import java.util.UUID;

public class PortalLink
{
	public UUID linkID;

	public BlockPos posPrimary;
	public BlockPos posSecondary;

	public boolean wallPrimary;
	public boolean wallSecondary;

	public boolean ceilingPrimary;
	public boolean ceilingSecondary;

	public ResourceKey<Level> dimensionPrimary;
	public ResourceKey<Level> dimensionSecondary;

	public Direction directionPrimary;
	public Direction directionSecondary;

	public boolean moonshotPrimary = false;
	public boolean moonshotSecondary = false;

	public int openingPrimary = 0;
	public int openingSecondary = 0;

	public PortalLink(UUID linkID)
	{
		this.linkID = linkID;
	}

	public PortalLink(UUID linkID, BlockPos posPrimary, BlockPos posSecondary,
					  boolean wallPrimary, boolean wallSecondary,
					  boolean ceilingPrimary, boolean ceilingSecondary,
					  ResourceKey<Level> dimensionPrimary, ResourceKey<Level> dimensionSecondary,
					  Direction directionPrimary, Direction directionSecondary)
	{
		this.linkID = linkID;

		this.posPrimary = posPrimary;
		this.posSecondary = posSecondary;

		this.wallPrimary = wallPrimary;
		this.wallSecondary = wallSecondary;

		this.ceilingPrimary = ceilingPrimary;
		this.ceilingSecondary = ceilingSecondary;

		this.dimensionPrimary = dimensionPrimary;
		this.dimensionSecondary = dimensionSecondary;

		this.directionPrimary = directionPrimary;
		this.directionSecondary = directionSecondary;
	}

	public void createPrimaryPortal(Level level, BlockPos pos, ResourceKey<Level> dimension, Direction direction, Direction facing)
	{
		this.posPrimary = pos;
		this.dimensionPrimary = dimension;
		this.directionPrimary = facing.equals(Direction.UP) ? direction : facing;
		this.wallPrimary = facing.equals(Direction.UP);
		this.ceilingPrimary = direction.equals(Direction.DOWN);
		this.moonshotPrimary = false;

		level.playSound(null, pos, SoundInit.PORTAL_OPEN_PRIMARY.get(), SoundSource.BLOCKS, 0.7f, 1f);

		PortalLinkData.get(level).setDirty();
	}

	public void createSecondaryPortal(Level level, BlockPos pos, ResourceKey<Level> dimension, Direction direction, Direction facing)
	{
		this.posSecondary = pos;
		this.dimensionSecondary = dimension;
		this.directionSecondary = facing.equals(Direction.UP) ? direction : facing;
		this.wallSecondary = facing.equals(Direction.UP);
		this.ceilingSecondary = direction.equals(Direction.DOWN);
		this.moonshotSecondary = false;

		level.playSound(null, pos, SoundInit.PORTAL_OPEN_SECONDARY.get(), SoundSource.BLOCKS, 0.7f, 1f);

		if(posSecondary != null)
		{

			ChunkPos portalPos = new ChunkPos(posSecondary);
			for(int i = 0; i < 3; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					ForgeChunkManager.forceChunk((ServerLevel) level, ApertureInnovations.MODID,
							linkID, portalPos.x+i, portalPos.z+j, true, true);
				}
			}
		}

		PortalLinkData.get(level).setDirty();
	}

	public void reset(Level level)
	{
		if(posPrimary != null || moonshotPrimary)
			resetPrimary(level);

		if(posSecondary != null || moonshotSecondary)
			resetSecondary(level);
	}

	public void resetPrimary(Level level)
	{
		if(posPrimary != null)
			level.playSound(null, posPrimary, SoundInit.PORTAL_FIZZLE.get(), SoundSource.BLOCKS, 0.5f, 1f);

		this.posPrimary = null;
		this.wallPrimary = false;
		this.ceilingPrimary = false;
		this.dimensionPrimary = null;
		this.directionPrimary = null;
		this.openingPrimary = 0;
		this.moonshotPrimary = false;

		PortalLinkData.get(level).setDirty();
	}

	public void resetSecondary(Level level)
	{
		if(posSecondary != null)
			level.playSound(null, posSecondary, SoundInit.PORTAL_FIZZLE.get(), SoundSource.BLOCKS, 0.5f, 1f);

		this.posSecondary = null;
		this.wallSecondary = false;
		this.ceilingSecondary = false;
		this.dimensionSecondary = null;
		this.directionSecondary = null;
		this.openingSecondary = 0;
		this.moonshotSecondary = false;

		PortalLinkData.get(level).setDirty();
	}

	public void setMoonshot(boolean isPrimary, boolean moonshot, Level level)
	{
		if(isPrimary)
			moonshotPrimary = moonshot;
		else moonshotSecondary = moonshot;

		PortalLinkData.get(level).setDirty();
	}

	public boolean isOpen()
	{
		return (posPrimary != null || moonshotPrimary) && (posSecondary != null || moonshotSecondary);
	}

	public boolean isInterdimensionalLink()
	{
		if(posPrimary == null || posSecondary == null)
			return false;

		return dimensionPrimary != dimensionSecondary;
	}

	public boolean isWallPrimary()
	{
		return wallPrimary;
	}

	public boolean isWallSecondary()
	{
		return wallSecondary;
	}

	public static PortalLink load(CompoundTag tag)
	{
		UUID linkID = tag.getUUID("link");

		BlockPos posPrimary = null;
		boolean wallPrimary = false;
		boolean ceilingPrimary = false;
		ResourceKey<Level> dimensionPrimary = null;
		Direction directionPrimary = null;
		int openingPrimary = 0;

		if(tag.contains("posPrimary"))
		{
			posPrimary = NbtUtils.readBlockPos(tag.getCompound("posPrimary"));
			wallPrimary = tag.getBoolean("wallPrimary");
			ceilingPrimary = tag.getBoolean("ceilingPrimary");
			dimensionPrimary = stringToDimension(tag.getString("dimensionPrimary"));
			directionPrimary = Direction.from3DDataValue(tag.getInt("directionPrimary"));
			openingPrimary = tag.getInt("openingPrimary");
		}

		BlockPos posSecondary = null;
		boolean wallSecondary = false;
		boolean ceilingSecondary = false;
		ResourceKey<Level> dimensionSecondary = null;
		Direction directionSecondary = null;
		int openingSecondary = 0;

		if(tag.contains("posSecondary"))
		{
			posSecondary = NbtUtils.readBlockPos(tag.getCompound("posSecondary"));
			wallSecondary = tag.getBoolean("wallSecondary");
			ceilingSecondary = tag.getBoolean("ceilingSecondary");
			dimensionSecondary = stringToDimension(tag.getString("dimensionSecondary"));
			directionSecondary = Direction.from3DDataValue(tag.getInt("directionSecondary"));
			openingSecondary = tag.getInt("openingSecondary");
		}

		PortalLink link = new PortalLink(linkID, posPrimary, posSecondary,
				wallPrimary, wallSecondary,
				ceilingPrimary, ceilingSecondary,
				dimensionPrimary, dimensionSecondary,
				directionPrimary, directionSecondary);

		link.openingPrimary = openingPrimary;
		link.openingSecondary = openingSecondary;

		return link;
	}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();

		tag.putUUID("link", linkID);

		if(posPrimary != null)
		{
			tag.put("posPrimary", NbtUtils.writeBlockPos(posPrimary));
			tag.putBoolean("wallPrimary", wallPrimary);
			tag.putBoolean("ceilingPrimary", ceilingPrimary);
			tag.putString("dimensionPrimary", dimensionPrimary.location().toString());
			tag.putInt("directionPrimary", directionPrimary.get3DDataValue());
			tag.putInt("openingPrimary", openingPrimary);
		}

		if(posSecondary != null)
		{
			tag.put("posSecondary", NbtUtils.writeBlockPos(posSecondary));
			tag.putBoolean("wallSecondary", wallSecondary);
			tag.putBoolean("ceilingSecondary", ceilingSecondary);
			tag.putString("dimensionSecondary", dimensionSecondary.location().toString());
			tag.putInt("directionSecondary", directionSecondary.get3DDataValue());
			tag.putInt("openingSecondary", openingSecondary);
		}

		return tag;
	}

	public static ResourceKey<Level> stringToDimension(String dimensionString)
	{
		String[] split = dimensionString.split(":");

		if(split.length > 1)
			return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));

		return null;
	}
}
