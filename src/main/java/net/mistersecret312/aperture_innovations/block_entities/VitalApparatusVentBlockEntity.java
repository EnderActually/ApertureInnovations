package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.MasterBlockEntity;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VitalApparatusVentBlockEntity extends MasterBlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open");
	protected static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	private boolean isOpen;

	public VitalApparatusVentBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.VITAL_APPARATUS_VENT.get(), pos, blockState);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();

		if(this.isOpen())
			this.triggerAnim("hatch", "open");
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		tag.putBoolean("open", this.isOpen);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.isOpen = tag.getBoolean("open");
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	public void toggleHatch(boolean state)
	{
		if(level == null)
			return;

		if(this.isOpen == state)
			return;

		this.isOpen = state;
		if(state)
		{
			level.playSound(null, getBlockPos(), SoundInit.VITAL_APPARATUS_VENT_OPEN.get(), SoundSource.BLOCKS, 0.33f, 1f);
			this.triggerAnim("hatch", "open");
		}
		else
		{
			level.playSound(null, getBlockPos(), SoundInit.VITAL_APPARATUS_VENT_CLOSE.get(), SoundSource.BLOCKS, 0.33f, 1f);
			this.triggerAnim("hatch", "close");
		}

		this.setChanged();
	}

	public void toggleHatch()
	{
		if(level == null)
			return;

		this.isOpen = !isOpen;
		this.toggleHatch(this.isOpen);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<VitalApparatusVentBlockEntity> controller =
				new AnimationController<>(this, "hatch", 0, this::hatchController);
		controller.triggerableAnim("open", OPEN);
		controller.triggerableAnim("close", CLOSE);
		controllers.add(controller);
	}

	private PlayState hatchController(AnimationState<VitalApparatusVentBlockEntity> animationState)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return this.saveWithoutMetadata(registries);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if(level != null)
			level.markAndNotifyBlock(getBlockPos(), level.getChunkAt(getBlockPos()), getBlockState(), getBlockState(), 3, 512);
	}
}
