package net.mistersecret312.aperture_innovations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LargeButtonBlock extends BaseEntityBlock
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");

	public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");

	public static final MapCodec<LargeButtonBlock> CODEC = simpleCodec(LargeButtonBlock::new);

	public LargeButtonBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
									  .setValue(NORMAL, Direction.UP)
									  .setValue(FACING, Direction.NORTH)
									  .setValue(PRESSED, false));
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (!level.isClientSide() && !state.getValue(PRESSED)) {

		}
	}

	private void checkPressed(Entity entity, Level level, BlockPos pos, BlockState state) {
		boolean wasPressed = state.getValue(PRESSED);

		AABB detectionBox = new AABB(
				pos.getX() + 0.1, pos.getY(), pos.getZ() + 0.1,
				pos.getX() + 0.9, pos.getY() + 0.25, pos.getZ() + 0.9);

		boolean isNowPressed = !level.getEntities(null, detectionBox).isEmpty();

		if (isNowPressed != wasPressed)
		{
			level.setBlock(pos, state.setValue(PRESSED, isNowPressed), 3);

			if (isNowPressed)
			{
				level.playSound(null, pos, SoundInit.LARGE_BUTTON_DOWN.get(), SoundSource.BLOCKS, 0.5F, 1f);
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if(blockEntity instanceof LargeButtonBlockEntity pedestal)
					pedestal.triggerAnim("press", "down");
			}
			else
			{
				level.playSound(null, pos, SoundInit.LARGE_BUTTON_UP.get(), SoundSource.BLOCKS, 0.5F, 1f);
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if(blockEntity instanceof LargeButtonBlockEntity pedestal)
					pedestal.triggerAnim("press", "up");
			}
		}

		if (isNowPressed)
			level.scheduleTick(pos, this, 10);

	}



	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		return getSignal(state, level, pos, state.getValue(NORMAL).getOpposite());
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		if(state.getValue(PRESSED))
			return 15;

		return super.getSignal(state, level, pos, direction);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
	{
		return true;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		VoxelShape shape = Shapes.empty();
		shape = Shapes.join(shape, Shapes.box(-0.6875, 0, 0.3125, 0.6875, 0.25, 1.6875), BooleanOp.OR);

		return shape;
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(NORMAL).add(FACING).add(PRESSED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(state.getValue(PRESSED))
			return InteractionResult.PASS;

		if(blockEntity instanceof LargeButtonBlockEntity pedestalButton)
		{
			pedestalButton.triggerAnim("press", "down");
			level.setBlock(pos, state.setValue(PRESSED, true), 3);
			level.scheduleTick(pos, this, 15);
			if(!level.isClientSide())
				level.playSound(null, pos, SoundInit.LARGE_BUTTON_DOWN.get(), SoundSource.BLOCKS);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		if (state.getValue(PRESSED)) {

		}
		super.tick(state, level, pos, random);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(stack.is(ItemInit.COLORFUL_GEL))
		{
			ColorfulGelItem gelItem = (ColorfulGelItem) stack.getItem();
			int color = gelItem.getColor(stack);

			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(!(blockEntity instanceof LargeButtonBlockEntity pedestal))
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

			if(player.isCrouching())
				pedestal.buttonColor = color;
			else pedestal.color = color;

			pedestal.setChanged();
			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
																			BlockEntityType<T> blockEntityType)
	{
		return createTickerHelper(blockEntityType, BlockEntityInit.LARGE_BUTTON.get(), LargeButtonBlockEntity::tick);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker)
	{
		return typeB == typeA ? (BlockEntityTicker<A>)ticker : null;
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos)
	{
		return Shapes.empty();
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state)
	{
		return super.useShapeForLightOcclusion(state);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.LARGE_BUTTON.get().create(pos, state);
	}
}
