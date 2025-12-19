package net.mistersecret312.aperture_innovations.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPacketListenerMixin
{
	@Redirect(method = "isPlayerCollidingWithAnythingNew(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/phys/AABB;DDD)Z",
	at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/lang/Iterable;"))
	public Iterable<VoxelShape> portalCollision(LevelReader instance, Entity entity, AABB aabb)
	{
		Iterable<VoxelShape> iterator = instance.getBlockCollisions(entity, aabb);
		List<VoxelShape> shapeList = new ArrayList<>();
		iterator.forEach(shapeList::add);
		if(instance instanceof ClientLevel client)
		{
			System.out.println("CLIENT BITCH");
		}

		if(entity != null && instance instanceof ServerLevel serverLevel)
		{
			AABB closestPortal = new AABB(BlockPos.ZERO);
			double closestDistance = Double.MAX_VALUE;

			PortalLinkData linkData = PortalLinkData.get(serverLevel);
			for(Map.Entry<UUID, PortalLink> entry : linkData.portalLinks.entrySet())
			{
				PortalLink link = entry.getValue();
				for(int i = 0; i < 2; i++)
				{
					BlockPos pos = i == 0 ? link.posPrimary : link.posSecondary;
					Direction direction = i == 0 ? link.directionPrimary : link.directionSecondary;
					boolean isWall = i == 0 ? link.wallPrimary : link.wallSecondary;
					boolean isCeiling = i == 0 ? link.ceilingPrimary : link.ceilingSecondary;

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

			AABB portal = closestPortal.inflate(0.1f, 0.1f, 0.1f);
			double dist = closestDistance;
			shapeList.removeIf(shape -> {
				boolean remove = shape.bounds().intersects(portal) && dist < 9;

				return remove;
			});
		}

		return shapeList;
	}
}
