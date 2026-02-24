package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.blocks.LargeButtonBlock;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class LargeButtonBlockEntity extends BlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation PRESS = RawAnimation.begin().thenPlay("down");
	protected static final RawAnimation UNPRESS = RawAnimation.begin().thenPlay("up");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public int color = -1;
	public int buttonColor = -1;

	public LargeButtonBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.LARGE_BUTTON.get(), pos, blockState);
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
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		tag.putInt("color", this.color);
		tag.putInt("button_color", this.buttonColor);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.color = tag.getInt("color");
		this.buttonColor = tag.getInt("button_color");
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<LargeButtonBlockEntity> controller =
				new AnimationController<>(this, "press", 0, this::pressController);
		controller.triggerableAnim("down", PRESS);
		controller.triggerableAnim("up", UNPRESS);
		controllers.add(controller);
	}

	private PlayState pressController(AnimationState<LargeButtonBlockEntity> state)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	public static void tick(Level level, BlockPos pos, BlockState blockState,
							LargeButtonBlockEntity button)
	{
		Vec3 centerPos = Vec3.atLowerCornerOf(button.getBlockPos()).add(0.5f, 0f, 0.5f);

		AABB box = new AABB(centerPos.x, centerPos.y+0.25, centerPos.z,
				centerPos.x-1f, centerPos.y+0.5f, centerPos.z+1f);
		List<Entity> entities = level.getEntities((Entity) null, box, entity -> entity.getBoundingBox().getSize() > 0.25);

		boolean isPressed = blockState.getValue(LargeButtonBlock.PRESSED);
		if(level.isClientSide())
			return;
		if(isPressed)
		{
			if(entities.isEmpty())
			{
				level.setBlock(pos, blockState.setValue(LargeButtonBlock.PRESSED, false), 3);
				button.triggerAnim("press", "up");
			}
		}

		if(!isPressed)
		{
			if(!entities.isEmpty())
			{
				level.setBlock(pos, blockState.setValue(LargeButtonBlock.PRESSED, true), 3);
				button.triggerAnim("press", "down");
			}
		}
	}
}
