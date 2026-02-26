package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityInit
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, ApertureInnovations.MODID);

	public static final Supplier<BlockEntityType<AntlineBlockEntity>> ANTLINE =
			BLOCK_ENTITIES.register("antline",
					() -> BlockEntityType.Builder.of(AntlineBlockEntity::new, BlockInit.ANTLINE.get()).build(null));

	public static final Supplier<BlockEntityType<AntlineOutputBlockEntity>> CHECKMARK =
			BLOCK_ENTITIES.register("antline_checkmark",
					() -> BlockEntityType.Builder.of(AntlineOutputBlockEntity::new, BlockInit.CHECKMARK.get()).build(null));
	public static final Supplier<BlockEntityType<AntlineTimerBlockEntity>> TIMER =
			BLOCK_ENTITIES.register("antline_timer",
					() -> BlockEntityType.Builder.of(AntlineTimerBlockEntity::new, BlockInit.TIMER.get()).build(null));

	public static final Supplier<BlockEntityType<PedestalButtonBlockEntity>> PEDESTAL_BUTTON =
			BLOCK_ENTITIES.register("pedestal_button",
					() -> BlockEntityType.Builder.of(PedestalButtonBlockEntity::new, BlockInit.PEDESTAL_BUTTON.get()).build(null));

	public static final Supplier<BlockEntityType<LargeButtonBlockEntity>> LARGE_BUTTON =
			BLOCK_ENTITIES.register("large_button",
					() -> BlockEntityType.Builder.of(LargeButtonBlockEntity::new, BlockInit.LARGE_BUTTON.get()).build(null));


	public static void register(IEventBus bus)
	{
		BLOCK_ENTITIES.register(bus);
	}
}
