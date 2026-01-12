package net.mistersecret312.aperture_innovations.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.model.LongFallBootsModel;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class Layers
{
	public static final ModelLayerLocation LONG_FALL_BOOTS = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(
			ApertureInnovations.MODID, "long_fall_boots"), "main");


	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(LONG_FALL_BOOTS, LongFallBootsModel::createBodyLayer);
	}
}
