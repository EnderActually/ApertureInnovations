package net.mistersecret312.aperture_innovations.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;

public class ItemInit
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			ApertureInnovations.MODID);

	public static final RegistryObject<Item> PORTAL_GUN = ITEMS.register("portal_gun",
			() -> new PortalGunItem(new Item.Properties().stacksTo(1).fireResistant()));

	public static void register(IEventBus bus)
	{
		ITEMS.register(bus);
	}
}
