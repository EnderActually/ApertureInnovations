package net.mistersecret312.aperture_innovations.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;

public interface IDynamicTexture<T extends GeoAnimatable>
{
	ResourceLocation getTexture(GeoBone bone, T animatable);
	int getColor(GeoBone bone, T animatable);
	RenderType getRenderType(GeoBone bone, T animatable);
}
