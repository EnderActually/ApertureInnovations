package net.mistersecret312.aperture_innovations.client.renderer.item;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.renderer.ColoredGlowingLayer;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientCubeVariant;
import net.mistersecret312.aperture_innovations.items.CubeItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoItemRenderer;

import java.awt.*;

public class CubeItemRenderer extends DynamicGeoItemRenderer<CubeItem>
{

    public CubeItemRenderer()
    {
        super(new DefaultedItemGeoModel<>(ApertureInnovations.of( "weighted_cube"))
        {
            @Override
            protected String subtype()
            {
                return "entity";
            }
        });

        this.addRenderLayer(new ColoredGlowingLayer<>(this,
                (cubeItem, bone) -> this.getTexture(bone, cubeItem),
                (cubeItem, bone) -> this.getColor(bone, cubeItem),
                (cubeItem, bone) -> this.getRenderType(bone, cubeItem)));
    }

    public ResourceLocation getTexture(GeoBone bone, CubeItem animatable)
    {
        int color = animatable.getColor(getCurrentItemStack());
        ClientCubeVariant cubeVariant = animatable.getCubeVariant(getCurrentItemStack());
        if(!bone.getName().equals("ColoredCircle"))
            return null;

        ResourceLocation texture = cubeVariant.idleTexture().orElse(null);
        if(color != 0)
            texture = cubeVariant.genericTexture().orElse(null);

        return texture;
    }

    public int getColor(GeoBone bone, CubeItem animatable)
    {
        int color = this.getAnimatable().getColor(getCurrentItemStack());

        return new Color(color-1, false).getRGB();
    }

    public RenderType getRenderType(GeoBone bone, CubeItem animatable)
    {
        return PortalRenderTypes.APERTURE_GLOW.apply(getTexture(bone, animatable), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
    }
}
