package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.model.WeightedCompanionCubeModel;
import net.mistersecret312.aperture_innovations.client.model.WeightedStorageCubeModel;
import net.mistersecret312.aperture_innovations.entities.WeightedCompanionCubeEntity;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.awt.*;

public class WeightedCompanionCubeRenderer extends DynamicGeoEntityRenderer<WeightedCompanionCubeEntity>
{
	public WeightedCompanionCubeRenderer(EntityRendererProvider.Context context)
	{
		super(context, new WeightedCompanionCubeModel());
	}

	@Override
	protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource,
										 VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay,
										 int colour)
	{
		boolean active = this.getAnimatable().isActive();
		int color = active ? this.getAnimatable().getActiveColor() : this.getAnimatable().getColor();

		if(bone.getName().equals("ColoredCircle") && color != -1)
		{
			Color colorD = new Color(color, false);
			colour = colorD.getRGB();

			renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);
			return true;
		}

		return super.boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay,
				colour);
	}

	@Override
	protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone, WeightedCompanionCubeEntity animatable,
																   float partialTick)
	{
		boolean active = animatable.isActive();
		int color = active ? animatable.getActiveColor() : animatable.getColor();
		if(bone.getName().equals("ColoredCircle"))
		{
			if(color != -1)
				return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "textures/entity/weighted_cube_generic.png");
			else return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "textures/entity/weighted_cube_" + (active ? "active" : "companion_glow") + ".png");
		}

		return super.getTextureOverrideForBone(bone, animatable, partialTick);
	}

	@Override
	protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, WeightedCompanionCubeEntity animatable,
																ResourceLocation texturePath,
																MultiBufferSource bufferSource, float partialTick)
	{
		if(bone.getName().equals("ColoredCircle"))
			return PortalRenderTypes.APERTURE_GLOW.apply(getTextureOverrideForBone(bone, animatable, partialTick), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
		return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
	}

	@Override
	public void preRender(PoseStack poseStack, WeightedCompanionCubeEntity animatable, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, colour);

		poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getYRot()));
	}

	@Override
	public void renderRecursively(PoseStack poseStack, WeightedCompanionCubeEntity animatable, GeoBone bone, RenderType renderType,
								  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
								  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		poseStack.pushPose();
		RenderUtil.translateMatrixToBone(poseStack, bone);
		RenderUtil.translateToPivotPoint(poseStack, bone);
		RenderUtil.rotateMatrixAroundBone(poseStack, bone);
		RenderUtil.scaleMatrixForBone(poseStack, bone);

		if (bone.isTrackingMatrices()) {
			Matrix4f poseState = new Matrix4f(poseStack.last().pose());
			Matrix4f localMatrix = RenderUtil.invertAndMultiplyMatrices(poseState, this.entityRenderTranslations);

			bone.setModelSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
			localMatrix.translate(new Vector3f(getRenderOffset(this.animatable, 1).toVector3f()));
			bone.setLocalSpaceMatrix(localMatrix);

			Matrix4f worldState = new Matrix4f(localMatrix);

			worldState.translate(new Vector3f(this.animatable.position().toVector3f()));
			bone.setWorldSpaceMatrix(worldState);
		}

		RenderUtil.translateAwayFromPivotPoint(poseStack, bone);

		this.textureOverride = getTextureOverrideForBone(bone, this.animatable, partialTick);
		ResourceLocation texture = this.textureOverride == null ? getTextureLocation(this.animatable) : this.textureOverride;
		RenderType renderTypeOverride = getRenderTypeOverrideForBone(bone, this.animatable, texture, bufferSource, partialTick);

		if (texture != null && renderTypeOverride == null)
			renderTypeOverride = getRenderType(this.animatable, texture, bufferSource, partialTick);

		if (renderTypeOverride != null)
			buffer = bufferSource.getBuffer(renderTypeOverride);

		if (!boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour))
			super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, colour);

		if (renderTypeOverride != null)
			buffer = bufferSource.getBuffer(renderType);

		if (!isReRender)
			applyRenderLayersForBone(poseStack, animatable, bone, renderTypeOverride, bufferSource, buffer, partialTick, packedLight, packedOverlay);

		buffer = checkAndRefreshBuffer(isReRender, buffer, bufferSource, renderTypeOverride);

		super.renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

		poseStack.popPose();
	}
}
