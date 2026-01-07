package net.mistersecret312.aperture_innovations.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.advancements.ThrownIntoFluidCriterion;
import net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderer;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;
import net.mistersecret312.aperture_innovations.portal.*;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
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
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components,
								TooltipFlag flag)
	{
		ClientPortalLink link = PortalUtilities.getPortalLinks().get(getUUID(stack, false));
		if(link != null) components.add(
				Component.translatable("aperture_innovations.portal_gun.variant_" + link.variantKey().getPath()).withStyle(
						ChatFormatting.DARK_AQUA));

	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected)
	{

		if(level.isClientSide() || !(entity instanceof Player player))
			return;

		if(!isInitialized(stack)) {
			setInitialized(stack, true);
			PortalLinkData data = PortalLinkData.get(level);

			UUID linkID = getUUID(stack, true);

			PortalLink link = data.getLink(linkID);
			if (link == null) {
				data.addFreshLink(linkID);

				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())), new ClientboundPortalSoundsPacket.GunActivate(linkID, player.blockPosition()));
			}
		}
		else
		{
			PortalLink link = PortalUtilities.getPortalLinks(level).get(getUUID(stack, false));
			if(link != null && isSelected)
			{
				link.updateColors(level, getPrimaryPortalColor(stack), getSecondaryPortalColor(stack));
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity)
	{
		if(entity.isInFluidType())
		{
			Entity owner = entity.getOwner();
			if(owner instanceof Player player)
				ThrownIntoFluidCriterion.INSTANCE.trigger((ServerPlayer) (player), stack, entity.blockPosition());
		}
		return super.onEntityItemUpdate(stack, entity);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player)
	{
		return super.onDroppedByPlayer(item, player);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return slotChanged;
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

		return level.clip(context);
	}

	public boolean isLookingAtMoon(Player player, Level level)
	{
		HitResult hit = player.pick(256D, 0.0F, false);

		if (hit.getType() != HitResult.Type.MISS)
			return false;

		float timeAngle = level.getSunAngle(0.0F);

		double moonX = Math.sin(timeAngle);
		double moonY = -Math.cos(timeAngle);

		Vec3 moonVector = new Vec3(moonX, moonY, 0);

		Vec3 lookVector = player.getLookAngle();

		double dot = lookVector.dot(moonVector);

		return dot > 0.995;
	}

	public UUID getUUID(ItemStack stack, boolean generateIfEmpty)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("link"))
			return tag.getUUID("link");
		else if(generateIfEmpty)
		{
			UUID uuid = UUID.randomUUID();
			setUUID(stack, uuid);
			return uuid;
		}

		return null;
	}

	public void setUUID(ItemStack stack, UUID uuid)
	{
		stack.getOrCreateTag().putUUID("link", uuid);
	}

	public int getPrimaryStripeColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("primaryStripeColor"))
			return tag.getInt("primaryStripeColor");
		else return -1;
	}

	public int getSecondaryStripeColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("secondaryStripeColor"))
			return tag.getInt("secondaryStripeColor");
		else return -1;
	}

	public int getPrimaryPortalColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("primaryPortalColor"))
			return tag.getInt("primaryPortalColor");
		else return -1;
	}

	public int getSecondaryPortalColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("secondaryPortalColor"))
			return tag.getInt("secondaryPortalColor");
		else return -1;
	}

	public void setPrimaryStripeColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("primaryStripeColor", color);
	}

	public void setSecondaryStripeColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("secondaryStripeColor", color);
	}

	public void setPrimaryPortalColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("primaryPortalColor", color);
	}

	public void setSecondaryPortalColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("secondaryPortalColor", color);
	}

	public int getLastShotPortal(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("lastPortal"))
			return tag.getInt("lastPortal");
		else return -1;
	}

	public void setLastShotPortal(ItemStack stack, int portal)
	{
		stack.getOrCreateTag().putInt("lastPortal", portal);
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
