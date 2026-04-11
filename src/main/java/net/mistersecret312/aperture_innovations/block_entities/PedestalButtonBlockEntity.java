package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.MasterBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.multiblock.OrientedMasterBlock;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.multitool.Color;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PedestalButtonBlockEntity extends MasterBlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation PRESS = RawAnimation.begin().thenPlay("press");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public Color idleColor = new Color(0, 0, 0);
	public Color hullColor = new Color(0, 0, 0);
	public Color buttonColor = new Color(0, 0, 0);

	public PedestalButtonBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.PEDESTAL_BUTTON.get(), pos, blockState);
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
		int[] hullArray = {hullColor.red(), hullColor.green(), hullColor.blue()};
		int[] idleArray = {idleColor.red(), idleColor.green(), idleColor.blue()};
		int[] buttonArray = {buttonColor.red(), buttonColor.green(), buttonColor.blue()};

		tag.putIntArray("hull_color", hullArray);
		tag.putIntArray("idle_color", idleArray);
		tag.putIntArray("button_color", buttonArray);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		int[] hullArray = tag.getIntArray("hull_color");
		int[] idleArray = tag.getIntArray("idle_color");
		int[] buttonArray = tag.getIntArray("button_color");

		this.hullColor = new Color(hullArray[0], hullArray[1], hullArray[2]);
		this.idleColor = new Color(idleArray[0], idleArray[1], idleArray[2]);
		this.buttonColor = new Color(buttonArray[0], buttonArray[1], buttonArray[2]);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<PedestalButtonBlockEntity> controller =
				new AnimationController<>(this, "press", 0, this::pressController);
		controller.triggerableAnim("press", PRESS);
		controllers.add(controller);
	}

	private PlayState pressController(AnimationState<PedestalButtonBlockEntity> state)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
