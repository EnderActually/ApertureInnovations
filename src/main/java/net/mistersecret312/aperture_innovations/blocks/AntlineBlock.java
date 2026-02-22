package net.mistersecret312.aperture_innovations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.aperture_innovations.block_entities.AntlineBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.enums.ConnectionState;
import net.mistersecret312.aperture_innovations.data.AntlineData;
import net.mistersecret312.aperture_innovations.data.antline.Antline;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.neo_events.AntlineActivateEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class AntlineBlock extends BaseEntityBlock
{
	public static final MapCodec<AntlineBlock> CODEC = simpleCodec(AntlineBlock::new);

	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");

	public AntlineBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(NORMAL, Direction.UP));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, state, placer, stack);
		updateAntline(level, pos);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
								   BlockPos neighborPos, boolean movedByPiston)
	{
		super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
		updateAntline(level, pos);

		toggleAntline(level, pos);
	}

	@Override
	protected void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int flags,
												 int recursionLeft)
	{
		super.updateIndirectNeighbourShapes(state, level, pos, flags, recursionLeft);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity != null && blockEntity.getLevel() != null && blockEntity instanceof AntlineBlockEntity)
			updateAntline(blockEntity.getLevel(), pos);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
	{
		if(!state.getBlock().equals(newState.getBlock()))
		{
			AntlineData.get(level).removeAntline(level, pos);
			this.updateAntline(level, pos);
			super.onRemove(state, level, pos, newState, movedByPiston);
		}
		else
		{
			this.updateAntline(level, pos);
			super.onRemove(state, level, pos, newState, movedByPiston);
		}
	}

	public void updateAntline(Level level, BlockPos pos)
	{
		if(!level.isClientSide())
		{
			AntlineData.get(level).update(level, pos);
			if(level.getBlockEntity(pos) instanceof AntlineBlockEntity antline)
			{
				antline.updateConnections();
				antline.trimConnections();
			}
		}
	}

	public void toggleAntline(Level level, BlockPos pos)
	{
		if(!level.isClientSide())
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof AntlineBlockEntity antline)
			{
				Antline link = antline.getAntline();
				if(link == null)
					return;

				boolean shouldActive = false;
				int signal = 0;
				for(BlockPos blockPos : link.links)
				{
					if(level.getBlockEntity(blockPos) instanceof AntlineBlockEntity linkBE)
						if(linkBE.outputting)
							continue;

					if(!shouldActive)
					{
						shouldActive = level.getBestNeighborSignal(blockPos) != 0;
						signal = level.getBestNeighborSignal(blockPos);
					}
				}

				for(BlockPos blockPos : link.antlineBlocks)
				{
					BlockEntity outputEntity = level.getBlockEntity(blockPos);
					if(outputEntity instanceof AntlineBlockEntity outputAntline)
					{
						outputAntline.active = shouldActive;
						outputAntline.signal = signal;

						outputAntline.setChanged();
						for(Direction connectedSide : outputAntline.getConnectedSides())
						{
							ConnectionState state = outputAntline.getState(connectedSide);
							if(state.equals(ConnectionState.LINK))
								NeoForge.EVENT_BUS.post(new AntlineActivateEvent(level, blockPos, blockPos.relative(connectedSide), signal));
						}
					}
				}
			}
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(stack.is(ItemInit.COLORFUL_GEL))
		{
			ColorfulGelItem gel = (ColorfulGelItem) stack.getItem();
			int color = gel.getColor(stack);

			AntlineBlockEntity antline = (AntlineBlockEntity) level.getBlockEntity(pos);
			if(antline == null)
				return ItemInteractionResult.FAIL;

			if(player.isCrouching())
				antline.activeColor = color;
			else antline.color = color;

			return ItemInteractionResult.sidedSuccess(level.isClientSide());
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
	{
		return true;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
									 LevelAccessor level, BlockPos pos, BlockPos neighborPos)
	{
		if(!canSurvive(state, level, pos))
		{
			return Blocks.AIR.defaultBlockState();
		}

		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos blockpos = pos.relative(state.getValue(NORMAL).getOpposite());
		BlockState blockstate = level.getBlockState(blockpos);
		return this.canSurviveOn(level, blockpos, blockstate, state.getValue(NORMAL));
	}

	private boolean canSurviveOn(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
		return state.isFaceSturdy(level, pos, direction);
	}
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = this.defaultBlockState();
		state = state.setValue(NORMAL, context.getClickedFace());
		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(NORMAL);
		super.createBlockStateDefinition(builder);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.ANTLINE.get().create(pos, state);
	}
}
