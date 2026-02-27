package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit
{
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ApertureInnovations.MODID);

	public static final DeferredItem<PortalGunItem> PORTAL_GUN = ITEMS.register("portal_gun",
			() -> new PortalGunItem(new Item.Properties().stacksTo(1).fireResistant()));
	public static final DeferredItem<ColorfulGelItem> COLORFUL_GEL = ITEMS.register("colorful_gel",
			() -> new ColorfulGelItem(new Item.Properties().stacksTo(64)));

	public static final DeferredItem<LongFallBootsItem> LONG_FALL_BOOTS = ITEMS.register("long_fall_boots",
			() -> new LongFallBootsItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).fireResistant()));

	public static final DeferredItem<CubeItem> WEIGHTED_STORAGE_CUBE = ITEMS.register("weighted_storage_cube",
			() -> new CubeItem(EntityInit.WEIGHTED_STORAGE_CUBE.get(), new Item.Properties().stacksTo(64)));
	public static final DeferredItem<CompanionCubeItem> WEIGHTED_COMPANION_CUBE = ITEMS.register("weighted_companion_cube",
			() -> new CompanionCubeItem(EntityInit.WEIGHTED_COMPANION_CUBE.get(), new Item.Properties().stacksTo(64)));

	public static void register(IEventBus bus)
	{
		ITEMS.register(bus);
	}
}
