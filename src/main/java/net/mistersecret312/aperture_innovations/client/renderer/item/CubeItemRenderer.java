package net.mistersecret312.aperture_innovations.client.renderer.item;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
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
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
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
    protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, CubeItem animatable,
                                                                ResourceLocation texturePath,
                                                                MultiBufferSource bufferSource, float partialTick) {
        List<String> gunCore = Lists.newArrayList("CoreOuter", "CoreInner", "PortalLight", "Muzzle");
        if(bone.getName().equals("Zap"))
            return RenderType.EYES.apply(getTextureOverrideForBone(bone, animatable, partialTick), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
        if (gunCore.contains(bone.getName())) {
            return PortalRenderTypes.APERTURE_GLOW.apply(getTextureOverrideForBone(bone, animatable, partialTick), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
        }
        return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
    }

    @Override
    protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone, CubeItem animatable,
                                                                   float partialTick) {
        int portal = this.getAnimatable().getLastShotPortal(this.currentItemStack);
        ClientPortalLink link = PortalUtilities.getPortalLinks().get(this.getAnimatable().getUUID(this.currentItemStack, false));

        if(bone.getName().equals("Zap") && animatable.getZapTick(currentItemStack) != -1)
        {
			return ApertureInnovations.of("textures/portal_gun/zap/portal_gun_zap_" + animatable.getZapTick(this.currentItemStack) + ".png");
        }

        if (link != null) {
            List<String> gunCore = Lists.newArrayList("CoreOuter", "CoreInner", "PortalLight", "Muzzle");
            if (gunCore.contains(bone.getName()))
                return ClientPortalUtilities.getPortalGunCoreTexture(link, portal);
            else return ClientPortalUtilities.getPortalGunTexture(link);
        }

        return ApertureInnovations.of( "textures/item/portal_gun.png");
    }

    @Override
    public GeoModel<CubeItem> getGeoModel()
    {
        if(currentItemStack.getItem() instanceof CubeItem item)
        {
            ClientCubeVariant variant = item.getCubeVariant(currentItemStack);
            return variant.
        }
        return super.getGeoModel();
    }



    @Override
    public ResourceLocation getTexture(GeoBone bone, CubeItem animatable)
    {
        boolean active = animatable.isActive();
        int color = active ? animatable.getActiveColor() : animatable.getColor();
        ClientCubeVariant cubeVariant = animatable.getClientVariant();

        ResourceLocation texture = animatable.getClientVariant().idleTexture().orElse(null);
        if(color != -1)
            texture = cubeVariant.genericTexture().orElse(null);

        if(active)
            texture = cubeVariant.activeTexture().orElse(null);

        return texture;
    }

    @Override
    public int getColor(GeoBone bone, CubeItem animatable)
    {
        return 0;
    }

    @Override
    public RenderType getRenderType(GeoBone bone, CubeItem animatable)
    {
        return null;
    }
}
