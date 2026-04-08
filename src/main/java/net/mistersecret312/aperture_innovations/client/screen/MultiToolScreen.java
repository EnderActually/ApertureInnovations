package net.mistersecret312.aperture_innovations.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
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
import net.mistersecret312.aperture_innovations.multitool.ConfigurationProperty;
import net.mistersecret312.aperture_innovations.multitool.IHaveConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MultiToolScreen extends Screen
{
	private HashMap<String, Object> properties = new HashMap<>();
	private IHaveConfiguration config = null;
	private final PreviewRenderer renderer;

	public MultiToolScreen(Component title, IHaveConfiguration configuration, PreviewRenderer renderer)
	{
		super(title);
		if(configuration != null)
		{
			this.config = configuration;

			for(ConfigurationProperty<?> property : configuration.getConfigurationProperties())
			{
				properties.put(property.getName(), property.get());
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

		poseStack.translate(this.width / 2f, this.height / 2f, 0);
		this.renderer.render(graphics, poseStack, mouseX, mouseY, partialTick);

		poseStack.popPose();

		poseStack.pushPose();

		poseStack.translate(this.width / 2f, this.height / 4f, 0);
		graphics.drawCenteredString(Minecraft.getInstance().font, title, 0,0, -1);

		properties.forEach((string, value) -> {
			poseStack.translate(0, -10, 0);
			graphics.drawString(Minecraft.getInstance().font, string + ": " + value, 0, 0, -1);
		});

		poseStack.popPose();
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}
