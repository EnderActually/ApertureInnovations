package net.mistersecret312.aperture_innovations.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.minecraft.world.level.block.Block;
import net.mistersecret312.aperture_innovations.blocks.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockInit
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ApertureInnovations.MODID);

    public static final DeferredBlock<Block> METAL_SURFACE_BLOCK = registerBlock("metal_surface_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final DeferredBlock<Block> METAL_SURFACE_TILE_BLOCK = registerBlock("metal_surface_tile_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final DeferredBlock<Block> METAL_SURFACE_1x2_BLOCK = registerBlock("metal_surface_1x2_block",
            () -> new VerticalOneByTwoBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));

    public static final DeferredBlock<Block> CONCRETE_SURFACE_BLOCK = registerBlock("concrete_surface_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final DeferredBlock<Block> CONCRETE_SURFACE_TILE_BLOCK = registerBlock("concrete_surface_tile_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final DeferredBlock<Block> CONCRETE_SURFACE_1x2_BLOCK = registerBlock("concrete_surface_1x2_block",
            () -> new VerticalOneByTwoBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));

    public static final DeferredBlock<Block> ANTLINE = registerBlock("antline",
            () -> new AntlineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE)
                 .isRedstoneConductor((state, getter, pos) -> true)));

    public static final DeferredBlock<Block> CHECKMARK = registerBlock("antline_checkmark",
            () -> new AntlineOutputBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE)));
    public static final DeferredBlock<Block> TIMER = registerBlock("antline_timer",
            () -> new AntlineTimerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE)));

    public static final DeferredBlock<Block> PEDESTAL_BUTTON = registerBlock("pedestal_button",
            () -> new PedestalButtonBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> LARGE_BUTTON = registerBlock("large_button",
            () -> new LargeButtonBlock(BlockBehaviour.Properties.of().noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block)
    {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredHolder<Item, BlockItem> registerBlockItem(String name, DeferredBlock<T> block)
    {
        return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}
