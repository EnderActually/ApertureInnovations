package net.mistersecret312.aperture_innovations.client.screen.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.aperture_innovations.multitool.ConfigurationProperty;
import net.mistersecret312.aperture_innovations.multitool.IItemConfiguration;

import java.util.HashMap;

public class ItemPreviewRenderer implements PreviewRenderer
{
	public ItemStack stack;
	public ItemPreviewRenderer(ItemStack stack)
	{
		this.stack = stack.copy();
	}

	@Override
	public void render(GuiGraphics graphics, PoseStack poseStack, int mouseX, int mouseY, float partialTick)
	{
		poseStack.pushPose();
		graphics.renderItem(this.stack, 0, 0);

		poseStack.popPose();
	}

	@Override
	public void applyFakeState(HashMap<String, Object> map)
	{
		if(stack == null || !(stack.getItem() instanceof IItemConfiguration configuration))
			return;

		for(ConfigurationProperty<?> property : configuration.getConfigurationProperties(stack))
		{
			Object value = map.get(property.getName());
			if(value != null)
				property.setUnsafe(value);
		}
	}
}
