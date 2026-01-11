package net.mistersecret312.aperture_innovations.items;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.mistersecret312.aperture_innovations.client.renderer.LongFallBootsRenderProperties;

import java.util.function.Consumer;

public class LongFallBootsItem extends ArmorItem
{
	public LongFallBootsItem(ArmorMaterial material, Type type, Properties properties)
	{
		super(material, type, properties);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(LongFallBootsRenderProperties.INSTANCE);
	}
}
