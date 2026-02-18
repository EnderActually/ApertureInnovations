package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.AntlineBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.enums.ConnectionState;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;

import java.util.stream.Collectors;

public class AntlineRenderer implements BlockEntityRenderer<AntlineBlockEntity>
{
	public AntlineRenderer(BlockEntityRendererProvider.Context context)
	{

	}

	@Override
	public void render(AntlineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
						   MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		ResourceLocation relayTexture = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "textures/antline/antline_relay_inactive.png");
		ResourceLocation connectionTexture = ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "textures/antline/antline_connection_inactive.png");

		poseStack.pushPose();
		poseStack.mulPose(Axis.XN.rotationDegrees(90));
		poseStack.translate(0.5f, -0.5f, 0.01f);

		poseStack.scale(0.25f, 0.25f, 0.25f);

		Direction normal = blockEntity.getNormal();
		Direction.Axis axis = normal.getAxis();
		boolean positive = normal.getAxisDirection().equals(Direction.AxisDirection.POSITIVE);
		if(axis.equals(Direction.Axis.X))
		{
			poseStack.mulPose(Axis.YP.rotationDegrees(positive ? 90 : -90));
			poseStack.translate(positive ? -2f : 2f, 0f, -1.95f);
		}
		if(axis.equals(Direction.Axis.Y))
		{
			poseStack.mulPose(Axis.XP.rotationDegrees(positive ? 0 : 180));
			poseStack.translate(0f, 0f, positive ? 0 : -3.95f);

		}
		if(axis.equals(Direction.Axis.Z))
		{
			poseStack.mulPose(Axis.XN.rotationDegrees(positive ? -90 : 90));
			poseStack.translate(0f, positive ? 2f : -2f, -1.95f);
		}

		ResourceLocation texture = relayTexture;

		int axises = blockEntity.getConnectedSides().stream().map(Direction::getAxis).distinct().toList().size();
		if(axises > 1)
			texture = connectionTexture;

		VertexConsumer consumer = bufferSource.getBuffer(PortalRenderTypes.antline(texture));

		consumer.addVertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				.setUv(0, 1)
				.setColor(-1);
		consumer.addVertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				.setUv(1, 1)
				.setColor(-1);
		consumer.addVertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				.setUv(1, 0)
				.setColor(-1);
		consumer.addVertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				.setUv(0, 0)
				.setColor(-1);

		consumer = bufferSource.getBuffer(PortalRenderTypes.antline(relayTexture));

		for(Direction direction : Direction.values())
		{
			if(direction.getAxis().equals(blockEntity.getNormal().getAxis()))
				continue;

			if(blockEntity.getState(direction) == ConnectionState.NONE)
				continue;

			poseStack.pushPose();

			poseStack.translate(axis.equals(Direction.Axis.X) ? (positive ? -1f : 1f) * 1.33f*direction.getNormal().getY() : 0F,
					axis.equals(Direction.Axis.Z) ? (positive ? 1f : -1f) * 1.33f*direction.getNormal().getY() : 0F, 0F);

			poseStack.translate(1.33f*direction.getNormal().getX(), (positive ? -1f : 1f ) * 1.33f*direction.getNormal().getZ(), 0);

			consumer.addVertex(poseStack.last().pose(), -0.5f, -0.5f, 0).setUv(0, 1).setColor(-1);
			consumer.addVertex(poseStack.last().pose(), 0.5f, -0.5f, 0).setUv(1, 1).setColor(-1);
			consumer.addVertex(poseStack.last().pose(), 0.5f, 0.5f, 0).setUv(1, 0).setColor(-1);
			consumer.addVertex(poseStack.last().pose(), -0.5f, 0.5f, 0).setUv(0, 0).setColor(-1);

			poseStack.popPose();
		}

		poseStack.popPose();
	}
}
