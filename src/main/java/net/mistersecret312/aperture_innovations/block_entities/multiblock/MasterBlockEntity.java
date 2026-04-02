package net.mistersecret312.aperture_innovations.block_entities.multiblock;

import mekanism.common.util.NBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.aperture_innovations.blocks.multiblock.DummyBlock;
import net.mistersecret312.aperture_innovations.init.BlockInit;

import java.util.ArrayList;
import java.util.List;

public class MasterBlockEntity extends BlockEntity
{
	public List<BlockPos> dummies = new ArrayList<>();
	public boolean beingRemoved = false;

	public MasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
	{
		super(type, pos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.saveAdditional(tag, registries);

		tag.put("dummies", NBTUtils.writeBlockPositions(dummies));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		NBTUtils.readBlockPositions(tag, "dummies", dummies);
	}

	public void addDummy(BlockPos pos)
	{
		this.dummies.add(pos);
	}

	public void clearDummies()
	{
		this.beingRemoved = true;
		if(level == null)
			return;

		for(BlockPos dummy : this.dummies)
		{
			BlockPos dummyPos = dummy.offset(this.getBlockPos());
			BlockState state = level.getBlockState(dummyPos);
			if(state.getBlock() instanceof DummyBlock)
				level.setBlock(dummyPos, Blocks.AIR.defaultBlockState(), 3);
		}
		this.dummies.clear();
		this.beingRemoved = false;
	}

	public boolean isBeingRemoved()
	{
		return beingRemoved;
	}

	public void setBeingRemoved(boolean beingRemoved)
	{
		this.beingRemoved = beingRemoved;
	}
}
