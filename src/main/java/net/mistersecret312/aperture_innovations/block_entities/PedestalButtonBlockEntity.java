package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PedestalButtonBlockEntity extends BlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation PRESS = RawAnimation.begin().thenPlay("press");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public PedestalButtonBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.PEDESTAL_BUTTON.get(), pos, blockState);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		controllers.add(new AnimationController<>(this, this::pressController));
	}

	private PlayState pressController(AnimationState<PedestalButtonBlockEntity> pedestalButtonBlockEntityAnimationState)
	{
		return PlayState.CONTINUE;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
