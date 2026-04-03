package net.mistersecret312.aperture_innovations.blocks.multiblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.DummyBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.MasterBlockEntity;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class DummyBlock extends BaseEntityBlock
{
	public static final MapCodec<DummyBlock> CODEC = simpleCodec(DummyBlock::new);

	public DummyBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
	{
		if(state.getBlock() != newState.getBlock())
		{
			MasterBlockEntity master = getMaster(level, pos);
			if(master != null && !master.beingRemoved)
				level.setBlock(master.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);

			super.onRemove(state, level, pos, newState, movedByPiston);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
											   Player player, BlockHitResult hitResult)
	{
		MasterBlockEntity master = getMaster(level, pos);
		if(master == null)
			return super.useWithoutItem(state, level, pos, player, hitResult);

		MasterBlock masterBlock = (MasterBlock) master.getBlockState().getBlock();
		return masterBlock.useItemLess(master.getBlockState(), level, master.getBlockPos(), player, hitResult);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		MasterBlockEntity master = getMaster(level, pos);
		if(master == null)
			return super.useItemOn(stack, state, level, pos, player, hand, hitResult);

		MasterBlock masterBlock = (MasterBlock) master.getBlockState().getBlock();
		return masterBlock.useWithItemOn(stack, master.getBlockState(), level, master.getBlockPos(), player, hand, hitResult);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
	{
		if(!(level instanceof Level realLevel))
			return super.canConnectRedstone(state, level, pos, direction);
		MasterBlockEntity master = getMaster(realLevel, pos);
		if(master == null)
			return super.canConnectRedstone(state, level, pos, direction);

		return master.getBlockState().canRedstoneConnectTo(level, master.getBlockPos(), direction);
	}

	public BlockPos getMasterPos(Level level, BlockPos pos)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(!(blockEntity instanceof DummyBlockEntity dummy))
			return null;

		return dummy.getMasterPos();
	}

	public MasterBlockEntity getMaster(Level level, BlockPos pos)
	{
		BlockEntity blockEntity = level.getBlockEntity(getMasterPos(level, pos));
		if(blockEntity instanceof MasterBlockEntity master)
			return master;

		return null;
	}

	public MasterBlock getMasterBlock(Level level, BlockPos pos)
	{
		MasterBlockEntity master = getMaster(level, pos);
		if(master == null)
			return null;

		if(master.getBlockState().getBlock() instanceof MasterBlock masterBlock)
			return masterBlock;

		return null;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.INVISIBLE;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.DUMMY.get().create(pos, state);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation)
	{
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror)
	{
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		if(!(level instanceof Level realLevel))
			return Shapes.empty();

		MasterBlock masterBlock = getMasterBlock(realLevel, pos);
		if(masterBlock == null)
 			return Shapes.block();

		BlockPos masterPos = getMasterPos(realLevel, pos);

		VoxelShape actualVolume = masterBlock.getFullShape(realLevel, masterPos);
		Vec3 offset = Vec3.ZERO;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof DummyBlockEntity dummy)
			offset = Vec3.atLowerCornerOf(dummy.getOffset());

		actualVolume = actualVolume.move(offset.x, offset.y, offset.z);

		return Shapes.join(actualVolume, Shapes.block(), BooleanOp.AND);
	}
}
