package net.mistersecret312.aperture_innovations.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.LargeButtonBlock;
import net.mistersecret312.aperture_innovations.client.model.LargeButtonModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DynamicGeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.awt.*;
import java.util.List;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderer.GLOWING_RENDER_TYPE;

public class LargeButtonRenderer extends DynamicGeoBlockRenderer<LargeButtonBlockEntity>
{
	public LargeButtonRenderer(BlockEntityRendererProvider.Context context)
	{
		super(new LargeButtonModel());
	}

	@Override
	protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource,
										 VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay,
										 int colour)
	{
		if(bone.getName().equals("ColoredLines"))
		{
			Color color = new Color(this.animatable.color, false);
			renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color.getRGB());
			return true;
		}
		if(bone.getName().equals("Button"))
		{
			Color color = new Color(this.animatable.buttonColor, false);
			renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color.getRGB());
			return true;
		}

		return super.boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay,
				colour);
	}

	@Override
	protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone, LargeButtonBlockEntity animatable,
																   float partialTick)
	{
		if(bone.getName().equals("ColoredLines"))
		{
			int color = animatable.color;
			if(color == -1)
			{
				if(animatable.getBlockState().getValue(LargeButtonBlock.PRESSED))
					return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
							"textures/block/large_button/large_button_lines_active.png");
				else return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
						"textures/block/large_button/large_button_lines_inactive.png");
			}
			else return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
						"textures/block/large_button/large_button_lines_generic.png");

		}
		if(bone.getName().equals("Button"))
		{
			int color = animatable.buttonColor;
			if(color != -1)
				return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
						"textures/block/large_button/large_button_button_generic.png");

			return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
					"textures/block/large_button/large_button_button.png");
		}
		return super.getTextureOverrideForBone(bone, animatable, partialTick);
	}

	@Override
	protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, LargeButtonBlockEntity animatable,
																ResourceLocation texturePath,
																MultiBufferSource bufferSource, float partialTick)
	{
		List<String> glows = Lists.newArrayList("ColoredLines", "Button");
		if(glows.contains(bone.getName()))
			return GLOWING_RENDER_TYPE.apply(getTextureOverrideForBone(bone, animatable, partialTick), false);

		return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
	}

	@Override
	public void preRender(PoseStack poseStack, LargeButtonBlockEntity animatable, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, colour);

		Direction facing = animatable.getBlockState().getValue(LargeButtonBlock.FACING);
		Direction normal = animatable.getBlockState().getValue(LargeButtonBlock.NORMAL);
		boolean positive = normal.getAxisDirection().equals(Direction.AxisDirection.POSITIVE);

		if(normal.getAxis().isVertical())
		{
			if(!positive)
			{
				poseStack.translate(0f, 1f, 0f);
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				if(facing.getAxis().equals(Direction.Axis.X))
					poseStack.mulPose(Axis.YP.rotationDegrees(180));
			}
		}
		if(normal.getAxis().isHorizontal())
		{
			if(normal.getAxis().equals(Direction.Axis.X))
			{
				poseStack.translate(positive ? -0.5f : 0.5f, 0.5f, 0f);

				poseStack.mulPose(Axis.ZP.rotationDegrees(normal.toYRot()));
				poseStack.mulPose(Axis.YP.rotationDegrees(positive ? 180 : 0));
			}
			if(normal.getAxis().equals(Direction.Axis.Z))
			{
				poseStack.translate(0f, 0.5f, positive ? -0.5f : 0.5f);
				poseStack.mulPose(Axis.ZP.rotationDegrees(90));
				poseStack.mulPose(Axis.XP.rotationDegrees(positive ? 90 : -90));
			}

			if(facing.getAxis().isVertical())
			{
				boolean posFace = facing.getAxisDirection().equals(Direction.AxisDirection.POSITIVE);
				poseStack.mulPose(Axis.YP.rotationDegrees(posFace ? 0 :180));
			}
		}

		poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getBlockState().getValue(LargeButtonBlock.FACING).toYRot()));
		if(facing.getAxis().equals(Direction.Axis.X))
			poseStack.mulPose(Axis.YP.rotationDegrees(180));
	}

	@Override
	public void renderRecursively(PoseStack poseStack, LargeButtonBlockEntity animatable, GeoBone bone,
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


}
