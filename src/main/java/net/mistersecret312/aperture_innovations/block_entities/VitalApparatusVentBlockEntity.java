package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.block_entities.multiblock.MasterBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.multiblock.OrientedMasterBlock;
import net.mistersecret312.aperture_innovations.entities.IFizzle;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.EntityInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.CubeItem;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class VitalApparatusVentBlockEntity extends MasterBlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open");
	protected static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	private boolean isOpen = false;
	private int openingTick = -1;

	private EntityType<?> trackingType = EntityInit.CUBE.get();
	private UUID trackingID = null;
	private CompoundTag trackingData = new CompoundTag();

	private int emptyTime = 0;

	public VitalApparatusVentBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.VITAL_APPARATUS_VENT.get(), pos, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState blockState,
							VitalApparatusVentBlockEntity vent)
	{
		if(vent.emptyTime != -1 && vent.emptyTime < 16)
			vent.emptyTime++;

		Entity trackedEntity = vent.getTrackingEntity(level);
		if(trackedEntity != null)
		{
			CompoundTag tag = trackedEntity.saveWithoutId(new CompoundTag());

			tag.remove("Pos");
			tag.remove("Motion");
			tag.remove("Rotation");
			tag.remove("UUID");
			tag.remove("FallDistance");
			tag.remove("Fire");
			tag.remove("Air");
			tag.remove("OnGround");

			vent.setTrackingData(tag);
		}

		if(vent.isOpen() && vent.getOpeningTick() != -1 && vent.getOpeningTick() < 20)
			vent.setOpeningTick(vent.getOpeningTick()+1);

		if(vent.isOpen() && vent.getOpeningTick() == 16)
		{
			vent.summonTrackingEntity(level);
		}

		if(vent.isOpen())
		{
			if(vent.getBlockState().getBlock() instanceof OrientedMasterBlock masterBlock)
			{
				AABB volume = masterBlock.getMultiblockVolume(level, pos).move(pos);
				List<Entity> entities = level.getEntities((Entity) null, volume, entity -> true);
				for(Entity entity : entities)
				{
					Vec3 normal = Vec3.atLowerCornerOf(
							vent.getBlockState().getValue(OrientedMasterBlock.NORMAL).getNormal());
					if(normal.y >= 0)
						entity.addDeltaMovement(normal.multiply(0.25, 0.6, 0.25));
				}
			}
		}
	}

	@Override
	public void onLoad()
	{
		super.onLoad();

		if(this.isOpen())
			this.triggerAnim("hatch", "open");
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		tag.putBoolean("open", this.isOpen);
		tag.putInt("openTick", this.openingTick);
		if(this.trackingID != null)
			tag.putUUID("tracking_id", this.trackingID);
		if(this.trackingData != null)
			tag.put("tracking_data", this.trackingData);

		tag.putInt("empty_time", this.emptyTime);

		if(this.getTrackingType() != null)
		{
			ResourceLocation location = BuiltInRegistries.ENTITY_TYPE.getKey(getTrackingType());
			tag.putString("tracking_type", location.toString());
		}

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.isOpen = tag.getBoolean("open");
		this.openingTick = tag.getInt("openTick");
		if(tag.contains("tracking_id"))
			this.trackingID = tag.getUUID("tracking_id");
		if(tag.contains("tracking_data"))
			this.trackingData = tag.getCompound("tracking_data");

		this.emptyTime = tag.getInt("empty_time");

		if(tag.contains("tracking_type"))
		{
			String locationString = tag.getString("tracking_type");
			ResourceLocation location = ResourceLocation.parse(locationString);

			this.trackingType = BuiltInRegistries.ENTITY_TYPE.getOptional(location).orElse(EntityInit.CUBE.get());
		}
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	public int getOpeningTick()
	{
		return openingTick;
	}

	public void setOpeningTick(int openingTick)
	{
		this.openingTick = openingTick;
		setChanged();
	}

	public int getEmptyTime()
	{
		return emptyTime;
	}

	public void setEmptyTime(int emptyTime)
	{
		this.emptyTime = emptyTime;
		setChanged();
	}

	public EntityType<?> getTrackingType()
	{
		return trackingType;
	}

	public UUID getTrackingID()
	{
		return trackingID;
	}

	public void setTrackingID(UUID trackingID)
	{
		this.trackingID = trackingID;
	}

	public CompoundTag getTrackingData()
	{
		return trackingData;
	}

	public void setTrackingData(CompoundTag trackingData)
	{
		this.trackingData = trackingData;
		setChanged();
	}

	public Entity getTrackingEntity(Level level)
	{
		if(this.getTrackingID() == null)
			return null;

		if(!(level instanceof ServerLevel serverLevel))
			return null;

		return serverLevel.getEntity(this.getTrackingID());
	}

	public void toggleHatch(boolean state)
	{
		if(level == null)
			return;

		if(this.isOpen == state)
			return;
		this.isOpen = state;
		if(state)
		{
			level.playSound(null, getBlockPos(), SoundInit.VITAL_APPARATUS_VENT_OPEN.get(), SoundSource.BLOCKS, 0.33f, 1f);
			this.triggerAnim("hatch", "open");
			this.openingTick = 0;
		}
		else
		{
			level.playSound(null, getBlockPos(), SoundInit.VITAL_APPARATUS_VENT_CLOSE.get(), SoundSource.BLOCKS, 0.33f, 1f);
			this.triggerAnim("hatch", "close");
			this.openingTick = -1;
		}

		this.setChanged();
	}

	public void toggleHatch()
	{
		if(level == null)
			return;

		boolean isOpen = !isOpen();
		this.toggleHatch(isOpen);
	}

	public void setTrackingType(EntityType<?> type)
	{
		this.trackingType = type;
		this.setTrackingData(new CompoundTag());
	}

	public void setTrackingCube(ItemStack stack)
	{
		this.trackingType = EntityInit.CUBE.get();
		if(stack.getItem() instanceof CubeItem cube)
		{
			CompoundTag tag = new CompoundTag();

			tag.putInt("color", cube.getColor(stack));
			tag.putInt("active_color", cube.getActiveColor(stack));
			tag.putString("variant", cube.getVariant(stack).toString());

			this.setTrackingData(tag);
		}
	}

	public void summonTrackingEntity(Level level)
	{
		Entity entity = getTrackingType().create(level);
		if(entity == null)
			return;

		Vec3 normal = Vec3.atLowerCornerOf(getBlockState().getValue(OrientedMasterBlock.NORMAL).getNormal()).multiply(1, 1.3, 1);
		Vec3 position = getBlockPos().getBottomCenter().add(normal);
		if(getTrackingData() != null && !getTrackingData().isEmpty())
			entity.load(getTrackingData());

		entity.setPos(position);
		entity.setNoGravity(false);
		level.addFreshEntity(entity);
		this.setTrackingID(entity.getUUID());

		this.emptyTime = 0;
	}

	public void fizzleTrackedEntity(Level level)
	{
		Entity entity = getTrackingEntity(level);
		if(entity instanceof IFizzle fizzle)
			fizzle.fizzle();

		if(entity == null)
		{
			if(!(level instanceof ServerLevel serverLevel))
				return;

			Entity trackingEntity = serverLevel.getEntity(getTrackingID());
			if(trackingEntity != null && !(trackingEntity instanceof IFizzle))
				trackingEntity.remove(Entity.RemovalReason.KILLED);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<VitalApparatusVentBlockEntity> controller =
				new AnimationController<>(this, "hatch", 0, this::hatchController);
		controller.triggerableAnim("open", OPEN);
		controller.triggerableAnim("close", CLOSE);
		controllers.add(controller);
	}

	private PlayState hatchController(AnimationState<VitalApparatusVentBlockEntity> animationState)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return this.saveWithoutMetadata(registries);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if(level != null)
			level.markAndNotifyBlock(getBlockPos(), level.getChunkAt(getBlockPos()), getBlockState(), getBlockState(), 3, 512);
	}
}
