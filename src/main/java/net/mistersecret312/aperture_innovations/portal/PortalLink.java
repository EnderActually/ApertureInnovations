package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalAmbientSoundPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;

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

	public ResourceLocation variantKey = null;

	public int primaryPortalColor = -1;
	public int secondaryPortalColor = -1;

	public PortalLink(UUID linkID, ResourceLocation variantKey)
	{
		this.linkID = linkID;

		if(variantKey == null)
			this.variantKey = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "chell");
		else this.variantKey = variantKey;
	}

	public PortalLink(UUID linkID, BlockPos posPrimary, BlockPos posSecondary,
					  boolean wallPrimary, boolean wallSecondary,
					  boolean ceilingPrimary, boolean ceilingSecondary,
					  ResourceKey<Level> dimensionPrimary, ResourceKey<Level> dimensionSecondary,
					  Direction directionPrimary, Direction directionSecondary,
					  ResourceLocation variantKey, int primaryPortalColor, int secondaryPortalColor)
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

		this.primaryPortalColor = primaryPortalColor;
		this.secondaryPortalColor = secondaryPortalColor;

		this.variantKey = variantKey;
	}

	public void createPrimaryPortal(Level level, BlockPos pos, ResourceKey<Level> dimension, Direction direction, Direction facing)
	{
		this.posPrimary = pos;
		this.dimensionPrimary = dimension;
		this.directionPrimary = facing.equals(Direction.UP) ? direction : facing;
		this.wallPrimary = facing.equals(Direction.UP);
		this.ceilingPrimary = direction.equals(Direction.DOWN);
		this.moonshotPrimary = false;
		this.openingPrimary = 0;

		Level portalLevel = level.getServer().getLevel(dimensionPrimary);
		if(portalLevel != null)
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
							() -> portalLevel.getChunkAt(pos)),
					new ClientboundPortalSoundsPacket.OpenPortal(linkID, true));
		}
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
		this.openingSecondary = 0;

		Level portalLevel = level.getServer().getLevel(dimensionSecondary);
		if(portalLevel != null)
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
							() -> portalLevel.getChunkAt(pos)),
					new ClientboundPortalSoundsPacket.OpenPortal(linkID, false));
		}

		PortalLinkData.get(level).setDirty();
	}

	public void updateColors(Level level, int primaryPortalColor, int secondaryPortalColor)
	{
		this.primaryPortalColor = primaryPortalColor;
		this.secondaryPortalColor = secondaryPortalColor;

		PortalLinkData.get(level).setDirty();
	}

	public void reset(Level level)
	{
		if(posPrimary != null || moonshotPrimary)
			resetPrimary(level);

		if(posSecondary != null || moonshotSecondary)
			resetSecondary(level);

		PortalLinkData.get(level).setDirty();
	}

	public void resetPrimary(Level level)
	{
		if(posPrimary != null)
		{
			Level portalLevel = level.getServer().getLevel(dimensionPrimary);
			if(portalLevel != null)
			{
				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
								() -> portalLevel.getChunkAt(posPrimary)),
						new ClientboundPortalSoundsPacket.FizzlePortal(linkID, true));
			}
		}

		this.posPrimary = null;
		this.wallPrimary = false;
		this.ceilingPrimary = false;
		this.dimensionPrimary = null;
		this.directionPrimary = null;
		this.openingPrimary = 0;
		this.moonshotPrimary = false;
	}

	public void resetSecondary(Level level)
	{
		if(posSecondary != null)
		{
			Level portalLevel = level.getServer().getLevel(dimensionSecondary);
			if(portalLevel != null)
			{
				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
								() -> portalLevel.getChunkAt(posSecondary)),
						new ClientboundPortalSoundsPacket.FizzlePortal(linkID, false));
			}
		}

		this.posSecondary = null;
		this.wallSecondary = false;
		this.ceilingSecondary = false;
		this.dimensionSecondary = null;
		this.directionSecondary = null;
		this.openingSecondary = 0;
		this.moonshotSecondary = false;
	}

	public void setMoonshot(boolean isPrimary, boolean moonshot, Level level)
	{
		if(isPrimary)
		{
			posPrimary = null;
			moonshotPrimary = moonshot;
		}
		else
		{
			posSecondary = null;
			moonshotSecondary = moonshot;
		};

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

		ResourceLocation variantKey = ResourceLocation.parse(tag.getString("variantKey"));
		int primaryPortalColor = tag.getInt("primaryPortalColor");
		int secondaryPortalColor = tag.getInt("secondaryPortalColor");

		PortalLink link = new PortalLink(linkID, posPrimary, posSecondary,
				wallPrimary, wallSecondary,
				ceilingPrimary, ceilingSecondary,
				dimensionPrimary, dimensionSecondary,
				directionPrimary, directionSecondary,
				variantKey, primaryPortalColor, secondaryPortalColor);

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

		if(variantKey == null)
			variantKey = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "chell");
		tag.putString("variantKey", variantKey.toString());

		tag.putInt("primaryPortalColor", primaryPortalColor);
		tag.putInt("secondaryPortalColor", secondaryPortalColor);

		return tag;
	}

	public PortalGunVariant getGunVariant(Level level)
	{
		Registry<PortalGunVariant> registry =  level.getServer().registryAccess()
													.registryOrThrow(PortalGunVariant.REGISTRY_KEY);
		return registry.get(variantKey);
	}

	public static ResourceKey<Level> stringToDimension(String dimensionString)
	{
		String[] split = dimensionString.split(":");

		if(split.length > 1)
			return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));

		return null;
	}
}
