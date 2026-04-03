package net.mistersecret312.aperture_innovations.client.model;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.PedestalButtonBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.VitalApparatusVentBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class VitalApparatusVentModel extends GeoModel<VitalApparatusVentBlockEntity>
{

	@Override
	public ResourceLocation getModelResource(VitalApparatusVentBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "geo/block/vital_apparatus_vent.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(VitalApparatusVentBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "textures/entity/vital_apparatus_vent/vital_apparatus_vent.png");
	}

	@Override
	public ResourceLocation getAnimationResource(VitalApparatusVentBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "animations/block/vital_apparatus_vent.animation.json");
	}
}
