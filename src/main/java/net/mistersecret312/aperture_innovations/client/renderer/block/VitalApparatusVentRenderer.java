package net.mistersecret312.aperture_innovations.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.PedestalButtonBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.VitalApparatusVentBlockEntity;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.model.VitalApparatusVentModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class VitalApparatusVentRenderer extends DynamicGeoBlockRenderer<VitalApparatusVentBlockEntity>
{
	public VitalApparatusVentRenderer(BlockEntityRendererProvider.Context context)
	{
		super(new VitalApparatusVentModel());
	}

	@Override
	protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource,
										 VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay,
										 int colour)
	{
		return super.boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
	}

	@Override
	protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone,
																   VitalApparatusVentBlockEntity animatable,
																   float partialTick)
	{
		if(bone.getName().equals("GlowingStuff"))
		{
			return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
					"textures/entity/vital_apparatus_vent/vital_apparatus_vent_"
							+ (animatable.isOpen() ? "active" : "inactive") + ".png");
		}

		return super.getTextureOverrideForBone(bone, animatable, partialTick);
	}

	@Override
	protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, VitalApparatusVentBlockEntity animatable,
																ResourceLocation texturePath,
																MultiBufferSource bufferSource, float partialTick)
	{
		if(bone.getName().equals("GlowingStuff"))
		{
			return PortalRenderTypes.APERTURE_GLOW.apply(getTextureOverrideForBone(bone, animatable, partialTick),
					RenderStateShard.TRANSLUCENT_TRANSPARENCY);
		}

		return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
	}

	@Override
	public @Nullable RenderType getRenderType(VitalApparatusVentBlockEntity animatable, ResourceLocation texture,
											  @Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}

	@Override
	public void preRender(PoseStack poseStack, VitalApparatusVentBlockEntity animatable, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, colour);
		poseStack.translate(0, -1, 0);
	}

	@Override
	public void renderRecursively(PoseStack poseStack, VitalApparatusVentBlockEntity animatable, GeoBone bone,
								  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
								  boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour)
	{
		poseStack.pushPose();
		RenderUtil.translateMatrixToBone(poseStack, bone);
		RenderUtil.translateToPivotPoint(poseStack, bone);
		RenderUtil.rotateMatrixAroundBone(poseStack, bone);
		RenderUtil.scaleMatrixForBone(poseStack, bone);

		if (bone.isTrackingMatrices()) {
			Matrix4f poseState = new Matrix4f(poseStack.last().pose());
			Matrix4f localMatrix = RenderUtil.invertAndMultiplyMatrices(poseState, this.blockRenderTranslations);
			Matrix4f worldState = new Matrix4f(localMatrix);
			BlockPos pos = this.animatable.getBlockPos();

			bone.setModelSpaceMatrix(RenderUtil.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
			bone.setLocalSpaceMatrix(localMatrix);
			bone.setWorldSpaceMatrix(worldState.translate(new Vector3f(pos.getX(), pos.getY(), pos.getZ())));
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

	@Override
	protected void rotateBlock(Direction facing, PoseStack poseStack)
	{

	}
}
