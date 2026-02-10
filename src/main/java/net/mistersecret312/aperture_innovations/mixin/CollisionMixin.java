package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(CollisionGetter.class)
public interface CollisionMixin
{

	@Inject(method = "getBlockCollisions",
	at = @At("RETURN"), cancellable = true)
	default void stopCollision(Entity entity, AABB collisionBox, CallbackInfoReturnable<Iterable<VoxelShape>> cir)
	{
		Iterable<VoxelShape> shapes = cir.getReturnValue();
		List<VoxelShape> list = new ArrayList<>();
		shapes.forEach(list::add);

		if(entity == null)
		{
			cir.setReturnValue(shapes);
			return;
		}
		Level level = entity.level();
		if(level == null)
		{
			cir.setReturnValue(shapes);
			return;
		}

		Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(entity);

		UUID uuid = portal.getFirst();
		boolean isPrimary = portal.getSecond();
		if(uuid == null)
		{
			cir.setReturnValue(shapes);
			return;
		}

		boolean isOpen = PortalUtilities.isPortalOpen(level, uuid);

		Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);

		Vec2 rotation = PortalUtilities.getPortalRotation(level, uuid, isPrimary);

		AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, rotation.x, rotation.y);
		AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, rotation.x, rotation.y);
		AABB floorBox = PortalUtilities.getPortalFloorBox(portalPos, rotation.x, rotation.y).inflate(0d, 0.01d, 0d);

		Direction direction = PortalUtilities.getPortalDirection(level, uuid, isPrimary);

		Vec3 logicPos = teleportBox.getCenter();
		logicPos = logicPos.add(direction.getOpposite().getStepX() * entity.getBbWidth() / 2f,
				direction.getOpposite().getStepY() * entity.getBbHeight() / 1.25f,
				direction.getOpposite().getStepZ() * entity.getBbWidth() / 2f);

		Vec3 currentPos = entity.position().add(0, entity.getBbHeight() / 2f, 0);
		Vec3 offsetFromPortal = currentPos.subtract(logicPos);

		double dotProduct = offsetFromPortal.dot(new Vec3(direction.step()));

		if(collisionBox.intersects(floorBox) && isOpen)
			list.add(Shapes.create(floorBox));

		List<VoxelShape> readdVoxels = PortalUtilities.getPortalVoxels(level, uuid, isPrimary,
				portalPos, rotation.x, rotation.y);

		list.removeIf(shape -> !shape.isEmpty() && shape.bounds().intersects(portalBox)
									   && isOpen
									   && dotProduct > 0);

		for(VoxelShape voxel : readdVoxels)
		{
			if(voxel.isEmpty() || !isOpen)
				continue;

			for(AABB aabb : voxel.toAabbs())
			{
				if(aabb.intersects(collisionBox))
				{
					list.add(voxel);
				}
			}
		}

		cir.setReturnValue(list);
	}
}
