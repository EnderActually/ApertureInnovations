package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;

public class ItemInit
{
	public static final PortalGunItem PORTAL_GUN = register("portal_gun",
			new PortalGunItem(new Item.Properties().stacksTo(1).fireResistant()));
	public static final ColorfulGelItem COLORFUL_GEL = register("colorful_gel",
			new ColorfulGelItem(new Item.Properties().stacksTo(64)));

	public static final LongFallBootsItem LONG_FALL_BOOTS = register("long_fall_boots",
			new LongFallBootsItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).fireResistant()));

	public static void initialize()
	{
		// Just to force class loading
	}

	public static <T extends Item> T register(String id, T item)
	{
		ResourceLocation location = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, id);
		return Registry.register(BuiltInRegistries.ITEM, location, item);
	}
}
