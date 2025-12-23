package net.mistersecret312.aperture_innovations.events;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
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
import net.minecraftforge.event.entity.living.LivingEvent;
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
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
			if(level instanceof ServerLevel serverLevel)
			{
				for(Entity entity : serverLevel.getAllEntities())
				{
					Pair<UUID, Boolean> pair = PortalUtilities.getClosestPortal(entity);
					UUID uuid = pair.getFirst();
					boolean isPrimary = pair.getSecond();
					if(uuid == null)
						return;

					Vec3 portalPos = PortalUtilities.getPortalPos(serverLevel, uuid, isPrimary);
					Direction portalDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, isPrimary);
					boolean isOnWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, isPrimary);
					boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid, isPrimary);

					AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, portalDirection, isOnWall);

					if(entity.getBoundingBox().expandTowards(entity.getDeltaMovement().multiply(0.1, 0.1, 0.1)).intersects(teleportBox))
					{
						Vec3 otherPortalPos = PortalUtilities.getPortalPos(serverLevel, uuid, !isPrimary);
						Direction otherDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, !isPrimary);

						boolean otherWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, !isPrimary);
						boolean otherCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid, !isPrimary);

						float rotation = otherDirection.toYRot() - portalDirection.toYRot() + 180;

						if(otherWall)
							otherPortalPos = otherPortalPos.add(0, -0.9, 0).add(
									Vec3.atLowerCornerOf(otherDirection.getNormal()).multiply(0.35f, 1f, 0.35f));
						else
							otherPortalPos = otherPortalPos.add(0, 2, 0);
						if(!isOnWall && otherWall)
						{
							otherPortalPos = otherPortalPos.add(
									Vec3.atLowerCornerOf(otherDirection.getNormal()));
						}
						Vector3f oldSpeed = entity.getDeltaMovement().toVector3f();

						entity.teleportTo((ServerLevel) level, otherPortalPos.x, otherPortalPos.y, otherPortalPos.z, Set.of(),
								entity.getYRot()+ ((!isOnWall && otherWall) ? rotation+180 : rotation),
								(!isOnWall && otherWall) ? entity.getXRot() : entity.getXRot());

						Quaternionf rotationQ = new Quaternionf(Axis.YP.rotationDegrees(rotation-180));

						Vector3f newSpeed = oldSpeed.rotate(rotationQ);
						if(!isOnWall && !otherWall)
						{
							if(!isOnCeiling && !otherCeiling)
								newSpeed = new Vector3f(newSpeed.x, -newSpeed.y, newSpeed.z);
							if(isOnCeiling && !otherCeiling)
								newSpeed = new Vector3f(newSpeed.x, -newSpeed.y, newSpeed.z);
						}
//						if(!isOnWall && otherWall)
//							newSpeed.mul(0.1f, 1f, 0.1f).rotateX(-90);
						if(!isOnWall && otherWall)
						{
							if(otherDirection.getAxis() == Direction.Axis.X)
								newSpeed = new Vector3f(
										newSpeed.x - (otherDirection.getAxisDirection().equals(
												Direction.AxisDirection.NEGATIVE) ? -newSpeed.y : newSpeed.y),
										0,
										0);
							if(otherDirection.getAxis() == Direction.Axis.Z)
								newSpeed = new Vector3f(0,
										0,
										newSpeed.z + (otherDirection.getAxisDirection().equals(
												Direction.AxisDirection.NEGATIVE) ? newSpeed.y : -newSpeed.y));
						}
						entity.setDeltaMovement(new Vec3(newSpeed));
						if(entity instanceof ServerPlayer player)
							NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundTeleportMomentumPacket(new Vec3(newSpeed), otherPortalPos, entity.getYRot()+rotation));

					}
				}
			}

			NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientBoundPortalLinkSyncPacket(data.portalLinks, new HashMap<>()));
		}
	}
}
