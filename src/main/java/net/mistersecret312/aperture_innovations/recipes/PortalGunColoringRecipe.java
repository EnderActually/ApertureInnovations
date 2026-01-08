package net.mistersecret312.aperture_innovations.recipes;

import com.google.common.collect.Lists;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.RecipeInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;

public class PortalGunColoringRecipe extends CustomRecipe
{

	public PortalGunColoringRecipe(ResourceLocation pId, CraftingBookCategory pCategory)
	{
		super(pId, pCategory);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();

		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack itemstack1 = container.getItem(i);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() instanceof PortalGunItem) {
					if (!itemstack.isEmpty()) {
						return false;
					}

					itemstack = itemstack1;
				} else {
					if (!(itemstack1.getItem() instanceof ColorfulGelItem)) {
						return false;
					}

					list.add(itemstack1);
				}
			}
		}

		return !itemstack.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess)
	{
		List<Pair<Integer, ItemStack>> list = Lists.newArrayList();
		ItemStack itemstack = ItemStack.EMPTY;
		int gunStack = -1;

		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack itemstack1 = container.getItem(i);
			if (!itemstack1.isEmpty()) {
				Item item = itemstack1.getItem();
				if (item instanceof PortalGunItem)
				{
					if (!itemstack.isEmpty())
					{
						return ItemStack.EMPTY;
					}

					itemstack = itemstack1.copy();
					gunStack = i;
				} else {
					if (!(item instanceof ColorfulGelItem))
					{
						return ItemStack.EMPTY;
					}

					list.add(new Pair<>(i, itemstack1));
				}
			}
		}

		ItemStack gunItemStack = container.getItem(gunStack).copy();
		PortalGunItem gunItem = (PortalGunItem) gunItemStack.getItem();

		for(int i = 0; i < list.size(); i++)
		{
			int slotID = list.get(i).getA();
			ItemStack gelStack = list.get(i).getB();
			ColorfulGelItem gel = (ColorfulGelItem) gelStack.getItem();

			if(slotID-gunStack == -1)
				gunItem.setPrimaryStripeColor(gunItemStack, gel.getColor(gelStack));
			if(slotID-gunStack == 1)
				gunItem.setSecondaryStripeColor(gunItemStack, gel.getColor(gelStack));
			if(slotID-gunStack == 3)
				gunItem.setPrimaryPortalColor(gunItemStack, gel.getColor(gelStack));
			if(slotID-gunStack == -3)
				gunItem.setSecondaryPortalColor(gunItemStack, gel.getColor(gelStack));
		}

		return !itemstack.isEmpty() && !list.isEmpty() ? gunItemStack : ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeInit.GUN_COLORING.get();
	}
}
