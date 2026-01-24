package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.minecraft.world.level.block.Block;
import net.mistersecret312.aperture_innovations.blocks.VerticalOneByTwoBlock;

import java.util.function.Supplier;

public class BlockInit
{
    public static final Block METAL_SURFACE_BLOCK = registerBlock("metal_surface_block",
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final Block METAL_SURFACE_TILE_BLOCK = registerBlock("metal_surface_tile_block",
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final Block METAL_SURFACE_1x2_BLOCK = registerBlock("metal_surface_1x2_block",
            new VerticalOneByTwoBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));

    public static final Block CONCRETE_SURFACE_BLOCK = registerBlock("concrete_surface_block",
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final Block CONCRETE_SURFACE_TILE_BLOCK = registerBlock("concrete_surface_tile_block",
            new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final Block CONCRETE_SURFACE_1x2_BLOCK = registerBlock("concrete_surface_1x2_block",
            new VerticalOneByTwoBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));

    public static void initialize() {
		// Just to force class loading
    }

	public static <T extends Block> T registerBlock(String name, T block, boolean shouldRegisterItem) {
		// Register the block and its item.
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, name);

		// Sometimes, you may not want to register an item for the block.
		// Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
		if (shouldRegisterItem) {
			BlockItem blockItem = new BlockItem(block, new Item.Properties());
			Registry.register(BuiltInRegistries.ITEM, id, blockItem);
		}

		return Registry.register(BuiltInRegistries.BLOCK, id, block);
	}

	public static <P extends Block> P registerBlock(String name, P block) {
		return registerBlock(name, block, true);
	}
}
