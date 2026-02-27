package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.entities.WeightedCompanionCubeEntity;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityInit
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
			DeferredRegister.create(Registries.ENTITY_TYPE, ApertureInnovations.MODID);

	public static final DeferredHolder<EntityType<?>, EntityType<WeightedStorageCubeEntity>> WEIGHTED_STORAGE_CUBE =
			ENTITY_TYPES.register("weighted_storage_cube",
					() -> EntityType.Builder.<WeightedStorageCubeEntity>of(WeightedStorageCubeEntity::new, MobCategory.MISC)
								  .sized(0.75f, 0.75f)
								  .build(ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
										  "weighted_storage_cube").toString()));

	public static final DeferredHolder<EntityType<?>, EntityType<WeightedCompanionCubeEntity>> WEIGHTED_COMPANION_CUBE =
			ENTITY_TYPES.register("weighted_companion_cube",
					() -> EntityType.Builder.<WeightedCompanionCubeEntity>of(WeightedCompanionCubeEntity::new, MobCategory.MISC)
											.sized(0.75f, 0.75f)
											.build(ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
													"weighted_companion_cube").toString()));

	public static void register(IEventBus bus)
	{
		ENTITY_TYPES.register(bus);
	}
}
