package net.mistersecret312.aperture_innovations.events;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.config.LongFallBootsConfig;
import net.mistersecret312.aperture_innovations.init.AdvancementInit;
import net.mistersecret312.aperture_innovations.init.AttachmentTypeInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.init.StatisticsInit;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.*;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = ApertureInnovations.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonEvents
{
	@SubscribeEvent
	public static void levelTick(LevelTickEvent.Pre event)
	{
		Level level = event.getLevel();
		if(level instanceof ServerLevel serverLevel)
		{
			PortalLinkData data = PortalLinkData.get(level);
			for(Map.Entry<UUID, PortalLink> entry : PortalUtilities.getPortalLinks(serverLevel).entrySet())
			{
				PortalLink link = entry.getValue();
				UUID uuid = entry.getKey();

				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;

					Vec3 portalPos = PortalUtilities.getPortalPos(serverLevel, uuid, isPrimary);
					BlockPos portalBlockPos = isPrimary ? link.posPrimary : link.posSecondary;
					if(portalPos == null) continue;

					ResourceKey<Level> dimension = PortalUtilities.getPortalDimension(serverLevel, uuid, isPrimary);
					if(!serverLevel.dimension().equals(dimension)) continue;

					Direction portalDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, isPrimary);
					boolean isOnWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, isPrimary);
					boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid, isPrimary);

					boolean otherMoonshot = isPrimary ? link.moonshotSecondary : link.moonshotPrimary;

					AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, portalDirection, isOnWall,
							isOnCeiling);

					Vec3 boxCenter = teleportBox.getCenter();
					if(isOnWall)
					{
						boxCenter = boxCenter.relative(portalDirection.getOpposite(), 0.5D);
					}
					AABB centerBox = new AABB(boxCenter, boxCenter).inflate(0.25D);
					if(level.getBlockStates(centerBox).anyMatch(state ->
						{
							Direction checkDirection = isOnWall ? portalDirection : isOnCeiling ? Direction.DOWN : Direction.UP;
							BlockPos statePos = portalBlockPos.relative(checkDirection.getOpposite());
							boolean isSturdy = state.isFaceSturdy(level, statePos, checkDirection);
							return state.is(Blocks.AIR) || !isSturdy;
						}))
					{
						if(isPrimary) link.resetPrimary(level);
						else link.resetSecondary(level);
						continue;
					}

					if(link.isOpen())
					{
						if(portalPos != null)
						{
							if(otherMoonshot)
							{
								List<Entity> entities = level.getEntitiesOfClass(Entity.class,
										new AABB(BlockPos.containing(portalPos)).inflate(5D));
								for(Entity entity : entities)
								{
									Vec3 pushVector = portalPos.subtract(entity.position()).multiply(0.08, 0.08, 0.08);
									entity.push(pushVector.x, pushVector.y, pushVector.z);
									if(entity instanceof ServerPlayer player)
										PacketDistributor.sendToPlayer(player, new ClientboundTeleportMomentumPacket(entity.getDeltaMovement().toVector3f()));
								}
							}

							PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(BlockPos.containing(portalPos)),
									new ClientboundPortalAmbientSoundPacket(link.linkID, isPrimary, false));
						}
					}
				}
			}

			for(Entity entity : serverLevel.getAllEntities())
			{
				Pair<UUID, Boolean> pair = PortalUtilities.getClosestPortal(entity);

				UUID uuid = pair.getFirst();
				boolean isPrimary = pair.getSecond();
				if(uuid == null) return;

				Vec3 portalPos = PortalUtilities.getPortalPos(serverLevel, uuid, isPrimary);

				if(portalPos == null) continue;

				ResourceKey<Level> dimension = PortalUtilities.getPortalDimension(serverLevel, uuid, isPrimary);
				if(!serverLevel.dimension().equals(dimension)) continue;

				Direction portalDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, isPrimary);
				boolean isOnWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, isPrimary);
				boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid, isPrimary);

				AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, portalDirection, isOnWall,
						isOnCeiling);

				Vec3 entityCenter = entity.getBoundingBox().getCenter();
				AABB entityCenterBox = new AABB(entityCenter, entityCenter).inflate(0.25D, 0.5D, 0.25D);

				if(entityCenterBox.expandTowards(entity.getDeltaMovement().multiply(1, 1, 1)).intersects(teleportBox))
				{
					Vec3 otherPortalPos = PortalUtilities.getPortalPos(serverLevel, uuid, !isPrimary);
					PortalLink link = PortalUtilities.getPortalLinks(serverLevel).get(uuid);
					boolean otherMoonshot = isPrimary ? link.moonshotSecondary : link.moonshotPrimary;
					if(otherPortalPos == null && otherMoonshot) otherPortalPos = portalPos.add(0, 1000, 0);
					if(otherPortalPos == null) continue;

					Direction otherDirection = PortalUtilities.getPortalDirection(serverLevel, uuid,
							otherMoonshot == isPrimary);

					boolean otherWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, otherMoonshot == isPrimary);
					boolean otherCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid,
							otherMoonshot == isPrimary);
					ResourceKey<Level> otherDimension = PortalUtilities.getPortalDimension(serverLevel, uuid,
							otherMoonshot == isPrimary);

					float rotation = otherDirection.toYRot() - portalDirection.toYRot() + 180;
					AABB otherTeleportBox = PortalUtilities.getPortalTeleportBox(otherPortalPos, otherDirection,
							otherWall, otherCeiling);

					otherPortalPos = otherTeleportBox.getCenter();
					if(otherWall) otherPortalPos = otherPortalPos.add(0, -entity.getBoundingBox().getYsize() * 0.45, 0)
																 .add(Vec3.atLowerCornerOf(otherDirection.getNormal())
																		  .multiply(0.35f, 1f, 0.35f));
					else otherPortalPos = otherPortalPos.add(0, 0.1, 0);
					if(!isOnWall && otherWall)
					{
						otherPortalPos = otherPortalPos.add(Vec3.atLowerCornerOf(otherDirection.getNormal()));
					}

					if(!otherWall && !isOnWall && !isOnCeiling && otherCeiling)
					{
						otherPortalPos = otherPortalPos.add(0, -2, 0);
					}
					if(!otherWall && !isOnWall && !isOnCeiling && !otherCeiling)
					{
						otherPortalPos = otherPortalPos.add(0, 1, 0);
					}
					if(!otherWall && !isOnWall && isOnCeiling && !otherCeiling)
					{
						otherPortalPos = otherPortalPos.add(0, 1, 0);
					}
					if(isOnWall && otherCeiling)
					{
						otherPortalPos = otherPortalPos.add(0, -2, 0);
					}
					if(isOnWall && !otherWall && !otherCeiling)
					{
						otherPortalPos = otherPortalPos.add(0, 1, 0);
					}

					Vector3f oldSpeed = entity.getDeltaMovement().toVector3f();

					ServerLevel otherPortalLevel = serverLevel.getServer().getLevel(otherDimension);

					entity.teleportTo(otherPortalLevel, otherPortalPos.x, otherPortalPos.y,
							otherPortalPos.z, Set.of(),
							entity.getYRot() + ((!isOnWall && otherWall) ? rotation + 180 : rotation),
							entity.getXRot());

					Vec3 otherPos = otherPortalPos;

					PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(BlockPos.containing(portalPos)),
							new ClientboundPortalSoundsPacket.EnterPortal(link.linkID, isPrimary));
					PacketDistributor.sendToPlayersTrackingChunk(otherPortalLevel, new ChunkPos(BlockPos.containing(otherPos)),
							new ClientboundPortalSoundsPacket.EnterPortal(link.linkID, isPrimary));

					Quaternionf rotationQ = new Quaternionf(Axis.YP.rotationDegrees(rotation - 180));
					if(isOnWall && otherWall)
					{
						if(rotation == 0)
							rotationQ = new Quaternionf();
						if(rotation == 180)
							rotationQ = new Quaternionf(Axis.YP.rotationDegrees(180));
					}

					Vector3f newSpeed = oldSpeed.rotate(rotationQ);
					if(!isOnWall && !otherWall)
					{
						if(!isOnCeiling && !otherCeiling)
							newSpeed = new Vector3f(newSpeed.x, -newSpeed.y + (link.isInterdimensionalLink() ? 0.25f : 0f), newSpeed.z);
						if(isOnCeiling && !otherCeiling)
							newSpeed = new Vector3f(newSpeed.x, -newSpeed.y, newSpeed.z);
					}
					//						if(!isOnWall && otherWall)
					//							newSpeed.mul(0.1f, 1f, 0.1f).rotateX(-90);
					if(!isOnWall && otherWall)
					{
						if(otherDirection.getAxis() == Direction.Axis.X) newSpeed = new Vector3f(
								newSpeed.x - (otherDirection.getAxisDirection()
															.equals(Direction.AxisDirection.NEGATIVE) ? -newSpeed.y : newSpeed.y),
								0, 0);
						if(otherDirection.getAxis() == Direction.Axis.Z) newSpeed = new Vector3f(0, 0,
								newSpeed.z + (otherDirection.getAxisDirection()
															.equals(Direction.AxisDirection.NEGATIVE) ? newSpeed.y : -newSpeed.y));
					}
					if(isOnWall && !otherWall && !otherCeiling)
					{
						newSpeed = new Vector3f(0F, (float) (0.25F + entity.getDeltaMovement().length()), 0F);
					}
					entity.setDeltaMovement(new Vec3(newSpeed));
					entity.resetFallDistance();
					if(entity instanceof ServerPlayer player)
					{
						player.awardStat(StatisticsInit.TIMES_USED_PORTALS.get(), 1);
						PacketDistributor.sendToPlayer(player, new ClientboundTeleportMomentumPacket(newSpeed));
					}
					Vec3 mathOtherPos = otherPortalPos;
					ApertureCapability aperture = entity.getData(AttachmentTypeInit.APERTURE);

					aperture.portal = new Pair<>(uuid, !isPrimary);
					aperture.updateDistance();
					aperture.setFrictionlessTime(400);

					entity.setData(AttachmentTypeInit.APERTURE, aperture);
					if(entity instanceof ServerPlayer player)
					{
						AdvancementInit.PORTAL_TRAVEL.get().trigger(player, dimension.location(),
								otherDimension.location(), portalPos.distanceToSqr(mathOtherPos),
								aperture.verticalDistance, aperture.horizontalDistance, otherMoonshot);
					}
				}
			}
		}
	}


	@SubscribeEvent
	public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		Player player = event.getEntity();
		if(player instanceof ServerPlayer serverPlayer)
		{
			PortalLinkData data = PortalLinkData.get(serverPlayer.level());
			data.portalLinks.forEach((uuid, link) -> {
				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					if(isPrimary)
						PacketDistributor.sendToPlayer(serverPlayer, new ClientBoundPortalSyncPacket(uuid, true,
								link.posPrimary, link.directionPrimary, link.wallPrimary, link.ceilingPrimary,
								link.dimensionPrimary, link.moonshotPrimary, link.variantKey, link.primaryPortalColor));
					else
						PacketDistributor.sendToPlayer(serverPlayer, new ClientBoundPortalSyncPacket(uuid, false,
								link.posSecondary, link.directionSecondary, link.wallSecondary, link.ceilingSecondary,
								link.dimensionSecondary, link.moonshotSecondary, link.variantKey, link.secondaryPortalColor));
				}
			});
		}
	}

	@SubscribeEvent
	public static void playerFall(EntityInvulnerabilityCheckEvent event)
	{
		Entity entity = event.getEntity();
		if(entity instanceof LivingEntity living)
		{
			for(ItemStack stack : living.getArmorSlots())
			{
				if((event.getSource().equals(entity.damageSources().fall()) || event.getSource().equals(entity.damageSources().flyIntoWall()))
						   && stack.getItem() instanceof LongFallBootsItem)
				{
					if(!living.level().isClientSide())
					{
						@Nullable IEnergyStorage cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
						if(cap != null && cap instanceof ApertureEnergy energy)
						{
							if(LongFallBootsConfig.long_fall_boots_use_energy.get())
							{
								long toExtract = LongFallBootsConfig.fall_energy_consumption.get();
								long extracted = energy.extractLongEnergy(toExtract, false);
								if(extracted < toExtract)
									return;
							}
						}
					}
					ServerLevel level = (ServerLevel) living.level();
					level.playSound(null, living.blockPosition(), SoundInit.LONG_FALL_BOOTS_LAND.get(),
							SoundSource.PLAYERS, 0.15F, 1F);
					event.setInvulnerable(true);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public static void playerDied(LivingDeathEvent event)
	{
		LivingEntity living = event.getEntity();
		Level level = living.level();
		if(living instanceof ServerPlayer player)
		{
			Pair<UUID, Boolean> closestPortal = PortalUtilities.getClosestPortal(player);
			UUID linkID = closestPortal.getFirst();
			boolean isPrimary = closestPortal.getSecond();

			Vec3 portalPos = PortalUtilities.getPortalPos(level, linkID, isPrimary);
			if(portalPos == null)
				return;

			boolean onWall = PortalUtilities.isPortalOnWall(level, linkID, isPrimary);
			boolean onCeiling = PortalUtilities.isPortalOnCeiling(level, linkID, isPrimary);

			double distance = portalPos.distanceTo(player.position());
			if(distance > 16)
				return;

			AdvancementInit.NEAR_PORTAL_DEATH.get().trigger(player, distance, !onWall && !onCeiling);
		}
	}

	@SubscribeEvent
	public static void totemDeath(LivingUseTotemEvent event)
	{
		LivingEntity living = event.getEntity();
		Level level = living.level();
		if(living instanceof ServerPlayer player)
		{
			Pair<UUID, Boolean> closestPortal = PortalUtilities.getClosestPortal(player);
			UUID linkID = closestPortal.getFirst();
			boolean isPrimary = closestPortal.getSecond();

			Vec3 portalPos = PortalUtilities.getPortalPos(level, linkID, isPrimary);
			if(portalPos == null)
				return;

			boolean onWall = PortalUtilities.isPortalOnWall(level, linkID, isPrimary);
			boolean onCeiling = PortalUtilities.isPortalOnCeiling(level, linkID, isPrimary);

			long distance = (long) portalPos.distanceTo(player.position());
			if(distance > 16)
				return;

			AdvancementInit.NEAR_PORTAL_DEATH.get().trigger(player, distance, !onWall && !onCeiling);
		}
	}

	@SubscribeEvent
	public static void livingTick(EntityTickEvent.Pre event)
	{
		Entity entity = event.getEntity();
		Level level = entity.level();
		if(entity instanceof LivingEntity living)
		{
			ApertureCapability aperture = living.getData(AttachmentTypeInit.APERTURE);
			aperture.tick(level, living);
		}
	}

	@SubscribeEvent
	public static void itemToss(ItemTossEvent event)
	{
		if(event.getEntity().getItem().getItem() instanceof PortalGunItem)
		{
			event.getEntity().setThrower(event.getPlayer());
		}
	}
}
