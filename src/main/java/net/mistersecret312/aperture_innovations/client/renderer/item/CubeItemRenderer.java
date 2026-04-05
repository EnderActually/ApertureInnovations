package net.mistersecret312.aperture_innovations.client.renderer.item;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.ColorUtil;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.renderer.ColoredGlowingLayer;
import net.mistersecret312.aperture_innovations.client.renderer.IDynamicTexture;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientCubeVariant;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariant;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariants;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.items.CubeItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.utilities.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;

public class CubeItemRenderer extends DynamicGeoItemRenderer<CubeItem> implements IDynamicTexture<CubeItem>
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

        this.addRenderLayer(new ColoredGlowingLayer<>(this));
    }

    @Override
    public ResourceLocation getTexture(GeoBone bone, CubeItem animatable)
    {
        int color = animatable.getColor(getCurrentItemStack());
        ClientCubeVariant cubeVariant = animatable.getCubeVariant(getCurrentItemStack());
        if(!bone.getName().equals("ColoredCircle"))
            return null;

        ResourceLocation texture = cubeVariant.idleTexture().orElse(null);
        if(color != -1)
            texture = cubeVariant.genericTexture().orElse(null);

        return texture;
    }

    @Override
    public int getColor(GeoBone bone, CubeItem animatable)
    {
        int color = this.getAnimatable().getColor(getCurrentItemStack());

        return new Color(color, false).getRGB();
    }

    @Override
    public RenderType getRenderType(GeoBone bone, CubeItem animatable)
    {
        return PortalRenderTypes.APERTURE_GLOW.apply(getTexture(bone, animatable), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
    }
}
