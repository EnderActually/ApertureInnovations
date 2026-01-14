package net.mistersecret312.aperture_innovations.items;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import org.jetbrains.annotations.Nullable;

public class LongFallBootsItem extends ArmorItem
{
	public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "textures/models/armor/long_fall_boots.png");

	public LongFallBootsItem(Holder<ArmorMaterial> material, Type type, Properties properties)
	{
		super(material, type, properties);
	}

	@Override
	public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer)
	{
		return true;
	}

	public static boolean isWorn(LivingEntity entity)
	{
		ItemStack boots = entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
		return boots.getItem() instanceof LongFallBootsItem;
	}

	@Override
	public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
													  ArmorMaterial.Layer layer, boolean innerModel)
	{
		return TEXTURE;
	}
}

