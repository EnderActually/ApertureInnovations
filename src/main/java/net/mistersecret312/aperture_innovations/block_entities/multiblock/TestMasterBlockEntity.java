package net.mistersecret312.aperture_innovations.block_entities.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;

public class TestMasterBlockEntity extends MasterBlockEntity
{

	public TestMasterBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.MASTER_TEST.get(), pos, blockState);
	}

	
}
