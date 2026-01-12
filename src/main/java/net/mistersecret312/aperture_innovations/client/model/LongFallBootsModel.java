package net.mistersecret312.aperture_innovations.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.mistersecret312.aperture_innovations.client.Layers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
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
