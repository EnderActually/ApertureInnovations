package net.mistersecret312.aperture_innovations;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.mistersecret312.aperture_innovations.client.Layers;
import net.mistersecret312.aperture_innovations.client.overlay.CrosshairOverlay;
import net.mistersecret312.aperture_innovations.client.renderer.LongFallBootsRenderProperties;
import net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderProperties;
import net.mistersecret312.aperture_innovations.client.resourcepack.ResourcePackReloadListener;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.*;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItemProperty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.*;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(ApertureInnovations.MODID)
public class ApertureInnovations
{
	public static final String MODID = "aperture_innovations";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final TagKey<Block> SHOOT_THROUGH = TagKey.create(
			BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath(MODID, "shoot_through"));
	public static final TagKey<Block> IMPORTALABLE = TagKey.create(
			BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath(MODID, "importalable"));
	public static final TagKey<Block> PORTALABLE = TagKey.create(
			BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath(MODID, "portalable"));

	public ApertureInnovations(IEventBus modEventBus, ModContainer modContainer)
	{
		ItemInit.register(modEventBus);
		BlockInit.register(modEventBus);
		ItemTabInit.register(modEventBus);
		SoundInit.register(modEventBus);
		StatisticsInit.register(modEventBus);
		RecipeInit.register(modEventBus);
		AdvancementInit.register(modEventBus);
		AttachmentTypeInit.register(modEventBus);
		DataComponentInit.register(modEventBus);

		modEventBus.addListener(Layers::registerLayers);
		modEventBus.addListener(NetworkInit::registerPackets);
		modEventBus.addListener(ApertureInnovations::registerCapabilities);

		modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) ->
			{
				event.dataPackRegistry(PortalGunVariant.REGISTRY_KEY, PortalGunVariant.CODEC, PortalGunVariant.CODEC);
			});

		modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "aperture_innovations-common.toml");
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new EnergyStorage(
				PortalGunConfig.portal_gun_max_energy_stored.get()), ItemInit.PORTAL_GUN);
	}

	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		public static final Lazy<KeyMapping> RESET_PORTAL_GUN = Lazy.of(() -> new KeyMapping("aperture_innovations.portal_gun.reset",
				KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category.aperture_innovations"));


		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{
			event.enqueueWork(()
									  -> {
					ItemProperties.register(ItemInit.COLORFUL_GEL.get(),
							ResourceLocation.fromNamespaceAndPath(MODID, "colored"), new ColorfulGelItemProperty());
				});
		}

		@SubscribeEvent
		public static void registerClientExtensions(RegisterClientExtensionsEvent event)
		{
			event.registerItem(LongFallBootsRenderProperties.INSTANCE, ItemInit.LONG_FALL_BOOTS);
			event.registerItem(PortalGunRenderProperties.INSTANCE, ItemInit.PORTAL_GUN);
		}

		@SubscribeEvent
		public static void registerClientReload(RegisterClientReloadListenersEvent event)
		{
			ResourcePackReloadListener.ReloadListener.registerReloadListener(event);
		}

		@SubscribeEvent
		public static void registerItemColors(RegisterColorHandlersEvent.Item event)
		{
			ColorfulGelItem gelItem = (ColorfulGelItem) ItemInit.COLORFUL_GEL.get();
			event.register(((stack, color) -> color != 1 ? -1 : gelItem.getColor(stack)), ItemInit.COLORFUL_GEL.get());
		}

		@SubscribeEvent
		public static void registerKeybindings(RegisterKeyMappingsEvent event)
		{
			event.register(RESET_PORTAL_GUN.get());
		}

		@SubscribeEvent
		public static void registerOverlays(RegisterGuiLayersEvent event)
		{
			event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MODID, "portal_crosshair"),
					CrosshairOverlay.OVERLAY);
		}
	}
}
