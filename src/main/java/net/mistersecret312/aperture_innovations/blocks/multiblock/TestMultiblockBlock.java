package net.mistersecret312.aperture_innovations.blocks.multiblock;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.aperture_innovations.blocks.AntlineBlock;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class TestMultiblockBlock extends MasterBlock
{
	public static final MapCodec<AntlineBlock> CODEC = simpleCodec(AntlineBlock::new);

	public TestMultiblockBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.MASTER_TEST.get().create(pos, state);
	}

	@Override
	public AABB getMultiblockVolume(Level level, BlockPos pos)
	{
		return new AABB(0, 0, 0, 2, 2,2);
	}
}
