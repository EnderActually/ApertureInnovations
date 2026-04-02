package net.mistersecret312.aperture_innovations.blocks.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class OrientedMasterBlock extends MasterBlock
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");

	public OrientedMasterBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(NORMAL, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING).add(NORMAL);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation)
	{
		state = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
		return state.setValue(NORMAL, rotation.rotate(state.getValue(NORMAL)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror)
	{
		return state.rotate(mirror.getRotation(state.getValue(NORMAL)));
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = this.defaultBlockState();

		Direction direction = context.getClickedFace().getAxis().isHorizontal() ?
									  context.getNearestLookingVerticalDirection().getOpposite()
									  : context.getHorizontalDirection();

		state = state.setValue(NORMAL, context.getClickedFace());
		state = state.setValue(FACING, direction);

		return state;
	}

	@Override
	public AABB getMultiblockVolume(Level level, BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);

		AABB original = this.getDefaultMultiblockVolume();

		Vec3 max = original.getMaxPosition();
		Vec3 min = original.getMinPosition();

		if(state.hasProperty(FACING) && state.hasProperty(NORMAL))
		{
			Direction normal = state.getValue(NORMAL);
			Direction facing = state.getValue(FACING);
			if(level.isClientSide())
				level.addParticle(ParticleTypes.ANGRY_VILLAGER, original.getCenter().x+pos.getX(), original.getCenter().y+pos.getY(), original.getCenter().z+pos.getZ(), 0, 0, 0);

			//TODO - rewire this to work good with rotatable multiblocks
			if(normal.equals(Direction.UP))
			{
				if(facing.equals(Direction.NORTH)) return original.move(0, 0, -original.getZsize());
				if(facing.equals(Direction.WEST)) return original.move(-original.getXsize(), 0, -original.getZsize());
				if(facing.equals(Direction.SOUTH)) return original.move(-original.getXsize(), 0, 0);
			}
			if(normal.equals(Direction.SOUTH))
			{
				return original.move(-(int) (original.getYsize()/2), -(int) (original.getXsize()/2), 0);
			}
			if(normal.equals(Direction.NORTH))
			{
				return original.move(-(int) (original.getXsize()/2), -(int) (original.getZsize()/2), -original.getYsize());
			}
			if(normal.equals(Direction.EAST))
			{
				return original.move(-(int) (original.getYsize()),0, (int) (original.getZsize()));
			}
		}

		return original;
	}

	public AABB getDefaultMultiblockVolume()
	{
		return new AABB(BlockPos.ZERO);
	}

	public Vec3 rotateVec(Vec3 vec, Direction facing, Direction normal)
	{
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		vec = switch (facing) {
			case EAST  -> new Vec3(x, y, z);      // 0 degrees
			case SOUTH -> new Vec3(-z, y, x);     // 90 degrees Clockwise
			case WEST  -> new Vec3(-x, y, -z);    // 180 degrees
			case NORTH -> new Vec3(z, y, -x);     // 90 degrees Counter-Clockwise
			default    -> new Vec3(x, y, z);      // Fallback for UP/DOWN
		};

		return vec;
	}
}
