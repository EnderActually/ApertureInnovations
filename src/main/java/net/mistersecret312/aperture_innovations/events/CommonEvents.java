package net.mistersecret312.aperture_innovations.events;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import mekanism.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.advancements.PortalTravelCriterion;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.config.LongFallBootsConfig;
import net.mistersecret312.aperture_innovations.init.*;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.*;
import net.mistersecret312.aperture_innovations.portal.Portal;
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

import java.text.NumberFormat;
import java.util.*;

@EventBusSubscriber(modid = ApertureInnovations.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonEvents
{
	@SubscribeEvent
	public static void levelTick(LevelTickEvent.Pre event)
	{
		Level level = event.getLevel();
		if(level instanceof ServerLevel)
		{
			PortalLinkData data = PortalLinkData.get(level);
			for(Map.Entry<UUID, PortalLink> entry : data.portalLinks.entrySet())
			{
				PortalLink link = entry.getValue();
				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					Vec3 portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();

					if(portalPos == null)
						continue;

					ResourceKey<Level> portalDim = isPrimary ? link.getPrimaryPortal().getDimension() : link.getSecondaryPortal()
																											.getDimension();
					if(!portalDim.equals(event.getLevel().dimension()))
						continue;


					float xRot = isPrimary ? link.getPrimaryPortal().getXRotation() : link.getSecondaryPortal()
																						  .getXRotation();
					float yRot = isPrimary ? link.getPrimaryPortal().getYRotation() : link.getSecondaryPortal().getYRotation();

					Direction direction = Direction.fromYRot(yRot);
					if(xRot == -90)
						direction = Direction.UP;
					if(xRot == 90)
						direction = Direction.DOWN;

					if(!link.checkForValidity(level, portalPos, xRot, yRot, direction, link.linkID, isPrimary))
					{
						if(isPrimary)
							link.resetPrimary(level);
						else link.resetSecondary(level);
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
								link.getPrimaryPortal(), link.variantKey));
					else
						PacketDistributor.sendToPlayer(serverPlayer, new ClientBoundPortalSyncPacket(uuid, false,
								link.getSecondaryPortal(), link.variantKey));
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
			if(linkID == null)
				return;
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
			if(linkID == null)
				return;

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

		ApertureCapability aperture = entity.getData(AttachmentTypeInit.APERTURE.get());
		aperture.tick(level, entity);

		if(aperture.ignorePortalsTime == 0 && !level.isClientSide())
		{
			Vec3 currentPos = entity.position().add(0, entity.getBbHeight()/2f, 0);

			Vec3 speed = entity.getDeltaMovement();
			Vec3 nextPos = currentPos.add(speed.multiply(2f, 2f, 2f));

			AABB movementBox = entity.getBoundingBox().expandTowards(speed);

			Pair<UUID, Boolean> pair = PortalUtilities.getClosestPortal(entity);
			UUID linkID = pair.getFirst();
			boolean isPrimary = pair.getSecond();
			if(linkID == null)
				return;

			PortalLink link = PortalUtilities.getPortalLinks(level).get(linkID);
			if(link == null || !link.isOpen())
				return;

			Portal portal = pair.getSecond() ? link.getPrimaryPortal() : link.getSecondaryPortal();
			Portal otherPortal = pair.getSecond() ? link.getSecondaryPortal() : link.getPrimaryPortal();

			AABB teleportBox = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
					portal.getYRotation());

			if(movementBox.intersects(teleportBox))
			{
				Direction direction = PortalUtilities.getPortalDirection(level, linkID, isPrimary);
				Direction otherDirection = PortalUtilities.getPortalDirection(level, linkID, !isPrimary);
				Vector3f normal = direction.step();

				Vec3 portalPos = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
						portal.getYRotation()).getCenter();
				portalPos = portalPos.add(direction.getOpposite().getStepX()*entity.getBbWidth()/2f,
						direction.getOpposite().getStepY()*entity.getBbHeight()/1.25f, direction.getOpposite().getStepZ()*entity.getBbWidth()/2f);

				Vec3 offsetFromPortal = currentPos.subtract(portalPos);
				Vec3 offsetPortalPlace = currentPos.subtract(portal.getPosition());
				Vec3 nextOffsetFromPortal = nextPos.subtract(portalPos);

				double relativePos = offsetFromPortal.dot(new Vec3(normal));
				double nextRelativePos = nextOffsetFromPortal.dot(new Vec3(normal));

				boolean slow = portalPos.closerThan(currentPos, 0.4f) && relativePos > 0;
				boolean fast = relativePos > 0 && nextRelativePos <= 0;

				if(slow || fast)
				{
					Quaternionf portalQ = new Quaternionf().rotationYXZ(
							(float) (Math.PI - Math.toRadians(portal.getYRotation() + (direction.getAxis().isHorizontal() ? 180 : 0))),
							(float) Math.toRadians(-portal.getXRotation()-90),
							0);

					Quaternionf otherPortalQ = new Quaternionf().rotationYXZ(
							(float) (Math.PI - Math.toRadians(otherPortal.getYRotation()+(direction.getAxis().isHorizontal() ? 180 : 0))),
							(float) Math.toRadians(-otherPortal.getXRotation()-90),
							0);

					if(direction.getAxis().isHorizontal())
						aperture.setIgnorePortalsTime(5);
					if(link.isInWorld() && link.isInterdimensionalLink())
						aperture.setIgnorePortalsTime(20);

					Vector3f newSpeed = portalQ.invert(new Quaternionf()).transform(speed.toVector3f());
					otherPortalQ.rotateZ((float) Math.toRadians(180), new Quaternionf()).transform(newSpeed);

					if(otherPortal.getXRotation() == -90 && newSpeed.length() < 0.5)
						newSpeed.add(0, 0.05f, 0);
					if(portal.getXRotation() == 0 && otherPortal.getXRotation() == -90)
						newSpeed.add(0f, 0.5f, 0f);

					Vector3f rotatedOffset = portalQ.invert(new Quaternionf()).transform(offsetPortalPlace.toVector3f());
					otherPortalQ.rotateZ((float) Math.toRadians(180), new Quaternionf()).transform(rotatedOffset);

					Vec3 targetPos;
					if(otherPortal.isMoonshot())
						targetPos = portalPos.add(0, 1000, 0);
					else targetPos = otherPortal.getPosition().subtract(0,entity.getBbHeight()/2, 0);

					targetPos = targetPos.add(otherDirection.getStepX()*0.05, 0, otherDirection.getStepZ()*0.05);

					entity.setDeltaMovement(new Vec3(newSpeed));
					entity.hasImpulse = true;
					entity.resetFallDistance();

					Quaternionf rot = new Quaternionf()
											  .rotationYXZ((180 - entity.getYRot()) * Mth.DEG_TO_RAD, -entity.getXRot() * Mth.DEG_TO_RAD, 0)
											  .premul(portalQ.invert(new Quaternionf()))
											  .premul(otherPortalQ.rotateZ((float) Math.toRadians(180), new Quaternionf()))
											  .conjugate();


					ServerLevel targetLevel;
					if(otherPortal.isMoonshot())
						targetLevel = (ServerLevel) level;
					else targetLevel = level.getServer().getLevel(otherPortal.getDimension());
					if(targetLevel == null)
						return;

					float yaw = (float) Math.atan2(-(rot.x * rot.z + rot.y * rot.w) * 2, 2 * (rot.y * rot.y + rot.z * rot.z) - 1);
					entity.setPos(targetPos);
					entity.teleportTo(targetLevel, targetPos.x, targetPos.y, targetPos.z, Set.of(),
							(float) Math.toDegrees(yaw)+(direction.getAxis().isVertical() ? 180 : 0), entity.getXRot());
					entity.setOldPosAndRot();
					if(entity instanceof ServerPlayer player)
					{
						PacketDistributor.sendToPlayer(player, new ClientboundTeleportMomentumPacket(
								newSpeed));

						aperture.setPortal(Pair.of(linkID, isPrimary));
						aperture.updateDistance();
						aperture.setFrictionlessTime(100*20);
						AdvancementInit.PORTAL_TRAVEL.get().trigger(player,
								portal.getDimension().location(),
								targetLevel.dimension().location(),
								portal.getPosition().distanceToSqr(targetPos),
								aperture.verticalDistance, aperture.horizontalDistance,
								otherPortal.isMoonshot());
					}
					else
					{
						PacketDistributor.sendToPlayersTrackingEntity(entity, new ClientboundEntityPortalLerpPacket(entity.getId(),
								targetPos.toVector3f(),(float) Math.toDegrees(yaw)+(direction.getAxis().isVertical() ? 180 : 0), entity.getXRot()));
					}
				}
			}
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
