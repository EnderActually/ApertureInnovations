package net.mistersecret312.aperture_innovations.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PortalGunRenderer extends GeoItemRenderer<PortalGunItem>
{
	public PortalGunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new ResourceLocation(ApertureInnovations.MODID, "portal_gun")));
	}

	@Override
	public RenderType getRenderType(PortalGunItem animatable, ResourceLocation texture,
									@Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}
}
