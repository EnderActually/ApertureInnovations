package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ColoredGlowingLayer<T extends GeoAnimatable> extends AutoGlowingGeoLayer<T>
{

	public ColoredGlowingLayer(GeoRenderer<T> renderer)
	{
		super(renderer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
					   MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick,
					   int packedLight, int packedOverlay)
	{
		if(getRenderer() instanceof IDynamicTexture)
		{
			IDynamicTexture<T> texture = ((IDynamicTexture<T>) getRenderer());
			for(GeoBone bone : bakedModel.topLevelBones())
			{
				renderColoredCube(texture, animatable, poseStack, bone, bufferSource, packedLight,
						packedOverlay);
			}
		}
	}

	public void renderColoredCube(IDynamicTexture<T> texture, T animatable, PoseStack poseStack,
								  GeoBone bone, MultiBufferSource bufferSource,
								  int packedLight, int packedOverlay)
	{
		if(texture.getTexture(bone, animatable) != null)
			getRenderer().renderCubesOfBone(poseStack, bone,
					bufferSource.getBuffer(texture.getRenderType(bone, animatable)), packedLight,
					packedOverlay, texture.getColor(bone, animatable));

		if(bone.isHidingChildren())
			return;

		for(GeoBone childBone : bone.getChildBones())
		{
			renderColoredCube(texture, animatable, poseStack, childBone, bufferSource,
					packedLight, packedOverlay);
		}
	}
}
