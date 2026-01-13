package net.mistersecret312.aperture_innovations.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.renderer.LongFallBootsRenderProperties;
import org.jetbrains.annotations.Nullable;

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

	@Override
	public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		return ApertureInnovations.MODID + ":textures/models/armor/long_fall_boots.png";
	}
}
