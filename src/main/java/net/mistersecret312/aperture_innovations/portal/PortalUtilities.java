package net.mistersecret312.aperture_innovations.portal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.events.ClientEvents;

import java.util.*;

public class PortalUtilities
{
	public static HashMap<UUID, PortalLink> getPortalLinks(Level level)
	{
		if(level.isClientSide())
			return new HashMap<>();

		PortalLinkData data = PortalLinkData.get(level);
		return data.portalLinks;
	}

	public static HashMap<UUID, ClientPortalLink> getPortalLinks()
	{
		return ClientEvents.LINKS;
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
	
	public static AABB getPortalBoundingBox(Vec3 portalPos, Direction portalDirection, boolean isOnWall)
	{
		AABB portal = new AABB(BlockPos.ZERO);
		if(isOnWall)
		{
			portal = new AABB(portalPos.x-0.25, portalPos.y-1, portalPos.z-0.25,
					portalPos.x+0.25, portalPos.y+1, portalPos.z+0.25);
		}
		else
		{
			Direction.Axis axis = portalDirection.getAxis();
			if(axis.equals(Direction.Axis.X))
				portal = new AABB(portalPos.x-1, portalPos.y-0.25, portalPos.z-0.25,
						portalPos.x+1, portalPos.y+0.25, portalPos.z+0.25);
			else if(axis.equals(Direction.Axis.Z))
				portal = new AABB(portalPos.x-0.25, portalPos.y-0.25, portalPos.z-1,
						portalPos.x+0.25, portalPos.y+0.25, portalPos.z+1);
		}

		return portal;
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
				ClientPortalLink link = entry.getValue();
				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary() : link.posSecondary();
					if(pos == null)
						continue;

					double distance = entity.position().distanceTo(Vec3.atLowerCornerOf(pos));
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
				PortalLink link = entry.getValue();
				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary : link.posSecondary;
					if(pos == null)
						continue;

					double distance = entity.position().distanceTo(Vec3.atLowerCornerOf(pos));
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
}
