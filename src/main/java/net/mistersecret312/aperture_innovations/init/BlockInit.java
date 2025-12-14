package net.mistersecret312.aperture_innovations.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.blocks.OneByTwoBlock;

import java.util.function.Supplier;

public class BlockInit
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ApertureInnovations.MODID);

    public static final RegistryObject<Block> METAL_SURFACE_BLOCK = registerBlock("metal_surface_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> METAL_SURFACE_TILE_BLOCK = registerBlock("metal_surface_tile_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> METAL_SURFACE_1x2_BLOCK = registerBlock("metal_surface_1x2_block",
            () -> new OneByTwoBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> METAL_SURFACE_2x2_BLOCK = registerBlock("metal_surface_2x2_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY)));

    public static final RegistryObject<Block> CONCRETE_SURFACE_BLOCK = registerBlock("concrete_surface_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).requiresCorrectToolForDrops().mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> CONCRETE_SURFACE_TILE_BLOCK = registerBlock("concrete_surface_tile_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).requiresCorrectToolForDrops().mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> CONCRETE_SURFACE_1x2_BLOCK = registerBlock("concrete_surface_1x2_block",
            () -> new OneByTwoBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).requiresCorrectToolForDrops().mapColor(MapColor.TERRACOTTA_WHITE)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block)
    {
        return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}
