package net.mistersecret312.aperture_innovations.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderer;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.portal.PortalLink;
import net.mistersecret312.aperture_innovations.portal.PortalLinkData;
import net.mistersecret312.aperture_innovations.portal.PortalPlacement;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Consumer;

public class PortalGunItem extends Item implements GeoItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	protected static class Animations
	{
		protected static final String MAIN_CONTROLLER = "main";

		protected static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
		protected static final RawAnimation RESET = RawAnimation.begin().thenPlay("reset");
		protected static final RawAnimation HOLD = RawAnimation.begin().thenLoop("hold");


		private Animations() {}
	}

	public PortalGunItem(Properties pProperties)
	{
		super(pProperties);

		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected)
	{
		if(level.isClientSide() || !(entity instanceof Player player))
			return;

		if(!isInitialized(stack)) {
			setInitialized(stack, true);
			PortalLinkData data = PortalLinkData.get(level);

			UUID linkID = getUUID(stack);

			PortalLink link = data.getLink(linkID);
			if (link == null) {
				data.addFreshLink(linkID);
				level.playSound(player, player.getEyePosition().x(), player.getEyePosition().y(), entity.getEyePosition().z(), SoundInit.PORTAL_GUN_ACTIVATION.get(), SoundSource.PLAYERS, 1f, 1f);
			}
		}
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
	{
		return true;
	}

	public static BlockHitResult rayTrace(Level level, Player player, double range) {
		float xRot = player.getXRot();
		float yRot = player.getYRot();
		Vec3 eyePos = player.getEyePosition();

		float f2 = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
		float f3 = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
		float f4 = -Mth.cos(-xRot * ((float)Math.PI / 180F));
		float f5 = Mth.sin(-xRot * ((float)Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;

		Vec3 lookVec = new Vec3(f6, f5, f7);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player)
		{
			@Override
			public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos)
			{
				if(state.is(ApertureInnovations.SHOOT_THROUGH))
					return Shapes.empty();

				return super.getBlockShape(state, level, pos);
			}
		};

		return level.clip(new ClipContext(
				eyePos,
				endPos,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				player
		));
	}

	public boolean isLookingAtMoon(Player player, Level level)
	{
		HitResult hit = player.pick(128.0D, 0.0F, false);

		if (hit.getType() == HitResult.Type.BLOCK) {
			return false;
		}

		float timeAngle = level.getSunAngle(0.0F);

		double moonX = Math.sin(timeAngle);
		double moonY = -Math.cos(timeAngle);

		Vec3 moonVector = new Vec3(moonX, moonY, 0);

		Vec3 lookVector = player.getLookAngle();

		double dot = lookVector.dot(moonVector);

		return dot > 0.995;
	}

	public UUID getUUID(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("link"))
		{
			return tag.getUUID("link");
		}
		else
		{
			UUID uuid = UUID.randomUUID();
			setUUID(stack, uuid);
			return uuid;
		}
	}

	public void setUUID(ItemStack stack, UUID uuid)
	{
		stack.getOrCreateTag().putUUID("link", uuid);
	}

	public void setInitialized(ItemStack stack, boolean value)
	{
		stack.getOrCreateTag().putBoolean("init", value);
	}

	public boolean isInitialized(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("init"))
			return tag.getBoolean("init");
		else return false;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private PortalGunRenderer renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new PortalGunRenderer();

				return this.renderer;
			}

			@Override
			public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand,
															  ItemStack itemStack)
			{
				return HumanoidModel.ArmPose.CROSSBOW_HOLD;
			}
		});
	}

	private <T extends PortalGunItem> PlayState handleAnimationState(AnimationState<T> state) {
		return PlayState.STOP;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<PortalGunItem> controller = new AnimationController<>(this,
				Animations.MAIN_CONTROLLER, 0, this::handleAnimationState);
		controller.triggerableAnim("shoot", Animations.SHOOT);
		controller.triggerableAnim("reset", Animations.RESET);
		controller.triggerableAnim("hold", Animations.HOLD);
		controllers.add(controller);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return this.cache;
	}
}
