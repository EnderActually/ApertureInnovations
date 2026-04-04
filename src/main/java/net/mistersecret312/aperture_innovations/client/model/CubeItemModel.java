package net.mistersecret312.aperture_innovations.client.model;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.entities.CubeEntity;
import net.mistersecret312.aperture_innovations.items.CubeItem;
import software.bernie.geckolib.model.GeoModel;

public class CubeItemModel extends GeoModel<CubeItem>
{
	@Override
	public ResourceLocation getModelResource(CubeItem animatable)
	{
		return animatable.getClientVariant().modelPath();
	}

	@Override
	public ResourceLocation getTextureResource(CubeItem animatable)
	{
		return animatable.getClientVariant().hullTexture();
	}

	@Override
	public ResourceLocation getAnimationResource(CubeItem animatable)
	{
		return null;
	}
}
