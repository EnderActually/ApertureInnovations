package net.mistersecret312.aperture_innovations.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.mistersecret312.aperture_innovations.events.ClientEvents;
import net.mistersecret312.aperture_innovations.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(Entity.class)
public class EntityMixin
{
	@Redirect(method = "collideBoundingBox", at = @At(value = "INVOKE",
			target = "Lcom/google/common/collect/ImmutableList$Builder;addAll(Ljava/lang/Iterable;)Lcom/google/common/collect/ImmutableList$Builder;",
	ordinal = 1))
	private static ImmutableList.Builder<VoxelShape> filterPortalCollision(ImmutableList.Builder<VoxelShape> builder,
																		   Iterable<? extends VoxelShape> collection,
																		   @Local(argsOnly = true) Entity entity,
																		   @Local(argsOnly = true) AABB collisionBox,
																		   @Local(argsOnly = true) Vec3 velocity,
																		   @Local(argsOnly = true) Level level)
	{
		Iterable<VoxelShape> iterator = level.getBlockCollisions(entity, collisionBox.expandTowards(velocity));
		List<VoxelShape> shapeList = new ArrayList<>();
		iterator.forEach(shapeList::add);

		if(entity instanceof Player living)
		{
			living.isInWall();
		}
		if(entity != null)
		{
			AABB closestPortal = new AABB(BlockPos.ZERO);
			double closestDistance = Double.MAX_VALUE;

			if(level.isClientSide())
			{
				for(Map.Entry<UUID, ClientPortalLink> entry : ClientEvents.LINKS.entrySet())
				{
					ClientPortalLink link = entry.getValue();
					for(int i = 0; i < 2; i++)
					{
						BlockPos pos = i == 0 ? link.posPrimary() : link.posSecondary();
						Direction direction = i == 0 ? link.directionPrimary() : link.directionSecondary();
						boolean isWall = i == 0 ? link.wallPrimary() : link.wallSecondary();
						boolean isCeiling = i == 0 ? link.ceilingPrimary() : link.ceilingSecondary();

						if(pos == null)
							continue;

						double distance = pos.distSqr(entity.blockPosition());

						if(closestDistance > distance)
						{
							closestDistance = distance;
							closestPortal = new AABB(pos).expandTowards(isWall ? new Vec3(0, 1, 0)
																				: Vec3.atLowerCornerOf(isCeiling ? direction.getNormal().below() : direction.getNormal().above()));
						}
					}

				}
			}
			else
			{
				PortalLinkData linkData = PortalLinkData.get(level);
				for(Map.Entry<UUID, PortalLink> entry : linkData.portalLinks.entrySet())
				{
					PortalLink link = entry.getValue();
					for(int i = 0; i < 2; i++)
					{
						BlockPos pos = i == 0 ? link.posPrimary : link.posSecondary;
						Direction direction = i == 0 ? link.directionPrimary : link.directionSecondary;
						boolean isWall = i == 0 ? link.wallPrimary : link.wallSecondary;
						boolean isCeiling = i == 0 ? link.ceilingPrimary : link.ceilingSecondary;

						if(pos == null) continue;

						double distance = pos.distSqr(entity.blockPosition());

						if(closestDistance > distance)
						{
							closestDistance = distance;
							closestPortal = new AABB(pos).expandTowards(
									isWall ? new Vec3(0, 1, 0) : Vec3.atLowerCornerOf(
											isCeiling ? direction.getNormal().below() : direction.getNormal().above()));
						}
					}
				}
			}

			AABB portal = closestPortal.inflate(0.1f, 0.1f, 0.1f);
			double dist = closestDistance;
			shapeList.removeIf(shape -> {
				boolean remove = shape.bounds().intersects(portal) && dist < 9;

				return remove;
			});
		}

		return builder.addAll(shapeList);
	}

	@Inject(method = "isInWall()Z", at = @At("RETURN"), cancellable = true)
	public void notInWall(CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = (Entity) (Object) this;

		AABB closestPortal = new AABB(BlockPos.ZERO);
		double closestDistance = Double.MAX_VALUE;

		if(entity.level().isClientSide())
		{
			for(Map.Entry<UUID, ClientPortalLink> entry : ClientEvents.LINKS.entrySet())
			{
				ClientPortalLink link = entry.getValue();
				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary() : link.posSecondary();
					Direction direction = i == 0 ? link.directionPrimary() : link.directionSecondary();
					boolean isWall = i == 0 ? link.wallPrimary() : link.wallSecondary();
					boolean isCeiling = i == 0 ? link.ceilingPrimary() : link.ceilingSecondary();

					if(pos == null)
						continue;

					double distance = pos.distSqr(entity.blockPosition());

					if(closestDistance > distance)
					{
						closestDistance = distance;
						closestPortal = new AABB(pos).expandTowards(isWall ? new Vec3(0, 1, 0)
																			: Vec3.atLowerCornerOf(isCeiling ? direction.getNormal().below() : direction.getNormal().above()));
					}
				}

			}
		}
		else
		{
			PortalLinkData linkData = PortalLinkData.get(entity.level());
			for(Map.Entry<UUID, PortalLink> entry : linkData.portalLinks.entrySet())
			{
				PortalLink link = entry.getValue();
				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary : link.posSecondary;
					Direction direction = i == 0 ? link.directionPrimary : link.directionSecondary;
					boolean isWall = i == 0 ? link.wallPrimary : link.wallSecondary;
					boolean isCeiling = i == 0 ? link.ceilingPrimary : link.ceilingSecondary;

					if(pos == null) continue;

					double distance = pos.distSqr(entity.blockPosition());

					if(closestDistance > distance)
					{
						closestDistance = distance;
						closestPortal = new AABB(pos).expandTowards(
								isWall ? new Vec3(0, 1, 0) : Vec3.atLowerCornerOf(
										isCeiling ? direction.getNormal().below() : direction.getNormal().above()));
					}
				}
			}
		}

		AABB portal = closestPortal.inflate(0.1f, 0.1f, 0.1f);
		if(closestDistance < 9)
		{
			cir.setReturnValue(!portal.intersects(entity.getBoundingBox()));
		}
	}
}
