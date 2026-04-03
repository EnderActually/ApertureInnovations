package net.mistersecret312.aperture_innovations.blocks.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public abstract class OrientedMasterBlock extends MasterBlock
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");

	public OrientedMasterBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(NORMAL, Direction.UP).setValue(UPDATE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING).add(NORMAL);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation)
	{
		state = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
		state = state.setValue(UPDATE, true);
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
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
									 LevelAccessor level, BlockPos pos, BlockPos neighborPos)
	{
		if(state.getValue(UPDATE))
		{
			level.setBlock(pos, state.setValue(UPDATE, false), 3);
			if(level instanceof Level realLevel)
				state.getBlock().setPlacedBy(realLevel, pos, state, null, ItemStack.EMPTY);
		}

		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public AABB getMultiblockVolume(Level level, BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);

		AABB original = this.getDefaultMultiblockVolume();
		if(state.hasProperty(FACING) && state.hasProperty(NORMAL))
		{
			Direction normal = state.getValue(NORMAL);
			Direction facing = state.getValue(FACING);

			if(normal.equals(Direction.UP))
			{
				if(facing.equals(Direction.NORTH))
					return original.move(0, 0, -original.getZsize())
								   .move(-(int) (original.getXsize() / 2), 0, (int) (original.getZsize() / 2));
				if(facing.equals(Direction.WEST))
					return original.move(-original.getXsize(), 0, -original.getZsize())
								   .move((int) (original.getXsize() / 2), 0, (int) (original.getZsize() / 2));
				if(facing.equals(Direction.SOUTH))
					return original.move(-original.getXsize(), 0, 0)
								   .move((int) (original.getXsize() / 2), 0, -(int) (original.getZsize() / 2));
				return original.move(-(int) (original.getXsize() / 2), 0, -(int) (original.getZsize() / 2));
			}
			if(normal.equals(Direction.DOWN))
				return original.move(-(int) (original.getXsize()/2), -original.getYsize(), -(int) (original.getZsize()/2));

			if(normal.getAxis().equals(Direction.Axis.Z))
				original = new AABB(original.minX, original.minZ, original.minY, original.maxX, original.maxZ, original.maxY);

			if(normal.getAxis().equals(Direction.Axis.X))
				original = new AABB(original.minY, original.minX, original.minZ, original.maxY, original.maxX, original.maxZ);


			if(normal.equals(Direction.SOUTH))
				return original.move(-(int) (original.getXsize()/2), -(int) (original.getYsize()/2), 0);
			if(normal.equals(Direction.EAST))
				return original.move(0, -(int) (original.getYsize()/2), -(int) (original.getZsize()/2));

			if(normal.equals(Direction.NORTH))
				return original.move(-(int) (original.getXsize()/2), -(int) (original.getYsize()/2), -original.getZsize());
			if(normal.equals(Direction.WEST))
				return original.move(-original.getXsize(), -(int) (original.getYsize()/2), -(int) (original.getZsize()/2));
		}

		return original;
	}

}
