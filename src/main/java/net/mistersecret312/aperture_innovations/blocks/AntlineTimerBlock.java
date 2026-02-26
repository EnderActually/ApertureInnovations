package net.mistersecret312.aperture_innovations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.mistersecret312.aperture_innovations.block_entities.AntlineOutputBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.AntlineTimerBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import org.jetbrains.annotations.Nullable;

import static net.mistersecret312.aperture_innovations.blocks.AntlineBlock.*;

public class AntlineTimerBlock extends BaseEntityBlock
{
	public static final MapCodec<AntlineTimerBlock> CODEC = simpleCodec(AntlineTimerBlock::new);

	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public AntlineTimerBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
									  .setValue(NORMAL, Direction.NORTH)
									  .setValue(FACING, Direction.UP)
									  .setValue(ACTIVE, false));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(stack.is(ItemInit.COLORFUL_GEL))
		{
			ColorfulGelItem gel = (ColorfulGelItem) stack.getItem();
			int color = gel.getColor(stack);

			AntlineOutputBlockEntity antline = (AntlineOutputBlockEntity) level.getBlockEntity(pos);
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
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		boolean active = state.getValue(ACTIVE);
		if(!active)
			level.scheduleTick(pos, this, 1, TickPriority.VERY_HIGH);
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
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		Direction normal = state.getValue(NORMAL);

		return switch (normal)
		{
			case UP -> SHAPE_UP;
			case DOWN -> SHAPE_DOWN;
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case EAST -> SHAPE_EAST;
			case WEST -> SHAPE_WEST;
		};
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = this.defaultBlockState();
		Direction direction = context.getClickedFace().getAxis().isHorizontal() ?
									  context.getNearestLookingVerticalDirection()
									  : context.getHorizontalDirection();

		state = state.setValue(NORMAL, context.getClickedFace());
		state = state.setValue(FACING, direction);
		return state;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof AntlineTimerBlockEntity output)
		{
			if(state.getValue(ACTIVE) && direction.equals(state.getValue(NORMAL)))
				return output.signal;
		}
		return 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(NORMAL, FACING, ACTIVE);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, BlockEntityInit.TIMER.get(), AntlineTimerBlockEntity::tick);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker) {
		return typeB == typeA ? (BlockEntityTicker<A>)ticker : null;
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
		return BlockEntityInit.TIMER.get().create(pos, state);
	}
}
