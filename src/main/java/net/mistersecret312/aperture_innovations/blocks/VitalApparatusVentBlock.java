package net.mistersecret312.aperture_innovations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.block_entities.VitalApparatusVentBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.multiblock.MasterBlock;
import net.mistersecret312.aperture_innovations.blocks.multiblock.OrientedMasterBlock;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

public class VitalApparatusVentBlock extends OrientedMasterBlock
{
	public static final MapCodec<VitalApparatusVentBlock> CODEC = simpleCodec(VitalApparatusVentBlock::new);

	public VitalApparatusVentBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(!level.isClientSide() && blockEntity instanceof VitalApparatusVentBlockEntity vent)
			vent.toggleHatch();

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	public AABB getDefaultMultiblockVolume()
	{
		return new AABB(0, 0, 0, 2, 2, 2);
	}

	@Override
	public VoxelShape getFullShape(Level level, BlockPos pos)
	{
		if(level.getBlockEntity(pos) instanceof VitalApparatusVentBlockEntity vent)
		{
			if(vent.isOpen())
			{
				VoxelShape shape = Shapes.empty();
				shape = Shapes.join(shape, Shapes.box(-0.5, -0.5, -0.5, 1.5, 2.125, 0), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(-0.5, -0.5, 1, 1.5, 2.125, 1.5), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(-0.5, -0.5, 0, 0, 2.125, 1), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(1, -0.5, 0, 1.5, 2.125, 1), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0, 1.5, 0, 1, 2.125, 1), BooleanOp.OR);

				return shape.move(0, -1, 0);
			}
			else
			{
				VoxelShape shape = Shapes.empty();
				shape = Shapes.join(shape, Shapes.box(-0.5, -0.5, -0.5, 1.5, 2.125, 0), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(-0.5, -0.5, 1, 1.5, 2.125, 1.5), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(-0.5, -0.5, 0, 0, 2.125, 1), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(1, -0.5, 0, 1.5, 2.125, 1), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0, 1.5, 0, 1, 2.125, 1), BooleanOp.OR);
				shape = Shapes.join(shape, Shapes.box(0, -0.5, 0, 1, -0.3125, 1), BooleanOp.OR);

				return shape.move(0, -1, 0);
			}
		}

		return Shapes.box(-0.5, -0.5, -0.5, 1.5, 2, 1.5).move(0, -1, 0);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.VITAL_APPARATUS_VENT.get().create(pos, state);
	}
}
