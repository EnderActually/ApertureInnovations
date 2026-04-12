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
import net.mistersecret312.aperture_innovations.init.MultiToolConfigTypeInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.multitool.Color;
import net.mistersecret312.aperture_innovations.multitool.ConfigurationProperty;
import net.mistersecret312.aperture_innovations.multitool.IHaveConfiguration;
import net.mistersecret312.aperture_innovations.multitool.InteractionType;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class PedestalButtonBlockEntity extends MasterBlockEntity implements GeoBlockEntity, IHaveConfiguration
{
	protected static final RawAnimation PRESS = RawAnimation.begin().thenPlay("press");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public Color linesColor = new Color(0, 0, 0);
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
		int[] lineArray = {linesColor.red(), linesColor.green(), linesColor.blue()};
		int[] buttonArray = {buttonColor.red(), buttonColor.green(), buttonColor.blue()};

		tag.putIntArray("hull_color", hullArray);
		tag.putIntArray("lines_color", lineArray);
		tag.putIntArray("button_color", buttonArray);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		int[] hullArray = tag.getIntArray("hull_color");
		int[] lineArray = tag.getIntArray("lines_color");
		int[] buttonArray = tag.getIntArray("button_color");

		this.hullColor = new Color(hullArray[0], hullArray[1], hullArray[2]);
		this.linesColor = new Color(lineArray[0], lineArray[1], lineArray[2]);
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

	public Color getHullColor()
	{
		return hullColor;
	}

	public void setHullColor(Color hullColor)
	{
		this.hullColor = hullColor;
	}

	public Color getButtonColor()
	{
		return buttonColor;
	}

	public void setButtonColor(Color buttonColor)
	{
		this.buttonColor = buttonColor;
	}

	public Color getLinesColor()
	{
		return linesColor;
	}

	public void setLinesColor(Color idleColor)
	{
		this.linesColor = idleColor;
	}

	@Override
	public List<ConfigurationProperty<?>> getConfigurationProperties()
	{
		List<ConfigurationProperty<?>> list = new ArrayList<>();
		list.add(new ConfigurationProperty<>("hull_color", "color",
				"multi_tool.aperture_innovations.pedestal_button.hull_color",
				MultiToolConfigTypeInit.COLOR.get(),
				new InteractionType.RGBColorPicker(),
				this::setHullColor, this::getHullColor));
		list.add(new ConfigurationProperty<>("button_color", "color",
				"multi_tool.aperture_innovations.pedestal_button.button_color",
				MultiToolConfigTypeInit.COLOR.get(),
				new InteractionType.RGBColorPicker(),
				this::setButtonColor, this::getButtonColor));
		list.add(new ConfigurationProperty<>("lines_color", "color",
				"multi_tool.aperture_innovations.pedestal_button.lines_color",
				MultiToolConfigTypeInit.COLOR.get(),
				new InteractionType.RGBColorPicker(),
				this::setLinesColor, this::getLinesColor));

		for(ConfigurationProperty<?> property : list)
		{
			if(property.getName().equals("hull_color"))
				property.setUnsafe(hullColor);
			if(property.getName().equals("button_color"))
				property.setUnsafe(buttonColor);
			if(property.getName().equals("lines_color"))
				property.setUnsafe(linesColor);
		}

		return list;
	}
}
