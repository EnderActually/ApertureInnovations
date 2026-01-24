package net.mistersecret312.aperture_innovations;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
import net.mistersecret312.aperture_innovations.client.Layers;
import net.mistersecret312.aperture_innovations.client.overlay.CrosshairOverlay;
import net.mistersecret312.aperture_innovations.client.renderer.LongFallBootsRenderProperties;
import net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderProperties;
import net.mistersecret312.aperture_innovations.client.resourcepack.ResourcePackReloadListener;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.*;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItemProperty;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import org.apache.logging.log4j.util.Lazy;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApertureInnovations implements ModInitializer
{
	public static final String MODID = "aperture_innovations";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final TagKey<Block> SHOOT_THROUGH = TagKey.create(
			BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath(MODID, "shoot_through"));
	public static final TagKey<Block> IMPORTALABLE = TagKey.create(
			BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath(MODID, "importalable"));
	public static final TagKey<Block> PORTALABLE = TagKey.create(
			BuiltInRegistries.BLOCK.key(), ResourceLocation.fromNamespaceAndPath(MODID, "portalable"));

	public static ResourceLocation of(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public ApertureInnovations(IEventBus modEventBus, ModContainer modContainer)
	{
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
		if(dist.isClient())
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	@Override
	public void onInitialize() {
		ItemInit.initialize();
		BlockInit.initialize();
		ItemTabInit.initialize();
		SoundInit.initialize();
		StatisticsInit.register();
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new PortalGunItem.Energy(stack), ItemInit.PORTAL_GUN);
		event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new LongFallBootsItem.Energy(stack), ItemInit.LONG_FALL_BOOTS);
	}

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
			ColorfulGelItem gelItem = ItemInit.COLORFUL_GEL.get();
			event.register(((stack, color) -> color != 1 ? -1 : FastColor.ARGB32.opaque(gelItem.getColor(stack))), ItemInit.COLORFUL_GEL.get());
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
