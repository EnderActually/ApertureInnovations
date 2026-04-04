package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.model.WeightedStorageCubeModel;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

import java.awt.*;

public class WeightedStorageCubeRenderer extends DynamicGeoEntityRenderer<WeightedStorageCubeEntity> implements IDynamicTexture<WeightedStorageCubeEntity>
{
	public WeightedStorageCubeRenderer(EntityRendererProvider.Context context)
	{
		super(context, new WeightedStorageCubeModel());
		this.addRenderLayer(new ColoredGlowingLayer<>(this));
	}

	@Override
	public software.bernie.geckolib.util.Color getRenderColor(WeightedStorageCubeEntity animatable, float partialTick,
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
	public void preRender(PoseStack poseStack, WeightedStorageCubeEntity animatable, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, colour);

		poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
	}

	@Override
	public @Nullable RenderType getRenderType(WeightedStorageCubeEntity animatable, ResourceLocation texture,
											  @Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}

	@Override
	public ResourceLocation getTexture(GeoBone bone, WeightedStorageCubeEntity animatable)
	{
		boolean active = animatable.isActive();
		int color = active ? animatable.getActiveColor() : animatable.getColor();

		if(color != -1)
			return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
				"textures/entity/weighted_cube_generic.png");
		else
			return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
				"textures/entity/weighted_cube_" + (active ? "active" : "inactive") + ".png");
	}

	@Override
	public int getColor(GeoBone bone, WeightedStorageCubeEntity animatable)
	{
		boolean active = this.getAnimatable().isActive();
		int color = active ? this.getAnimatable().getActiveColor() : this.getAnimatable().getColor();

		return new Color(color, false).getRGB();
	}

	@Override
	public RenderType getRenderType(GeoBone bone, WeightedStorageCubeEntity animatable)
	{
		return PortalRenderTypes.APERTURE_GLOW.apply(getTexture(bone, animatable), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
	}
}
