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
				Vector3f normal = direction.step();

				Vec3 portalPos = PortalUtilities.getPortalBoundingBox(portal.getPosition(), portal.getXRotation(),
						portal.getYRotation()).getCenter();

				Vec3 offsetFromPortal = currentPos.subtract(portalPos);
				Vec3 offsetPortalPlace = currentPos.subtract(portal.getPosition());
				Vec3 nextOffsetFromPortal = nextPos.subtract(portalPos);

				double relativePos = offsetFromPortal.dot(new Vec3(normal));
				double nextRelativePos = nextOffsetFromPortal.dot(new Vec3(normal));

				boolean slow = portalPos.closerThan(currentPos, 0.4f) && relativePos > 0;
				boolean fast = relativePos > 0 && nextRelativePos <= 0;

				if(slow || fast)
				{
					float xSum = portal.getXRotation()+otherPortal.getXRotation();
					float ySum = portal.getYRotation()+otherPortal.getYRotation();

					float xRotDiff = Mth.wrapDegrees(xSum);
					float yRotDiff = Mth.wrapDegrees(ySum);

					System.out.println("This Portal - X:"+portal.getXRotation() + ", Y:"+portal.getYRotation());
					System.out.println("Other Portal - X:"+otherPortal.getXRotation() + ", Y:"+otherPortal.getYRotation());

					System.out.println("rotation degrees - X:" + xRotDiff + ", Y:" + yRotDiff);

					Quaternionf rotX = Axis.XP.rotation(xRotDiff);
					Quaternionf rotY = Axis.YP.rotation(yRotDiff);

					aperture.setIgnorePortalsTime(5);

					Vec3 newSpeed = new Vec3(speed.toVector3f().rotate(rotY).rotate(rotX));
					Vec3 rotatedOffset = new Vec3(offsetPortalPlace.toVector3f().rotate(rotY).rotate(rotX));

					Vec3 targetPos = otherPortal.getPosition().add(rotatedOffset).subtract(0,entity.getBbHeight()/2, 0);

					entity.setDeltaMovement(newSpeed);
					entity.teleportTo((ServerLevel) level, targetPos.x, targetPos.y, targetPos.z, Set.of(),
							(float) (entity.getYRot()+yRotDiff), entity.getXRot());
					if(entity instanceof ServerPlayer player)
					{
						PacketDistributor.sendToPlayer(player, new ClientboundTeleportMomentumPacket(
								newSpeed.toVector3f()));

						aperture.portal = Pair.of(linkID, isPrimary);
						aperture.updateDistance();
						aperture.setFrictionlessTime(100*20);
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
