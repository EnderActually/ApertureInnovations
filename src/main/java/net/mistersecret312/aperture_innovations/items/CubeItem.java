package net.mistersecret312.aperture_innovations.items;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientCubeVariant;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientCubeVariants;
import net.mistersecret312.aperture_innovations.datapack.CubeVariant;
import net.mistersecret312.aperture_innovations.entities.CubeEntity;
import net.mistersecret312.aperture_innovations.init.DataComponentInit;
import net.mistersecret312.aperture_innovations.init.EntityInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.awt.*;
import java.util.List;

public class CubeItem extends Item implements GeoItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public CubeItem(Properties properties)
	{
		super(properties);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	public static DispenseItemBehavior getDispenserBehaviour()
	{
		return (blockSource, item) ->
			{
				if(!(item.getItem() instanceof CubeItem cubeItem))
					return item;

				Level level = blockSource.level();
				Vec3 dirVec = Vec3.atLowerCornerOf(blockSource.state().getValue(DispenserBlock.FACING).getNormal());
				EntityType<?> entitytype = EntityInit.CUBE.get();
				Entity entity = entitytype.spawn((ServerLevel)level, null, null,
						BlockPos.containing(blockSource.center().add(dirVec)), MobSpawnType.SPAWN_EGG,
						true, false);
				if (entity instanceof CubeEntity cube)
				{
					cube.setDeltaMovement(
							dirVec.multiply(0.5, dirVec.y < 0 ? 0 : 0.5, 0.5));

					cube.setVariantKey(cubeItem.getVariant(item));
					cube.setColor(cubeItem.getColor(item));
					cube.setActiveColor(cubeItem.getActiveColor(item));

					item.shrink(1);
					return item;
				}
				return item;
			};
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components,
								TooltipFlag tooltipFlag)
	{
		super.appendHoverText(stack, context, components, tooltipFlag);

		Level level = context.level();
		if(level != null)
		{
			Color hsbColor = Color.getHSBColor(level.getTimeOfDay(1f)*50, 1f, 1f);
			components.add(Component.translatable("tooltip.aperture_innovations.is_colorable").withStyle((style -> style.withColor(
					hsbColor.getRGB()))));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level level = context.getLevel();
		if(level.isClientSide())
			return InteractionResult.SUCCESS;

		ItemStack itemstack = context.getItemInHand();
		if(!(itemstack.getItem() instanceof CubeItem item))
			return InteractionResult.FAIL;

		BlockPos blockpos = context.getClickedPos();
		Direction direction = context.getClickedFace();
		BlockState blockstate = level.getBlockState(blockpos);
		BlockPos blockpos1;
		if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
			blockpos1 = blockpos;
		} else {
			blockpos1 = blockpos.relative(direction);
		}

		EntityType<?> entitytype = EntityInit.CUBE.get();
		Entity spawned = entitytype.spawn((ServerLevel)level, itemstack, context.getPlayer(),
				blockpos1, MobSpawnType.SPAWN_EGG, true, false);
		if (spawned != null)
		{
			if(spawned instanceof CubeEntity cube)
			{
				cube.setVariantKey(item.getVariant(itemstack));
				cube.setColor(item.getColor(itemstack));
				cube.setActiveColor(item.getActiveColor(itemstack));
			}

			itemstack.shrink(1);
			level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
		}

		return InteractionResult.CONSUME;
	}

	public ResourceLocation getVariant(ItemStack stack)
	{
		return stack.getOrDefault(DataComponentInit.VARIANT,
				ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "weighted_storage_cube"));
	}

	@OnlyIn(Dist.CLIENT)
	public ClientCubeVariant getCubeVariant(ItemStack stack)
	{
		CubeVariant variant =  Minecraft.getInstance().level.registryAccess()
											.registryOrThrow(CubeVariant.REGISTRY_KEY)
											.get(getVariant(stack));

		if(variant != null)
			return ClientCubeVariants.getCubeVariant(variant.getClientVariant());

		return ClientCubeVariant.DEFAULT_VARIANT;
	}

	public void setVariant(ItemStack stack, ResourceLocation location)
	{
		stack.set(DataComponentInit.VARIANT, location);
	}

	public Integer getColor(ItemStack stack)
	{
		return stack.get(DataComponentInit.COLOR);
	}

	public void setColor(ItemStack stack, int color)
	{
		stack.set(DataComponentInit.COLOR, color);
	}

	public Integer getActiveColor(ItemStack stack)
	{
		return stack.get(DataComponentInit.ACTIVE_COLOR);
	}

	public void setActiveColor(ItemStack stack, int color)
	{
		stack.set(DataComponentInit.ACTIVE_COLOR, color);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{

	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
