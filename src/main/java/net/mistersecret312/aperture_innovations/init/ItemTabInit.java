package net.mistersecret312.aperture_innovations.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
public class ItemTabInit
{
	public static final CreativeModeTab APERTURE_INNOVATIONS = register("aperture_innovations",
			FabricItemGroup.builder()
						  .icon(() -> new ItemStack(ItemInit.PORTAL_GUN))
						  .title(Component.translatable("tabs.aperture_innovations"))
						  .displayItems((parameters, output) -> {
							  output.accept(PortalGunItem.createPortalGun(ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "chell")));
							  output.accept(PortalGunItem.createPortalGun(ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "atlas")));
							  output.accept(PortalGunItem.createPortalGun(ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "pbody")));

							  output.accept(ItemInit.LONG_FALL_BOOTS);

							  output.accept(ItemInit.COLORFUL_GEL);

							  output.accept(BlockInit.CONCRETE_SURFACE_BLOCK);
							  output.accept(BlockInit.CONCRETE_SURFACE_TILE_BLOCK);
							  output.accept(BlockInit.CONCRETE_SURFACE_1x2_BLOCK);

							  output.accept(BlockInit.METAL_SURFACE_BLOCK);
							  output.accept(BlockInit.METAL_SURFACE_TILE_BLOCK);
							  output.accept(BlockInit.METAL_SURFACE_1x2_BLOCK);
						  }
						  ).build());

	public static void initialize() {

	}

	public static CreativeModeTab register(String id, CreativeModeTab tab) {
		ResourceLocation location = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, id);

		return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, location, tab);
	}
}
