package net.mistersecret312.aperture_innovations.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.model.CubeModel;
import net.mistersecret312.aperture_innovations.client.renderer.ColoredGlowingLayer;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientCubeVariant;
import net.mistersecret312.aperture_innovations.entities.CubeEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

import java.awt.*;

public class CubeRenderer extends DynamicGeoEntityRenderer<CubeEntity>
{
	public CubeRenderer(EntityRendererProvider.Context context)
	{
		super(context, new CubeModel());
		this.addRenderLayer(new ColoredGlowingLayer<>(this,
				(cube, bone) -> getTexture(bone, cube),
				(cube, bone) -> getColor(bone, cube),
				(cube, bone) -> getRenderType(bone, cube)
		));
	}

	@Override
	public software.bernie.geckolib.util.Color getRenderColor(CubeEntity animatable, float partialTick,
															  int packedLight)
	{
		if(animatable.getFizzlingTick() != -1)
		{
			float delta = (this.getAnimatable().getFizzlingTick() - 30f) /
								  (this.getAnimatable().getMaxFizzleTime() - 30f);

			float colorDelta = this.getAnimatable().getFizzlingTick()/10f;
			float color = Mth.clampedLerp(1f, 0f, colorDelta);

			return software.bernie.geckolib.util.Color.ofARGB(Mth.clampedLerp(1f, 0f, delta),
					color, color, color);
		}
		return super.getRenderColor(animatable, partialTick, packedLight);
	}

	@Override
	public void preRender(PoseStack poseStack, CubeEntity animatable, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, colour);

		poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
	}

	@Override
	public @Nullable RenderType getRenderType(CubeEntity animatable, ResourceLocation texture,
											  @Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}

	public ResourceLocation getTexture(GeoBone bone, CubeEntity animatable)
	{
		boolean active = animatable.isActive();
		int color = active ? animatable.getActiveColor().packagedInt() : animatable.getColor().packagedInt();
		ClientCubeVariant cubeVariant = animatable.getClientVariant();
		if(!bone.getName().equals("ColoredCircle"))
			return null;

		ResourceLocation texture = animatable.getClientVariant().idleTexture().orElse(null);
		if(color != 0)
			texture = cubeVariant.genericTexture().orElse(null);

		if(active)
			texture = cubeVariant.activeTexture().orElse(null);

		return texture;
	}

	public int getColor(GeoBone bone, CubeEntity animatable)
	{
		boolean active = this.getAnimatable().isActive();
		int color = active ? this.getAnimatable().getActiveColor().packagedInt() : this.getAnimatable().getColor().packagedInt();

		if(color != 0)
			return new Color(color, false).getRGB();

		return -1;
	}

	public RenderType getRenderType(GeoBone bone, CubeEntity animatable)
	{
		return PortalRenderTypes.APERTURE_GLOW.apply(getTexture(bone, animatable), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
	}
}
