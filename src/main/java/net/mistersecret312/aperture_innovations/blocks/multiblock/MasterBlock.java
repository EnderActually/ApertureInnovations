package net.mistersecret312.aperture_innovations.blocks.multiblock;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.DummyBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.MasterBlockEntity;
import net.mistersecret312.aperture_innovations.init.BlockInit;
import org.jetbrains.annotations.Nullable;

public abstract class MasterBlock extends BaseEntityBlock
{
	public MasterBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
	{
		removeDummyBlocks(level, pos);
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
							  @Nullable BlockEntity blockEntity, ItemStack tool)
	{
		removeDummyBlocks(level, pos);
		super.playerDestroy(level, player, pos, state, blockEntity, tool);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, state, placer, stack);
		placeDummyBlocks(level, placer, pos);
	}

	/**
	 *
	 * @return AABB that defines the volume this multiblock will take up.
	 * OrientedMasterblock uses this to rotate and has a separate method for which AABB it rotates.
	 */
	public AABB getMultiblockVolume(Level level, BlockPos pos)
	{
		return new AABB(BlockPos.ZERO);
	}

	public boolean canBePlaced(Level level, BlockPos placePos)
	{
		return BlockPos.betweenClosedStream(getMultiblockVolume(level, placePos).move(placePos)).allMatch(pos ->
			{
				if(pos.equals(placePos))
					return true;

				BlockState state = level.getBlockState(pos);
				return state.isAir() || state.canBeReplaced();
			});
	}

	public void removeDummyBlocks(Level level, BlockPos pos)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof MasterBlockEntity master)
			master.clearDummies();
	}

	public void placeDummyBlocks(Level level, @Nullable LivingEntity living, BlockPos placePos)
	{
		BlockEntity masterBlockEntity = level.getBlockEntity(placePos);
		if(!(masterBlockEntity instanceof MasterBlockEntity master))
			return;

		master.clearDummies();

		if(!canBePlaced(level, placePos))
		{
			if(living instanceof Player player)
			{
				player.displayClientMessage(
						Component.translatable("message.aperture_innovations.multiblock.not_enough_room")
								 .withStyle(ChatFormatting.DARK_RED), true);
			}

			return;
		}

		AABB volume = this.getMultiblockVolume(level, placePos).move(placePos);

		BlockPos.betweenClosedStream(volume).forEach(pos ->
			{
				if(!pos.equals(placePos))
				{
					level.setBlock(pos, BlockInit.DUMMY_BLOCK.get().defaultBlockState(), 3);
					BlockEntity blockEntity = level.getBlockEntity(pos);
					if(blockEntity instanceof DummyBlockEntity dummy)
					{
						dummy.setMasterPos(placePos.subtract(pos));
						master.addDummy(pos.subtract(placePos));
					}
				}
			});
	}


	//TODO Make sure the math for getting the voxelshape for a large multiblock
	//TODO gets mathed out correctly and center the shapes Joining
	//TODO At a later point, make it only calculate this once and then cache it in the block entity;

	public VoxelShape getFullShape(Level level, BlockPos pos)
	{
		return Shapes.create(getMultiblockVolume(level, pos).inflate(1));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		if(true)
			return Shapes.block();
		if(!(level instanceof Level realLevel))
			return Shapes.empty();

		Vec3 centerPos = getFullShape(realLevel, pos).bounds().move(pos).getCenter();

		VoxelShape fullShape = getFullShape(realLevel, pos).move(centerPos.x, centerPos.y, centerPos.z);
		VoxelShape blockShape = Shapes.block().move(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
		return Shapes.join(fullShape, blockShape, BooleanOp.AND).move(-pos.getX()-0.5, -pos.getY()-0.5, -pos.getZ()-0.5);
	}
}
