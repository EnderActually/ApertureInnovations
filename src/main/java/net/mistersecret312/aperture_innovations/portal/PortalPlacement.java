package net.mistersecret312.aperture_innovations.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PortalPlacement
{
	//TODO : Rewrite this placeholder bullshit
	public static class Result {
		public BlockPos bottomPos;
		public Direction rotation;
		public Direction facing;

		public Result(BlockPos pos, Direction rot, Direction face) {
			this.bottomPos = pos;
			this.rotation = rot;
			this.facing = face;
		}
	}

	public static Result getBestPlacement(Level level, BlockHitResult hit, Player player) {
		BlockPos hitPos = hit.getBlockPos();
		Direction face = hit.getDirection();

		Direction rotation;

		if (face.getAxis().isVertical()) {
			rotation = player.getDirection();
		} else {
			rotation = Direction.UP;
		}
		if (isValidSpot(level, hitPos, rotation, face)) {
			return new Result(hitPos, rotation, face);
		}

		BlockPos shiftedPos = hitPos.relative(rotation.getOpposite());
		if (isValidSpot(level, shiftedPos, rotation, face)) {
			return new Result(shiftedPos, rotation, face);
		}

		return null;
	}

	private static boolean isValidSpot(Level level, BlockPos bottomPos, Direction rotation, Direction face) {
		BlockPos topPos = bottomPos.relative(rotation);

		if(face.equals(Direction.DOWN))
		{
			topPos = bottomPos.relative(rotation.getOpposite());
		}

		BlockPos airBottom = bottomPos.relative(face);
		BlockPos airTop = topPos.relative(face);

		if(!isReplaceable(level, airBottom) || !isReplaceable(level, airTop) || level.getBlockState(topPos).isAir()) {
			return false;
		}

		return true;
	}

	private static boolean isReplaceable(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.canBeReplaced();
	}
}
