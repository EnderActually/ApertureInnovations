package net.mistersecret312.aperture_innovations.client.screen.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class ItemPreviewRenderer implements PreviewRenderer
{
	private final ItemStack stack;
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

	}
}
