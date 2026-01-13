package net.mistersecret312.aperture_innovations.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.aperture_innovations.client.Layers;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LongFallBootsModel extends HumanoidModel<LivingEntity>
{
	public static LongFallBootsModel INSTANCE;

	public LongFallBootsModel(ModelPart root)
	{
		super(root);
	}

	public static LayerDefinition createBodyLayer()
	{
		CubeDeformation cubeDeformation = CubeDeformation.NONE;
		float yOffset = 0.0F;

		MeshDefinition meshdefinition = HumanoidModel.createMesh(cubeDeformation, yOffset);
		PartDefinition partdefinition = meshdefinition.getRoot();

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of();
	}

	@SubscribeEvent
	public static void bakeModelLayers(EntityRenderersEvent.AddLayers event)
	{
		EntityModelSet entityModelSet = event.getEntityModels();
		INSTANCE = new LongFallBootsModel(entityModelSet.bakeLayer(Layers.LONG_FALL_BOOTS));
	}
}
