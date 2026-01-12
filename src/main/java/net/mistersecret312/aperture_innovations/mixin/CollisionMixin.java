package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.portal.PortalUtilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(CollisionGetter.class)
public interface CollisionMixin
{
	/**
	 * @author mistersecret312
	 * @reason Portal need removing of collisions in the blocks behind them for smooth teleportation experience
	 */
	@Overwrite
	default Iterable<VoxelShape> getBlockCollisions(Entity entity, AABB entityBox)
	{
		List<VoxelShape> list = new ArrayList<>();

		CollisionGetter getter = ((CollisionGetter) (Object) this);

		Iterable<VoxelShape> iterable =
				() -> new BlockCollisions<>(getter, entity, entityBox, false,
						(pos, shape) -> shape);

		for(VoxelShape voxelShape : iterable) list.add(voxelShape);

		if(entity == null)
			return list;

		Level level = entity.level();
		if(level == null)
			return list;

		Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(entity);

		UUID uuid = portal.getFirst();
		boolean isPrimary = portal.getSecond();
		if(uuid == null)
			return list;

		boolean isOpen = PortalUtilities.isPortalOpen(level, uuid);

		Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
		Direction portalDirection = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
		boolean isOnWall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);
		boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(level, uuid, isPrimary);

		AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, portalDirection, isOnWall, isOnCeiling);
		AABB floorBox = PortalUtilities.getPortalFloorBox(portalPos, portalDirection, isOnWall).inflate(0d, 0.01d, 0d);

		list.removeIf(shape -> shape.bounds().intersects(portalBox) && isOpen);
		if(entityBox.intersects(floorBox) && isOpen)
			list.add(Shapes.create(floorBox));

		return list;
	}

	/**
	 * @author mistersecret312
	 * @reason Required to actually be able to enter the portal without getting pushed out.
	 */
	@Overwrite
	default boolean collidesWithSuffocatingBlock(Entity entity, AABB entityBox)
	{
		CollisionGetter getter = ((CollisionGetter) (Object) this);

		BlockCollisions<VoxelShape> blockcollisions =
				new BlockCollisions<>(getter, entity, entityBox, true, (pos, shape) -> {
			return shape;
		});

		if(entity == null)
			return original(blockcollisions);

		Level level = entity.level();
		if(level == null)
			return original(blockcollisions);

		Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(entity);

		UUID uuid = portal.getFirst();
		boolean isPrimary = portal.getSecond();
		if(uuid == null)
			return original(blockcollisions);

		boolean isOpen = PortalUtilities.isPortalOpen(level, uuid);

		Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
		Direction portalDirection = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
		boolean isOnWall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);
		boolean isOnCeiling = PortalUtilities.isPortalOnCeiling(level, uuid, isPrimary);

		AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, portalDirection, isOnWall, isOnCeiling);
		AABB floorBox = PortalUtilities.getPortalFloorBox(portalPos, portalDirection, isOnWall).inflate(0d, 0.01d, 0d);

		if(!isOpen)
		{
			portalBox = new AABB(0D,0D,0D,0D,0D,0D);
			floorBox = new AABB(0D,0D,0D,0D,0D,0D);
		}

		List<VoxelShape> list = new ArrayList<>();
		blockcollisions.forEachRemaining(list::add);
		if(entityBox.intersects(floorBox))
			list.add(Shapes.create(floorBox));

		Iterator<VoxelShape> iterator = list.iterator();
		while(iterator.hasNext()) {
			VoxelShape shape = iterator.next();
			if(!shape.isEmpty() && shape.bounds() == floorBox)
				return true;

			if (!shape.isEmpty() && !shape.bounds().intersects(portalBox)) {
				return true;
			}
		}
		return false;
	}

	@Unique
	default boolean original(Iterator<VoxelShape> blockcollisions)
	{
		while(blockcollisions.hasNext()) {
			VoxelShape shape = blockcollisions.next();
			if (!shape.isEmpty()) {
				return true;
			}
		}

		return false;
	}
}
