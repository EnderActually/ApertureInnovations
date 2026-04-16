package net.mistersecret312.aperture_innovations.items;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.MasterBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.multiblock.DummyBlock;
import net.mistersecret312.aperture_innovations.client.screen.MultiToolScreen;
import net.mistersecret312.aperture_innovations.client.screen.renderers.BlockEntityPreviewRenderer;
import net.mistersecret312.aperture_innovations.client.screen.renderers.ItemPreviewRenderer;
import net.mistersecret312.aperture_innovations.client.screen.renderers.PreviewRenderer;
import net.mistersecret312.aperture_innovations.multitool.IHaveConfiguration;
import net.mistersecret312.aperture_innovations.multitool.IItemConfiguration;
import org.jetbrains.annotations.NotNull;

public class MultiToolItem extends Item
{

	public MultiToolItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
	{
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();

		BlockState state = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		if(state.getBlock() instanceof DummyBlock dummyBlock)
		{
			MasterBlockEntity master = dummyBlock.getMaster(level, pos);
			if(master != null)
			{
				blockEntity = master;
				state = master.getBlockState();
			}
		}

		if(level.isClientSide())
		{
			PreviewRenderer renderer = new BlockEntityPreviewRenderer(state, blockEntity, level.registryAccess());
			MultiToolScreen screen = new MultiToolScreen(state.getBlock().getName(),
					blockEntity instanceof IHaveConfiguration configuration ? configuration : null,
					renderer);

			Minecraft.getInstance().setScreen(screen);
		}

		return super.useOn(context);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player)
	{
		ItemStack other = slot.getItem();
		if(player.level().isClientSide() && other.getItem() instanceof IItemConfiguration configuration)
		{
			ItemPreviewRenderer renderer = new ItemPreviewRenderer(other);
			MultiToolScreen screen = new MultiToolScreen(other.getHoverName(), configuration, renderer);

			Minecraft.getInstance().setScreen(screen);
		}

		return false;
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
											Player player, SlotAccess access)
	{
		if(player.level().isClientSide() && other.getItem() instanceof IItemConfiguration configuration)
		{
			ItemPreviewRenderer renderer = new ItemPreviewRenderer(other);
			MultiToolScreen screen = new MultiToolScreen(other.getHoverName(), configuration, renderer);

			Minecraft.getInstance().setScreen(screen);
		}

		return false;
	}
}
