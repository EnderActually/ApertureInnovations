package net.mistersecret312.aperture_innovations.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColorfulGelItem extends Item
{

	public ColorfulGelItem(Properties pProperties)
	{
		super(pProperties);
	}
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components,
								TooltipFlag flag)
	{
		int gelColor = getColor(stack);
		if(gelColor != -1)
			components.add(Component.translatable("item.aperture_innovations.colorful_gel.color", Integer.toHexString(gelColor).toUpperCase()).withStyle(style -> style.withColor(gelColor)));
	}



	public void setColor(ItemStack stack, int color)
	{
		stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
	}

	public int getColor(ItemStack stack)
	{
		DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
		if(color == null)
			return -1;
		return color.rgb();
	}
}
