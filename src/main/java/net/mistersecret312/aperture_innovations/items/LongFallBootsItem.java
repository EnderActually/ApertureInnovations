package net.mistersecret312.aperture_innovations.items;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class LongFallBootsItem extends ArmorItem
{
	public LongFallBootsItem(Holder<ArmorMaterial> material, Type type, Properties properties)
	{
		super(material, type, properties);
	}

	@Override
	public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer)
	{
		return true;
	}
}
