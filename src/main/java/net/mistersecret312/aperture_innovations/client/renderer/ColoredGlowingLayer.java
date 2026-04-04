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
				if(texture.getTexture(bone, animatable) == null)
					return;
				renderType = texture.getRenderType(bone, animatable);
				renderColoredCube(poseStack, bone, bufferSource.getBuffer(renderType), packedLight,
						packedOverlay, texture.getColor(bone, animatable));
			}
		}
	}

	public void renderColoredCube(PoseStack poseStack, GeoBone bone, VertexConsumer buffer,
								  int packedLight, int packedOverlay, int color)
	{
		getRenderer().renderCubesOfBone(poseStack, bone, buffer,
				packedLight, packedOverlay, color);

		if(bone.isHidingChildren())
			return;

		for(GeoBone childBone : bone.getChildBones())
		{
			renderColoredCube(poseStack, childBone, buffer,
					packedLight, packedOverlay, color);
		}
	}
}
