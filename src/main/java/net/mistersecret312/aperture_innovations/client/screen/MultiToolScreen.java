package net.mistersecret312.aperture_innovations.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.screen.renderers.BlockEntityPreviewRenderer;
import net.mistersecret312.aperture_innovations.client.screen.renderers.PreviewRenderer;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.multitool.Color;
import net.mistersecret312.aperture_innovations.multitool.ConfigurationProperty;
import net.mistersecret312.aperture_innovations.multitool.IHaveConfiguration;
import net.mistersecret312.aperture_innovations.multitool.InteractionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiToolScreen extends Screen
{
	private HashMap<String, Object> properties = new HashMap<>();
	private HashMap<String, Category> categories = new HashMap<>();
	private IHaveConfiguration config = null;
	private final PreviewRenderer renderer;

	private String openCategory = "";

	public MultiToolScreen(Component title, IHaveConfiguration configuration, PreviewRenderer renderer)
	{
		super(title);
		if(configuration != null)
		{
			this.config = configuration;

			for(ConfigurationProperty<?> property : configuration.getConfigurationProperties())
			{
				properties.put(property.getName(), property.get());

				Category category = categories.computeIfAbsent(property.getCategory(), name -> new Category(
						name, new HashMap<>()));
				category.entries.computeIfAbsent(property.getName(), name -> new CategoryEntry(name,
						property.getTranslatable()));
			}
		}

		this.renderer = renderer;

		this.renderer.applyFakeState(properties);
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		if(config == null)
			return;

		int id = 0;
		for(ConfigurationProperty<?> property : config.getConfigurationProperties())
		{
			makeWidget(property, (int) (width/1.5f), width/16+id*(24));
			id++;
		}

		for(Map.Entry<String, Category> entry : categories.entrySet())
		{
			this.addRenderableWidget(new PlainTextButton((int) (width/10f), (int) (height/8f), 25, 10,
					Component.literal(entry.getKey()), button ->
				{
					System.out.println("Category: " + entry.getKey());
					this.openCategory = entry.getKey();
				}, Minecraft.getInstance().font));
		}
	}

	@Override
	public void onClose()
	{
		super.onClose();
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

		poseStack.translate(this.width / 4f, this.height / 4f, 0);
		graphics.drawCenteredString(Minecraft.getInstance().font, title, 0,0, -1);

		properties.forEach((string, value) -> {
			poseStack.translate(0, -10, 0);
			graphics.drawString(Minecraft.getInstance().font, string + ": " + value.toString(), 0, 0, -1);
		});

		poseStack.popPose();
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	private void makeWidget(ConfigurationProperty<?> property, int x, int y)
	{
		if(property.getInteraction() instanceof InteractionType.RGBColorPicker)
		{
			String name = property.getName();
			for(int i = 0; i < 3; i++)
			{
				String text = "R";
				if(i == 1)
					text = "G";
				if(i == 2)
					text = "B";
				int textColor = 0xFF0000;
				if(i == 1)
					textColor = 0x00FF00;
				if(i == 2)
					textColor = 0x0000FF;

				EditBox box = new EditBox(Minecraft.getInstance().font, x+i*44, y, 40, 16,
						Component.empty());
				box.setFilter(string -> string.matches("^$|^([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$"));
				int finalI = i;
				box.setTextColor(textColor);
				box.setResponder(string ->
					{
						if(string.isBlank())
							return;

						int value;
						try
						{
							value = Integer.parseInt(string);
						}
						catch(NumberFormatException ignored)
						{
							value = Integer.MAX_VALUE;
							box.setValue(String.valueOf(value));
						}
						Object object = properties.get(name);
						if(object instanceof Color(int red, int green, int blue))
						{
							if(finalI == 0)
								properties.put(name, new Color(value, green, blue));
							if(finalI == 1)
								properties.put(name, new Color(red, value, blue));
							if(finalI == 2)
								properties.put(name, new Color(red, green, value));
						}
						this.renderer.applyFakeState(properties);
					});
				box.setHint(Component.literal(text));
				this.addRenderableWidget(box);
			}
		}
	}

	public String getOpenCategory()
	{
		return openCategory;
	}

	public void setOpenCategory(String openCategory)
	{
		this.openCategory = openCategory;
	}

	record CategoryEntry(String name, String translatable)
	{

	}

	record Category(String category, HashMap<String, CategoryEntry> entries)
	{

	}
}
