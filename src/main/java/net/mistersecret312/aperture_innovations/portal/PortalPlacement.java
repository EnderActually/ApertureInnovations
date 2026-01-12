package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PortalPlacement
{
	public static class Result
	{
		public BlockPos bottomPos;
		public Direction rotation;
		public Direction facing;

		public Result(BlockPos pos, Direction rot, Direction face)
		{
			this.bottomPos = pos;
			this.rotation = rot;
			this.facing = face;
		}
	}

	public static Result getBestPlacement(Level level, BlockHitResult hit, Player player, UUID uuid, boolean isPrimary)
	{
		BlockPos hitPos = hit.getBlockPos();
		Direction face = hit.getDirection();

		Direction rotation;
		if (face.getAxis().isVertical()) {
			rotation = player.getDirection();
		} else {
			rotation = Direction.UP;
		}
		BlockPos tryBottomPos;

		if(!face.getAxis().isVertical())
			tryBottomPos = hitPos.relative(rotation.getOpposite());
		else
		{
			if(face.getAxisDirection().equals(Direction.AxisDirection.POSITIVE))
				tryBottomPos = hitPos.relative(rotation.getOpposite());
			else tryBottomPos = hitPos.relative(rotation);
		}
		if (isValidSpot(level, tryBottomPos, rotation, face, uuid, isPrimary))
		{
			return new Result(tryBottomPos, rotation, face);
		}

		tryBottomPos = hitPos;
		if(face.getAxis().isVertical())
		{
			if(face.getAxisDirection().equals(Direction.AxisDirection.POSITIVE))
				tryBottomPos = hitPos;
		}
		if (isValidSpot(level, tryBottomPos, rotation, face, uuid, isPrimary)) {
			return new Result(tryBottomPos, rotation, face);
		}

		return null;
	}

	private static boolean isValidSpot(Level level, BlockPos bottomPos, Direction rotation, Direction face,
									   UUID uuid, boolean isPrimary) {
		BlockPos topPos;
		if(face.getAxis().isVertical())
		{
			if(face.getAxisDirection().equals(Direction.AxisDirection.POSITIVE))
				topPos = bottomPos.relative(rotation);
			else topPos = bottomPos.relative(rotation.getOpposite());
		}
		else
		{
			topPos = bottomPos.relative(rotation);
		}

		BlockPos portalBottom = bottomPos.relative(face);
		BlockPos portalTop = topPos.relative(face);

		if (level.getBlockState(bottomPos).isAir() || level.getBlockState(topPos).isAir())
			return false;

		if (!isReplaceable(level, portalBottom) || !isReplaceable(level, portalTop))
			return false;

		if (!level.getBlockState(bottomPos).isFaceSturdy(level, bottomPos, face)
					|| !level.getBlockState(topPos).isFaceSturdy(level, topPos, face))
			return false;

		if(hasExistingPortal(level, portalBottom, uuid, isPrimary) || hasExistingPortal(level, portalTop, uuid, isPrimary))
			return false;


		return true;
	}

	private static boolean isReplaceable(Level level, BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.canBeReplaced();
	}

	private static boolean hasExistingPortal(Level level, BlockPos pos,
											 UUID uuid, boolean isPrimary)
	{
		AtomicBoolean found = new AtomicBoolean(false);
		PortalUtilities.getPortalLinks(level).forEach((entryID, portalLink) ->
			{
				for(int i = 0; i < 2; i++)
				{
					boolean entryPrimarity = i == 0;
					if(uuid.equals(entryID) && entryPrimarity == isPrimary)
						continue;

					Vec3 portalPos = PortalUtilities.getPortalPos(level, entryID, entryPrimarity);
					if(portalPos == null)
						continue;

					Direction portalDirection = PortalUtilities.getPortalDirection(level, entryID, entryPrimarity);
					boolean onWall = PortalUtilities.isPortalOnWall(level, entryID, entryPrimarity);
					boolean onCeiling = PortalUtilities.isPortalOnCeiling(level, entryID, entryPrimarity);

					AABB portalBox = PortalUtilities.getPortalTeleportBox(portalPos, portalDirection, onWall, onCeiling);
					if(portalBox.intersects(new AABB(pos)))
						found.set(true);
				}
			});
		return found.get();
	}
}
