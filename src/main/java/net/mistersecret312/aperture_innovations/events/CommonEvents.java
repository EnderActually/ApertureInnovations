package net.mistersecret312.aperture_innovations.events;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ClientBoundPortalLinkSyncPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;

import java.util.*;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents
{
	@SubscribeEvent
	public static void levelTick(TickEvent.LevelTickEvent event)
	{
		if(event.side.isServer() && event.phase.equals(TickEvent.Phase.END))
		{
			Level level = event.level;
			PortalLinkData data = PortalLinkData.get(event.level);

			for(Map.Entry<UUID, PortalLink> entry : data.portalLinks.entrySet())
			{
				UUID uuid = entry.getKey();
				PortalLink link = entry.getValue();

				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary : link.posSecondary;
					Direction direction = i == 0 ? link.directionPrimary : link.directionSecondary;
					boolean wall = i == 0 ? link.wallPrimary : link.wallSecondary;
					boolean ceiling = i == 0 ? link.ceilingPrimary : link.ceilingSecondary;

					if(link.posPrimary != null && link.posSecondary != null)
					{
						Vec3 realPos = pos.getCenter().add(Vec3.atLowerCornerOf(direction.getNormal())
															   .multiply(0.5f, 0.5f, 0.5f)
															   .add(0f, 0.5f, 0f));

						AABB portal = new AABB(pos);
						if(wall)
						{
							portal = new AABB(realPos.x-0.25, realPos.y-1, realPos.z-0.25,
									realPos.x+0.25, realPos.y+1, realPos.z+0.25);
						}
						else
						{
							Direction.Axis axis = direction.getAxis();
								if(axis.equals(Direction.Axis.X))
									portal = new AABB(realPos.x-1, realPos.y-0.25, realPos.z-0.25,
											realPos.x+1, realPos.y+0.25, realPos.z+0.25);
								else if(axis.equals(Direction.Axis.Z))
									portal = new AABB(realPos.x-0.25, realPos.y-0.25, realPos.z-1,
											realPos.x+0.25, realPos.y+0.25, realPos.z+1);
						}
						List<Entity> entitiesNearby = level.getEntitiesOfClass(Entity.class, portal.inflate(0.5D), entity -> true);

						for(Entity entity : entitiesNearby)
						{
							AABB box = entity.getBoundingBox().expandTowards(entity.getDeltaMovement().multiply(3,3,3));
							if(box.intersects(portal))
							{
								Vec3 otherPortalPos = (i == 0 ? link.posSecondary : link.posPrimary).getCenter();
								Direction otherDirection = i == 0 ? link.directionSecondary : link.directionPrimary;

								boolean otherWall = i == 0 ? link.wallSecondary : link.wallPrimary;
								boolean otherCeiling = i == 0 ? link.ceilingSecondary : link.ceilingPrimary;

								float rotation = otherDirection.toYRot() - direction.toYRot() + 180;

								otherPortalPos = otherPortalPos.add(Vec3.atLowerCornerOf(otherDirection.getNormal())
																		.multiply(1.25f, 1f, 1.25f)
																		.add(0f, 0.5f, 0f));
								if(otherWall)
									otherPortalPos = otherPortalPos.add(0, -0.9, 0);
								else
									otherPortalPos = otherPortalPos.add(0, 2, 0);
								if(!wall && otherWall)
								{
									otherPortalPos = otherPortalPos.add(
											Vec3.atLowerCornerOf(otherDirection.getNormal()));
								}

								entity.teleportTo((ServerLevel) level, otherPortalPos.x, otherPortalPos.y, otherPortalPos.z, Set.of(),
										entity.getYRot()+rotation,
										entity.getXRot());

								Vec3 oldSpeed = entity.getDeltaMovement();
								Vec3 newSpeed = entity.getDeltaMovement().yRot(rotation);
								if(!wall && !otherWall)
								{
									if(!ceiling && !otherCeiling)
										newSpeed = new Vec3(newSpeed.x, -newSpeed.y, newSpeed.z);
									if(ceiling && !otherCeiling)
										newSpeed = new Vec3(newSpeed.x, -newSpeed.y, newSpeed.z);
								}
								if(!wall && otherWall)
								{
									if(otherDirection.getAxis() == Direction.Axis.X)
										newSpeed = new Vec3(
												newSpeed.x - (otherDirection.getAxisDirection().equals(
												Direction.AxisDirection.NEGATIVE) ? -newSpeed.y : newSpeed.y),
												0, newSpeed.z);
									if(otherDirection.getAxis() == Direction.Axis.Z)
										newSpeed = new Vec3(newSpeed.x, 0,
												newSpeed.z - (otherDirection.getAxisDirection().equals(
														Direction.AxisDirection.NEGATIVE) ? -newSpeed.y : newSpeed.y));
								}
								entity.setDeltaMovement(newSpeed);
								if(entity instanceof ServerPlayer player)
									NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundTeleportMomentumPacket(newSpeed));
							}
						}
					}
				}
			}

			NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientBoundPortalLinkSyncPacket(data.portalLinks, new HashMap<>()));
		}
		else if(event.side.isClient() && event.phase.equals(TickEvent.Phase.END))
		{
			for(Map.Entry<UUID, ClientPortalLink> entry : ClientEvents.LINKS.entrySet())
			{
				UUID uuid = entry.getKey();
				ClientPortalLink link = entry.getValue();

				Level level = Minecraft.getInstance().level;

				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary() : link.posSecondary();
					Direction direction = i == 0 ? link.directionPrimary() : link.directionSecondary();

					if(pos != null)
					{
						Vec3 realPos = pos.getCenter().add(Vec3.atLowerCornerOf(direction.getNormal())
															   .multiply(0.5f, 0.5f, 0.5f)
															   .add(0f, 0.5f, 0f));
						boolean touchesPortal = Minecraft.getInstance().player.getBoundingBox().inflate(0.25f).contains(realPos);
						if(touchesPortal)
						{

						}
						level.addParticle(ParticleTypes.CRIT, realPos.x, realPos.y, realPos.z, 0f, 0f,
								0f);
					}
				}
			}
		}
	}
}
