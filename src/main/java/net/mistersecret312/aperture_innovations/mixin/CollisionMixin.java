package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
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

		Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
		Direction portalDirection = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
		boolean isOnWall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);

		AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, portalDirection, isOnWall);

		list.removeIf(shape -> shape.bounds().intersects(portalBox));

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

		Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
		Direction portalDirection = PortalUtilities.getPortalDirection(level, uuid, isPrimary);
		boolean isOnWall = PortalUtilities.isPortalOnWall(level, uuid, isPrimary);

		AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, portalDirection, isOnWall);

		//TODO : Add a void floor for on-wall portals so players can peek throug the portal without needing a floor
		while(blockcollisions.hasNext()) {
			VoxelShape shape = blockcollisions.next();
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
