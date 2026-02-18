package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.AntlineBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BlockEntityInit
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, ApertureInnovations.MODID);

	public static final Supplier<BlockEntityType<AntlineBlockEntity>> ANTLINE =
			BLOCK_ENTITIES.register("antline",
					() -> BlockEntityType.Builder.of(AntlineBlockEntity::new, BlockInit.ANTLINE.get()).build(null));

	public static void register(IEventBus bus)
	{
		BLOCK_ENTITIES.register(bus);
	}
}
