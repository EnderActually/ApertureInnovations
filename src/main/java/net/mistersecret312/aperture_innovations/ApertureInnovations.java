package net.mistersecret312.aperture_innovations;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.aperture_innovations.client.Layers;
import net.mistersecret312.aperture_innovations.client.overlay.CrosshairOverlay;
import net.mistersecret312.aperture_innovations.client.renderer.AntlineRenderer;
import net.mistersecret312.aperture_innovations.client.renderer.LongFallBootsRenderProperties;
import net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderProperties;
import net.mistersecret312.aperture_innovations.client.resourcepack.ResourcePackReloadListener;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.*;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItemProperty;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.registries.*;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.IOException;

import static net.neoforged.fml.loading.FMLEnvironment.dist;

@Mod(ApertureInnovations.MODID)
public class ApertureInnovations
{
	public static final String MODID = "aperture_innovations";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourceLocation of(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public ApertureInnovations(IEventBus modEventBus, ModContainer modContainer)
	{
		ItemInit.register(modEventBus);
		BlockInit.register(modEventBus);
		BlockEntityInit.register(modEventBus);
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
		if(dist.isClient())
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new PortalGunItem.Energy(stack), ItemInit.PORTAL_GUN);
		event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new LongFallBootsItem.Energy(stack), ItemInit.LONG_FALL_BOOTS);
	}

	public static boolean isIrisLoaded()
	{
		return ModList.get().isLoaded("iris");
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		public static ShaderInstance portalCorridorShaderInstance;

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
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerBlockEntityRenderer(BlockEntityInit.ANTLINE.get(), AntlineRenderer::new);
		}

		@SubscribeEvent
		public static void registerClientExtensions(RegisterClientExtensionsEvent event)
		{
			event.registerItem(LongFallBootsRenderProperties.INSTANCE, ItemInit.LONG_FALL_BOOTS);
			event.registerItem(PortalGunRenderProperties.INSTANCE, ItemInit.PORTAL_GUN);
		}

		@SubscribeEvent
		public static void registerShaders(RegisterShadersEvent event) throws IOException
		{
			event.registerShader(new ShaderInstance(event.getResourceProvider(),
					ResourceLocation.fromNamespaceAndPath(MODID, "portal_corridor"),
					DefaultVertexFormat.POSITION_TEX_COLOR),
					shaderInstance -> portalCorridorShaderInstance = shaderInstance);
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
