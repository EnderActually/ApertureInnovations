package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.DyeableGeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PortalGunRenderer extends GeoItemRenderer<PortalGunItem>
{
	public PortalGunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new ResourceLocation(ApertureInnovations.MODID, "portal_gun")));
	}

	@Override
	public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight,
								  int packedOverlay, float red, float green, float blue, float alpha)
	{
		if(bone.getName().equals("Back"))
		{
			red *= 1;
			blue *= 1;
			green *= 1;
			alpha *= 1;
		}
		super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public RenderType getRenderType(PortalGunItem animatable, ResourceLocation texture,
									@Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}
}
