package net.mistersecret312.aperture_innovations.multitool;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemConfiguration extends IHaveConfiguration
{
	List<ConfigurationProperty<?>> getConfigurationProperties(ItemStack stack);

	@Override
	default List<ConfigurationProperty<?>> getConfigurationProperties()
	{
		return List.of();
	}
}
