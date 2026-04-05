package net.mistersecret312.aperture_innovations.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.PedestalButtonBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.VitalApparatusVentBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.multiblock.OrientedMasterBlock;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.model.VitalApparatusVentModel;
import net.mistersecret312.aperture_innovations.client.renderer.ColoredGlowingLayer;
import net.mistersecret312.aperture_innovations.client.renderer.IDynamicTexture;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VitalApparatusVentRenderer extends DynamicGeoBlockRenderer<VitalApparatusVentBlockEntity> implements IDynamicTexture<VitalApparatusVentBlockEntity>
{
	private final Map<EntityType<?>, Entity> ENTITIES = new HashMap<>();

	public VitalApparatusVentRenderer(BlockEntityRendererProvider.Context context)
	{
		super(new VitalApparatusVentModel());
		this.addRenderLayer(new ColoredGlowingLayer<>(this));
	}

	@Override
	public @Nullable RenderType getRenderType(VitalApparatusVentBlockEntity animatable, ResourceLocation texture,
											  @Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}

	@Override
	public void preRender(PoseStack poseStack, VitalApparatusVentBlockEntity vent, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, int colour)
	{
		super.preRender(poseStack, vent, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, colour);
		Direction down = vent.getBlockState().getValue(OrientedMasterBlock.NORMAL).getOpposite();
		poseStack.mulPose(down.getRotation());

		if(down.equals(Direction.UP))
			poseStack.translate(0, -1.125, 0);
		else if(down.equals(Direction.DOWN))
			poseStack.translate(0, -2.125, 0);
		else
			poseStack.translate(0, -1.625, -0.5);

		Minecraft mc = Minecraft.getInstance();
		Level level = vent.getLevel();
		if(level == null)
			return;

		Entity entity = vent.getTrackingType().create(level);
		if(entity == null)
			return;

		if(!vent.getTrackingData().isEmpty())
			entity.load(vent.getTrackingData());

		if(bufferSource != null)
		{
			poseStack.pushPose();

			float y = Mth.lerp(vent.getEmptyTime() / 16f, 1f, -0.25f);

			poseStack.translate(0, y, 0);

			mc.getEntityRenderDispatcher()
			  .render(entity, 0, 0, 0, 0, partialTick, poseStack, bufferSource, packedLight);

			poseStack.popPose();
		}
	}

	@Override
	protected void rotateBlock(Direction facing, PoseStack poseStack)
	{

	}

	@Override
	public ResourceLocation getTexture(GeoBone bone, VitalApparatusVentBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID,
			"textures/entity/vital_apparatus_vent/vital_apparatus_vent_"
					+ (animatable.isOpen() ? "active" : "inactive") + ".png");
	}

	@Override
	public int getColor(GeoBone bone, VitalApparatusVentBlockEntity animatable)
	{
		return -1;
	}

	@Override
	public RenderType getRenderType(GeoBone bone, VitalApparatusVentBlockEntity animatable)
	{
		return PortalRenderTypes.APERTURE_GLOW.apply(getTexture(bone, animatable), RenderStateShard.TRANSLUCENT_TRANSPARENCY);
	}
}
