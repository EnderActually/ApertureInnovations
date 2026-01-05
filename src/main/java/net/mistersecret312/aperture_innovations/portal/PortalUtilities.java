package net.mistersecret312.aperture_innovations.portal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.client.ColorUtil;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariant;
import net.mistersecret312.aperture_innovations.sounds.PortalAmbientSound;
import net.mistersecret312.aperture_innovations.sounds.PortalSoundWrapper;

import java.awt.*;
import java.util.*;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.LINKS;

public class PortalUtilities
{
	public static HashMap<UUID, Pair<PortalSoundWrapper.PortalAmbient, PortalSoundWrapper.PortalAmbient>> AMBIENTS = new HashMap<>();
	public static HashMap<UUID, Pair<Float, Float>> OPENING_ANIMATIONS = new HashMap<>();

	public static HashMap<UUID, PortalLink> getPortalLinks(Level level)
	{
		if(level.isClientSide())
			return new HashMap<>();

		PortalLinkData data = PortalLinkData.get(level);
		return data.portalLinks;
	}

	public static HashMap<UUID, ClientPortalLink> getPortalLinks()
	{
		return LINKS;
	}

	public static Vec3 getPortalPos(Level level, UUID uuid, boolean isPrimary)
	{
		if(level.isClientSide())
		{
			ClientPortalLink link = getPortalLinks().get(uuid);
			BlockPos portalPos;
			Direction direction;
			if(isPrimary)
			{
				portalPos = link.posPrimary();
				direction = link.directionPrimary();
			}
			else
			{
				portalPos = link.posSecondary();
				direction = link.directionSecondary();
			}

			if(portalPos == null)
				return null;

			return portalPos.getCenter().add(Vec3.atLowerCornerOf(direction.getNormal())
														  .multiply(0.5f, 0.5f, 0.5f)
														  .add(0f, 0.5f, 0f));
		}
		else
		{
			PortalLink link = getPortalLinks(level).get(uuid);
			BlockPos portalPos;
			Direction direction;
			if(isPrimary)
			{
				portalPos = link.posPrimary;
				direction = link.directionPrimary;
			}
			else
			{
				portalPos = link.posSecondary;
				direction = link.directionSecondary;
			}

			if(portalPos == null)
				return null;

			return portalPos.getCenter().add(Vec3.atLowerCornerOf(direction.getNormal())
										   .multiply(0.5f, 0.5f, 0.5f)
										   .add(0f, 0.5f, 0f));
		}
	}

	public static Direction getPortalDirection(Level level, UUID uuid, boolean isPrimary)
	{
		if(level.isClientSide())
		{
			ClientPortalLink link = getPortalLinks().get(uuid);
			return isPrimary ? link.directionPrimary() : link.directionSecondary();
		}
		else
		{
			PortalLink link = getPortalLinks(level).get(uuid);
			return isPrimary ? link.directionPrimary : link.directionSecondary;
		}
	}

	public static boolean isPortalOnWall(Level level, UUID uuid, boolean isPrimary)
	{
		if(level.isClientSide())
		{
			ClientPortalLink link = getPortalLinks().get(uuid);
			return isPrimary ? link.wallPrimary() : link.wallSecondary();
		}
		else
		{
			PortalLink link = getPortalLinks(level).get(uuid);
			return isPrimary ? link.wallPrimary : link.wallSecondary;
		}
	}

	public static boolean isPortalOpen(Level level, UUID uuid)
	{
		if(level.isClientSide())
		{
			ClientPortalLink link = getPortalLinks().get(uuid);
			return link.isOpen();
		}
		else
		{
			PortalLink link = getPortalLinks(level).get(uuid);
			return link.isOpen();
		}
	}

	public static boolean isPortalOnCeiling(Level level, UUID uuid, boolean isPrimary)
	{
		if(level.isClientSide())
		{
			ClientPortalLink link = getPortalLinks().get(uuid);
			return isPrimary ? link.ceilingPrimary() : link.ceilingSecondary();
		}
		else
		{
			PortalLink link = getPortalLinks(level).get(uuid);
			return isPrimary ? link.ceilingPrimary : link.ceilingSecondary;
		}
	}

	public static ResourceKey<Level> getPortalDimension(Level level, UUID uuid, boolean isPrimary)
	{
		if(level.isClientSide())
		{
			ClientPortalLink link = getPortalLinks().get(uuid);
			return isPrimary ? link.dimensionPrimary() : link.dimensionSecondary();
		}
		else
		{
			PortalLink link = getPortalLinks(level).get(uuid);
			return isPrimary ? link.dimensionPrimary : link.dimensionSecondary;
		}
	}
	
	public static AABB getPortalBoundingBox(Vec3 portalPos, Direction portalDirection, boolean isOnWall, boolean isOnCeiling)
	{
		AABB portal = new AABB(0D,0D,0D,0D,0D,0D);
		if(isOnWall)
		{
			Direction.Axis axis = portalDirection.getAxis();
			if(axis.equals(Direction.Axis.X))
				portal = new AABB(portalPos.x-0.25, portalPos.y-1, portalPos.z-0.25,
						portalPos.x+0.25, portalPos.y+1, portalPos.z+0.5);
			else
				portal = new AABB(portalPos.x-0.25, portalPos.y-1, portalPos.z-0.25,
						portalPos.x+0.25, portalPos.y+1, portalPos.z+0.25);
		}
		else
		{
			portalPos = portalPos.subtract(
					Vec3.atLowerCornerOf(isOnCeiling ? portalDirection.getNormal() : Vec3i.ZERO));
			portalPos = portalPos.subtract(0, isOnCeiling ? 1 : 0, 0);
			Direction.Axis axis = portalDirection.getAxis();
			if(axis.equals(Direction.Axis.X))
				portal = new AABB(portalPos.x - 1, portalPos.y - 0.25, portalPos.z - 0.25, portalPos.x + 1,
						portalPos.y + 0.25, portalPos.z + 0.5);
			else if(axis.equals(Direction.Axis.Z))
				portal = new AABB(portalPos.x - 0.25, portalPos.y - 0.25, portalPos.z - 1, portalPos.x + 0.25,
						portalPos.y + 0.25, portalPos.z + 1);

			if(isOnCeiling)
				portal = portal.expandTowards(0, 1,0);
			else
				portal = portal.expandTowards(0, -1, 0);
		}

		return portal;
	}

	public static AABB getPortalTeleportBox(Vec3 portalPos, Direction portalDirection,
											boolean isOnWall, boolean isOnCeiling)
	{
		AABB portal = new AABB(0D,0D,0D,0D,0D,0D);
		if(isOnWall)
		{
			Direction.Axis axis = portalDirection.getAxis();
			if(axis.equals(Direction.Axis.X))
				portal = new AABB(portalPos.x-0.01, portalPos.y-1, portalPos.z-0.5,
						portalPos.x+0.01, portalPos.y+1, portalPos.z+0.5);
			else
				portal = new AABB(portalPos.x-0.5, portalPos.y-1, portalPos.z-0.01,
						portalPos.x+0.5, portalPos.y+1, portalPos.z+0.01);
		}
		else
		{
				portalPos = portalPos.subtract(
						Vec3.atLowerCornerOf(isOnCeiling ? portalDirection.getNormal() : Vec3i.ZERO));
			portalPos = portalPos.subtract(0, isOnCeiling ? 1 : 0, 0);
			Direction.Axis axis = portalDirection.getAxis();
			if(axis.equals(Direction.Axis.X))
				portal = new AABB(portalPos.x-0.95, portalPos.y-0.01, portalPos.z-0.5,
						portalPos.x+0.95, portalPos.y+0.01, portalPos.z+0.5);
			else if(axis.equals(Direction.Axis.Z))
				portal = new AABB(portalPos.x-0.5, portalPos.y-0.01, portalPos.z-0.95,
						portalPos.x+0.5, portalPos.y+0.01, portalPos.z+0.95);

			if(isOnCeiling)
				portal = portal.expandTowards(0, 1,0);
			else
				portal = portal.expandTowards(0, -1, 0);
		}

		return portal;
	}

	public static AABB getPortalFloorBox(Vec3 portalPos, Direction direction, boolean isOnWall)
	{
		if(isOnWall)
		{
			return new AABB(portalPos.x-0.25, portalPos.y-1, portalPos.z-0.25,
					portalPos.x+0.25, portalPos.y-1, portalPos.z+0.25);
		}
		else return new AABB(0D,0D,0D,0D,0D,0D);
	}

	public static Pair<UUID, Boolean> getClosestPortal(Entity entity)
	{
		Level level = entity.level();

		UUID uuid = null;
		boolean isPrimary = false;
		double closestDistance = Double.MAX_VALUE;

		if(level.isClientSide())
		{

			for(Map.Entry<UUID, ClientPortalLink> entry : getPortalLinks().entrySet())
			{
				for(int i = 0; i < 2; i++)
				{
					Vec3 pos = PortalUtilities.getPortalPos(entity.level(), entry.getKey(), i == 0);
					if(pos == null)
						continue;

					double distance = entity.position().distanceTo(pos);
					if(closestDistance > distance)
					{
						closestDistance = distance;
						uuid = entry.getKey();
						isPrimary = i == 0;
					}
				}
			}
			return Pair.of(uuid, isPrimary);
		}
		else
		{
			for(Map.Entry<UUID, PortalLink> entry : getPortalLinks(level).entrySet())
			{
				for(int i = 0; i < 2; i++)
				{
					Vec3 pos = PortalUtilities.getPortalPos(entity.level(), entry.getKey(), i == 0);
					if(pos == null)
						continue;

					double distance = entity.position().distanceTo(pos);
					if(closestDistance > distance)
					{
						closestDistance = distance;
						uuid = entry.getKey();
						isPrimary = i == 0;
					}
				}
			}
			return Pair.of(uuid, isPrimary);
		}
	}

	public static ColorUtil.RGBA getPortalColor(ClientPortalLink link, boolean isPrimary)
	{
		ClientPortalGunVariant variant = link.getVariant();
		int gunColor = isPrimary ? link.primaryPortalColor() : link.secondaryPortalColor();
		ColorUtil.RGBA variantColor = isPrimary ? variant.getPrimaryPortal().getColor() : variant.secondaryPortal.getColor();
		if(gunColor == -1)
			return variantColor;
		else
		{
			Color rgbaColor = new Color(gunColor, true);
			return new ColorUtil.RGBA(rgbaColor.getRed(), rgbaColor.getGreen(), rgbaColor.getBlue(), rgbaColor.getAlpha());
		}
	}

	public static ColorUtil.RGBA getPortalTexture(ClientPortalLink link, boolean isPrimary)
	{
		ClientPortalGunVariant variant = link.getVariant();
		int gunColor = isPrimary ? link.primaryPortalColor() : link.secondaryPortalColor();
		ColorUtil.RGBA variantColor = isPrimary ? variant.getPrimaryPortal().getColor() : variant.secondaryPortal.getColor();
		if(gunColor == -1)
			return variantColor;
		else
		{
			Color rgbaColor = new Color(gunColor, true);
			return new ColorUtil.RGBA(rgbaColor.getRed(), rgbaColor.getGreen(), rgbaColor.getBlue(), rgbaColor.getAlpha());
		}
	}

	public static PortalSoundWrapper.PortalAmbient getAmbientSound(UUID uuid, boolean isPrimary)
	{
		Pair<PortalSoundWrapper.PortalAmbient, PortalSoundWrapper.PortalAmbient> pair = AMBIENTS.getOrDefault(uuid, new Pair<>(null, null));
		if(isPrimary)
			return pair.getFirst();
		else return pair.getSecond();
	}

	public static void setAmbientSound(PortalSoundWrapper.PortalAmbient ambient, UUID uuid, boolean isPrimary)
	{
		Pair<PortalSoundWrapper.PortalAmbient, PortalSoundWrapper.PortalAmbient> pair = AMBIENTS.getOrDefault(uuid, new Pair<>(null, null));
		if(isPrimary)
			pair = new Pair<>(ambient, pair.getSecond());
		else pair = new Pair<>(pair.getFirst(), ambient);

		AMBIENTS.put(uuid, pair);
	}

	public static float getPortalOpeningAnimationProgress(UUID uuid, boolean isPrimary)
	{
		Pair<Float, Float> pair = OPENING_ANIMATIONS.getOrDefault(uuid, new Pair<>(0F, 0F));
		if(isPrimary)
			return pair.getFirst();
		else return pair.getSecond();
	}

	public static void setPortalOpeningAnimationProgress(float progress, UUID uuid, boolean isPrimary)
	{
		Pair<Float, Float> pair = OPENING_ANIMATIONS.getOrDefault(uuid, new Pair<>(0F, 0F));
		if(isPrimary)
			pair = new Pair<>(progress, pair.getSecond());
		else pair = new Pair<>(pair.getFirst(), progress);

		OPENING_ANIMATIONS.put(uuid, pair);
	}
}
