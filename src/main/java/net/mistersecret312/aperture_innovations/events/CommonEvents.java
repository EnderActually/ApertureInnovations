package net.mistersecret312.aperture_innovations.events;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.advancements.NearPortalDeathCriterion;
import net.mistersecret312.aperture_innovations.advancements.PortalTravelCriterion;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.capabilities.GenericProvider;
import net.mistersecret312.aperture_innovations.config.LongFallBootsConfig;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.init.StatisticsInit;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.ClientBoundPortalLinkSyncPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalAmbientSoundPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;
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
				for(Map.Entry<UUID, PortalLink> entry : PortalUtilities.getPortalLinks(serverLevel).entrySet())
				{
					PortalLink link = entry.getValue();
					UUID uuid = entry.getKey();

					for(int i = 0; i < 2; i++)
					{
						boolean isPrimary = i == 0;

						Vec3 portalPos = PortalUtilities.getPortalPos(serverLevel, uuid, isPrimary);
						BlockPos portalBlockPos = isPrimary ? link.posPrimary : link.posSecondary;
						if(portalPos == null)
							continue;

						ResourceKey<Level> dimension = PortalUtilities.getPortalDimension(serverLevel, uuid, isPrimary);
						if(!serverLevel.dimension().equals(dimension))
							continue;

						Direction portalDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, isPrimary);
						boolean isOnWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, isPrimary);
						boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid, isPrimary);

						boolean otherMoonshot = isPrimary ? link.moonshotSecondary : link.moonshotPrimary;

						AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, portalDirection, isOnWall, isOnCeiling);

						Vec3 boxCenter = teleportBox.getCenter();
						if(isOnWall)
						{
							boxCenter = boxCenter.relative(portalDirection.getOpposite(), 0.5D);
						}
						AABB centerBox = new AABB(boxCenter, boxCenter).inflate(0.25D);
						if(level.getBlockStates(centerBox).anyMatch(
						state -> {
							Direction checkDirection = isOnWall ? portalDirection : isOnCeiling ? Direction.DOWN : Direction.UP;
							BlockPos statePos = portalBlockPos.relative(checkDirection.getOpposite());
							boolean isSturdy = state.isFaceSturdy(level, statePos, checkDirection);
							return state.is(Blocks.AIR) || !isSturdy;
						}))
						{
							if(isPrimary)
								link.resetPrimary(level);
							else link.resetSecondary(level);
							continue;
						}

						if(link.isOpen())
						{
							if(portalPos != null)
							{
								if(otherMoonshot)
								{
									List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(BlockPos.containing(portalPos))
																				   .inflate(5D));
									for(Entity entity : entities)
									{
										Vec3 pushVector = portalPos.subtract(entity.position())
																   .multiply(0.08, 0.08, 0.08);
										entity.push(pushVector.x, pushVector.y, pushVector.z);
										if(entity instanceof ServerPlayer player)
											NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
													new ClientboundTeleportMomentumPacket(entity.getDeltaMovement()));

									}
								}

								NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
												BlockPos.containing(portalPos))),
									new ClientboundPortalAmbientSoundPacket(link.linkID, isPrimary, false));
							}
						}

						if(portalPos != null)
						{
							if(i == 0)
								link.openingPrimary++;
							else link.openingSecondary++;
						}
					}
				}

				for(Entity entity : serverLevel.getAllEntities())
				{
					Pair<UUID, Boolean> pair = PortalUtilities.getClosestPortal(entity);

					UUID uuid = pair.getFirst();
					boolean isPrimary = pair.getSecond();
					if(uuid == null)
						return;

					Vec3 portalPos = PortalUtilities.getPortalPos(serverLevel, uuid, isPrimary);

					if(portalPos == null)
						continue;

					ResourceKey<Level> dimension = PortalUtilities.getPortalDimension(serverLevel, uuid, isPrimary);
					if(!serverLevel.dimension().equals(dimension))
						continue;

					Direction portalDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, isPrimary);
					boolean isOnWall = PortalUtilities.isPortalOnWall(serverLevel, uuid, isPrimary);
					boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid, isPrimary);

					AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, portalDirection, isOnWall, isOnCeiling);

					Vec3 entityCenter = entity.getBoundingBox().getCenter();
					AABB entityCenterBox = new AABB(entityCenter, entityCenter).inflate(0.25D, 0.5D, 0.25D);

					if(entityCenterBox.expandTowards(entity.getDeltaMovement().multiply(1, 1, 1)).intersects(teleportBox))
					{
						Vec3 otherPortalPos = PortalUtilities.getPortalPos(serverLevel, uuid, !isPrimary);
						PortalLink link = PortalUtilities.getPortalLinks(serverLevel).get(uuid);
						boolean otherMoonshot = isPrimary ? link.moonshotSecondary : link.moonshotPrimary;
						if(otherPortalPos == null && otherMoonshot)
							otherPortalPos = portalPos.add(0, 1000, 0);
						if(otherPortalPos == null)
							continue;

						Direction otherDirection = PortalUtilities.getPortalDirection(serverLevel, uuid, otherMoonshot == isPrimary);

						boolean otherWall = PortalUtilities.isPortalOnWall(serverLevel, uuid,
								otherMoonshot == isPrimary);
						boolean otherCeiling = PortalUtilities.isPortalOnCeiling(serverLevel, uuid,
								otherMoonshot == isPrimary);
						ResourceKey<Level> otherDimension = PortalUtilities.getPortalDimension(serverLevel, uuid,
								otherMoonshot == isPrimary);

						float rotation = otherDirection.toYRot() - portalDirection.toYRot() + 180;
						AABB otherTeleportBox = PortalUtilities.getPortalTeleportBox(otherPortalPos, otherDirection,
								otherWall, otherCeiling);

						otherPortalPos = otherTeleportBox.getCenter();
						if(otherWall)
							otherPortalPos = otherPortalPos.add(0, -entity.getBoundingBox().getYsize()*0.45, 0).add(
									Vec3.atLowerCornerOf(otherDirection.getNormal()).multiply(0.35f, 1f, 0.35f));
						else
							otherPortalPos = otherPortalPos.add(0, 0.1, 0);
						if(!isOnWall && otherWall)
						{
							otherPortalPos = otherPortalPos.add(
									Vec3.atLowerCornerOf(otherDirection.getNormal()));
						}

						if(!otherWall && !isOnWall && !isOnCeiling && otherCeiling)
						{
							otherPortalPos = otherPortalPos.add(0, -3, 0);
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
							otherPortalPos = otherPortalPos.add(0, -3, 0);
						}
						if(isOnWall && !otherWall && !otherCeiling)
						{
							otherPortalPos = otherPortalPos.add(0, 1, 0);
						}

						Vector3f oldSpeed = entity.getDeltaMovement().toVector3f();

						Level otherPortalLevel = serverLevel.getServer().getLevel(otherDimension);

						entity.teleportTo((ServerLevel) otherPortalLevel, otherPortalPos.x, otherPortalPos.y, otherPortalPos.z, Set.of(),
								entity.getYRot()+ ((!isOnWall && otherWall) ? rotation+180 : rotation),
								entity.getXRot());

						Vec3 otherPos = otherPortalPos;

						NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
										BlockPos.containing(portalPos))),
								new ClientboundPortalSoundsPacket.EnterPortal(uuid, isPrimary));
						NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> otherPortalLevel.getChunkAt(
										BlockPos.containing(otherPos))),
								new ClientboundPortalSoundsPacket.EnterPortal(uuid, isPrimary));

						Quaternionf rotationQ = new Quaternionf(Axis.YP.rotationDegrees(rotation-180));

						Vector3f newSpeed = oldSpeed.rotate(rotationQ);
						if(!isOnWall && !otherWall)
						{
							if(!isOnCeiling && !otherCeiling)
								newSpeed = new Vector3f(newSpeed.x, -newSpeed.y + (link.isInterdimensionalLink() ? 0.25f : 0f), newSpeed.z);
							if(isOnCeiling && !otherCeiling)
								newSpeed = new Vector3f(newSpeed.x, newSpeed.y, newSpeed.z);
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
						if(isOnWall && !otherWall && !otherCeiling)
						{
							newSpeed = new Vector3f(0F, (float) (0.25F+entity.getDeltaMovement().length()), 0F);
						}
						entity.setDeltaMovement(new Vec3(newSpeed));
						entity.resetFallDistance();
						if(entity instanceof ServerPlayer player)
						{
							player.awardStat(StatisticsInit.TIMES_USED_PORTALS.get(), 1);
							NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
									new ClientboundTeleportMomentumPacket(new Vec3(newSpeed)));
						}
						Vec3 mathOtherPos = otherPortalPos;
						entity.getCapability(CapabilityInit.APERTURE).ifPresent(cap ->
							{
								cap.portal = new Pair<>(uuid, !isPrimary);

								cap.updateDistance();
								cap.setFrictionlessTime(100*20);
								if(entity instanceof ServerPlayer player)
									PortalTravelCriterion.INSTANCE.trigger(player, dimension.location(), otherDimension.location(),
											(long) portalPos.distanceToSqr(mathOtherPos), (long) cap.verticalDistance,
											(long) cap.horizontalDistance, otherMoonshot);
							});
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
			NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
					new ClientBoundPortalLinkSyncPacket(data.portalLinks, new HashMap<>()));
		}
	}

	@SubscribeEvent
	public static void playerFall(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntity();
		DamageSource source = event.getSource();
		for(ItemStack stack : living.getArmorSlots())
		{
			if((source.equals(living.damageSources().fall()) || source.equals(living.damageSources().flyIntoWall()))
					&& stack.getItem() instanceof LongFallBootsItem)
			{
				if(!living.level().isClientSide())
				{
					ServerLevel level = (ServerLevel) living.level();

					level.playSound(null, living.blockPosition(), SoundInit.LONG_FALL_BOOTS_LAND.get(),
							SoundSource.PLAYERS, 0.15F, 1F);
				}
				event.setCanceled(true);
				return;
			}
		}
	}

	@SubscribeEvent
	public static void playerFallDamage(LivingFallEvent event)
	{
		LivingEntity living = event.getEntity();
		for(ItemStack stack : living.getArmorSlots())
		{
			if(stack.getItem() instanceof LongFallBootsItem)
			{

				if(!living.level().isClientSide() && event.getDistance() > 5f)
				{
					if(LongFallBootsConfig.long_fall_boots_use_energy.get() && !consumeEnergy(stack))
						return;

					ServerLevel level = (ServerLevel) living.level();

					level.playSound(null, living.blockPosition(), SoundInit.LONG_FALL_BOOTS_LAND.get(),
							SoundSource.PLAYERS, 0.15F, 1F);
				}
				event.setCanceled(true);
				return;
			}
		}
	}

	public static boolean consumeEnergy(ItemStack stack)
	{
		Optional<IEnergyStorage> optionalCapability = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
		if(optionalCapability.isPresent())
		{
			IEnergyStorage storage = optionalCapability.get();
			if(storage instanceof ApertureEnergy energy)
			{
				long requiredEnergy = LongFallBootsConfig.fall_energy_consumption.get();
				if(energy.getTrueEnergyStored() >= requiredEnergy)
				{
					energy.extractLongEnergy(requiredEnergy, false);
					return true;
				}
				else return false;
			}
		}
		return false;
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

			long distance = (long) portalPos.distanceTo(player.position());
			if(distance > 16)
				return;

			NearPortalDeathCriterion.INSTANCE.trigger(player, distance, !onWall && !onCeiling);
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

			NearPortalDeathCriterion.INSTANCE.trigger(player, distance, !onWall && !onCeiling);
		}
	}

	@SubscribeEvent
	public static void livingTick(LivingEvent.LivingTickEvent event)
	{
		event.getEntity().getCapability(CapabilityInit.APERTURE).ifPresent(cap ->
			{
				cap.tick(event.getEntity().level(), event.getEntity());
			});
	}

	@SubscribeEvent
	public static void itemToss(ItemTossEvent event)
	{
		if(event.getEntity().getItem().getItem() instanceof PortalGunItem)
		{
			event.getEntity().setThrower(event.getPlayer().getUUID());
		}
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(new ResourceLocation(ApertureInnovations.MODID, "aperture"),
					new GenericProvider<>(CapabilityInit.APERTURE, new ApertureCapability()));
	}
}
