package net.mistersecret312.aperture_innovations.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.mistersecret312.aperture_innovations.client.screen.renderers.BlockEntityPreviewRenderer;
import net.mistersecret312.aperture_innovations.client.screen.renderers.EntityPreviewRenderer;
import net.mistersecret312.aperture_innovations.client.screen.renderers.ItemPreviewRenderer;
import net.mistersecret312.aperture_innovations.client.screen.renderers.PreviewRenderer;
import net.mistersecret312.aperture_innovations.multitool.*;
import net.mistersecret312.aperture_innovations.network.ServerboundMultiToolApplyBlockEntityPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundMultiToolApplyEntityPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundMultiToolApplyItemStackPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MultiToolScreen extends Screen
{
	public HashMap<String, Object> properties = new HashMap<>();
	public HashMap<String, Category> categories = new HashMap<>();

	public HashMap<Category, List<Renderable>> categoryWidgets = new HashMap<>();

	private IHaveConfiguration config = null;
	public final PreviewRenderer renderer;

	public boolean hsbMode = false;
	public boolean colorSliderMode = false;

	private String openCategory = "";

	public MultiToolScreen(Component title, IHaveConfiguration configuration, PreviewRenderer renderer)
	{
		super(title);
		this.renderer = renderer;

		if(configuration != null)
		{
			this.config = configuration;
			if(configuration instanceof Item)
				constructItemProperties();
			else constructProperties();
		}

		this.renderer.applyFakeState(properties);
	}

	public void constructProperties()
	{
		for(ConfigurationProperty<?> property : this.config.getConfigurationProperties(Minecraft.getInstance().level.registryAccess()))
		{
			properties.put(property.getName(), property.get());

			Category category = categories.computeIfAbsent(property.getCategory(), name -> new Category(
					name, new HashMap<>()));
			category.entries.computeIfAbsent(property.getName(), name -> new CategoryEntry(name,
					property.getTranslatable()));
		}
	}

	public void constructItemProperties()
	{
		if(!(renderer instanceof ItemPreviewRenderer itemPreviewRenderer))
			return;
		if(!(config instanceof IItemConfiguration configuration))
			return;

		for(ConfigurationProperty<?> property : configuration.getConfigurationProperties(itemPreviewRenderer.stack, Minecraft.getInstance().level.registryAccess()))
		{
			properties.put(property.getName(), property.get());

			Category category = categories.computeIfAbsent(property.getCategory(), name -> new Category(
					name, new HashMap<>()));
			category.entries.computeIfAbsent(property.getName(), name -> new CategoryEntry(name,
					property.getTranslatable()));
		}
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		if(config == null)
			return;

		int categoryID = 0;
		for(Map.Entry<String, Category> entry : categories.entrySet())
		{
			categoryID++;
			if(!openCategory.isBlank() && !entry.getKey().equals(openCategory))
				continue;

			String type = "category.aperture_innovations."+entry.getKey();
			MutableComponent component = Component.translatable(type);

			int x = (int) (width/10f);
			int y = (int) (height/8f) + categoryID*12;
			this.addRenderableWidget(new PlainTextButton(2*x, y, Minecraft.getInstance().font.width(component), Minecraft.getInstance().font.lineHeight,
					component, button ->
				{
					if(this.openCategory.equals(entry.getKey()))
						this.openCategory = "";
					else this.openCategory = entry.getKey();
					init();
				}, Minecraft.getInstance().font));

			boolean hasColor = entry.getValue().entries.entrySet().stream().anyMatch(catEntry -> properties.get(catEntry.getKey()) instanceof Color);
			if(hasColor)
			{
				String colorMode = "RGB";
				if(hsbMode)
					colorMode = "HSB";

				String sliderMode = "Text";
				if(colorSliderMode)
					sliderMode = "Slider";

				this.addCategoryWidget(new PlainTextButton(2*x-24, y, Minecraft.getInstance().font.width(colorMode),
						Minecraft.getInstance().font.lineHeight, Component.literal(colorMode),
						button ->
							{
								this.hsbMode = !this.hsbMode;
								this.init();
							}, Minecraft.getInstance().font),
						entry.getValue());

				this.addCategoryWidget(new PlainTextButton(2*x-56, y, Minecraft.getInstance().font.width(sliderMode),
						Minecraft.getInstance().font.lineHeight, Component.literal(sliderMode),
						button ->
							{
								this.colorSliderMode = !this.colorSliderMode;
								this.init();
							}, Minecraft.getInstance().font),
						entry.getValue());
			}

			Category category = entry.getValue();
			if(config instanceof IItemConfiguration)
				makeWidgetForItemProperty(category, x, y);
			else makeWidgetForProperty(category, x, y);
		}
	}

	public void makeWidgetForProperty(Category category, int x, int y)
	{
		int entryID = 0;
		for(Map.Entry<String, CategoryEntry> entry : category.entries.entrySet())
		{
			Optional<ConfigurationProperty<?>> property = config.getConfigurationProperties(Minecraft.getInstance().level.registryAccess()).stream().filter(
					prop -> prop.getName().equals(entry.getValue().name)
									&& prop.getCategory().equals(category.category)).findFirst();
			if(property.isPresent())
			{
				property.get().getInteraction().makeWidget(property.get(), x, y+(entryID)*24+12, this);
				entryID++;
			}
		}
	}

	public void makeWidgetForItemProperty(Category category, int x, int y)
	{
		if(!(renderer instanceof ItemPreviewRenderer itemRenderer))
			return;
		if(!(config instanceof IItemConfiguration configuration))
			return;

		int entryID = 0;
		for(Map.Entry<String, CategoryEntry> entry : category.entries.entrySet())
		{
			Optional<ConfigurationProperty<?>> property = configuration.getConfigurationProperties(itemRenderer.stack, Minecraft.getInstance().level.registryAccess()).stream().filter(
					prop -> prop.getName().equals(entry.getValue().name)
									&& prop.getCategory().equals(category.category)).findFirst();
			if(property.isPresent())
			{
				property.get().getInteraction().makeWidget(property.get(), x, y+(entryID)*24+12, this);
				entryID++;
			}
		}
	}

	@Override
	public void onClose()
	{
		super.onClose();
		if(Minecraft.getInstance().player != null)
			Minecraft.getInstance().player.inventoryMenu.broadcastChanges();

		if(this.config == null)
			return;

		for(ConfigurationProperty<?> property : this.config.getConfigurationProperties(Minecraft.getInstance().level.registryAccess()))
		{
			Object value = properties.get(property.getName());
			if(renderer instanceof BlockEntityPreviewRenderer blockEntityPreviewRenderer)
				PacketDistributor.sendToServer(new ServerboundMultiToolApplyBlockEntityPacket(blockEntityPreviewRenderer.blockEntity.getBlockPos(),
						property.getName(), property.getType(), value));
			if(renderer instanceof EntityPreviewRenderer entityPreviewRenderer)
				PacketDistributor.sendToServer(new ServerboundMultiToolApplyEntityPacket(entityPreviewRenderer.entity.getUUID(),
						property.getName(), property.getType(), value));
		}
		if(config instanceof IItemConfiguration itemConfiguration)
		{
			if(renderer instanceof ItemPreviewRenderer itemPreviewRenderer)
				PacketDistributor.sendToServer(new ServerboundMultiToolApplyItemStackPacket(itemPreviewRenderer.stack,
						itemPreviewRenderer.hand == InteractionHand.MAIN_HAND));

		}
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		renderBackground(graphics, mouseX, mouseY, partialTick);
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();

		graphics.fillRenderType(RenderType.endGateway(),
				width/3,
				height/6,
				(int) (width*0.66f), (int) (height/1.5f), -100);

		for(Renderable renderable : renderables)
		{
			renderable.render(graphics, mouseX, mouseY, partialTick);
		}

		poseStack.translate(this.width / 2f, this.height / 2f, 0);
		this.renderer.render(graphics, poseStack, mouseX, mouseY, partialTick);

		poseStack.popPose();

		poseStack.pushPose();

		poseStack.translate(this.width / 2f, this.height / 5f, 0);
		graphics.drawCenteredString(Minecraft.getInstance().font, title, 0,0, -1);

		poseStack.popPose();
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	public String getOpenCategory()
	{
		return openCategory;
	}

	public void setOpenCategory(String openCategory)
	{
		this.openCategory = openCategory;
	}

	public <T extends GuiEventListener & Renderable & NarratableEntry> void addCategoryWidget(T widget,
																							  Category category)
	{
		this.categoryWidgets.computeIfAbsent(category, ctg -> new ArrayList<>()).add(widget);
		if(this.openCategory.equals(category.category))
		{
			super.addRenderableWidget(widget);
		}
	}

	public record CategoryEntry(String name, String translatable)
	{

	}

	public record Category(String category, HashMap<String, CategoryEntry> entries)
	{

	}
}
