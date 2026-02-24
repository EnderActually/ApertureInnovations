package net.mistersecret312.aperture_innovations.capabilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.ClientboundApertureCapabilityPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundEntityHeldUpdatePacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HoldEntityCapability implements INBTSerializable<CompoundTag>
{
	public static final EntityCapability<HoldEntityCapability, Void> APERTURE_CAPABILITY = EntityCapability.createVoid(
		ResourceLocation.fromNamespaceAndPath(ApertureInnovations.MODID, "hold_entity"), HoldEntityCapability.class);

	public boolean isHeld = false;

	public void tick(Level level, Entity entity)
	{
		if(!isHeld)
			return;

		Player player = findHoldingPlayer(level, entity);
		if(player == null)
		{
			this.setHeld(entity, false);
			entity.setNoGravity(false);
			return;
		}

		entity.setNoGravity(true);
		entity.resetFallDistance();

		Vec3 targetPos = player.getEyePosition().add(player.getViewVector(1F).multiply(3f, 3f, 3f));
		Vec3 offset = entity.getBoundingBox().getCenter().vectorTo(targetPos);

		entity.setOldPosAndRot();
		entity.setYRot(-player.getYRot());
		entity.setDeltaMovement(offset);
	}

	public Player findHoldingPlayer(Level level, Entity entity)
	{
		AABB box = new AABB(entity.blockPosition()).inflate(4);
		List<Player> players = new ArrayList<>();
		for(Player player : level.players())
		{
			if(box.contains(player.position()))
				players.add(player);
		}

		Player holdingPlayer = null;
		for(Player player : players)
		{
			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if(!hasPortalGun)
				continue;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			Integer id = portalGun.getHeldEntity(gunStack);
			if(id == null)
				continue;

			if(id.equals(entity.getId()))
			{
				holdingPlayer = player;
				break;
			}
		}

		return holdingPlayer;
	}

	public void setHeld(Entity entity, boolean held)
	{
		if(!entity.level().isClientSide())
		{
			this.isHeld = held;
			PacketDistributor.sendToAllPlayers(new ClientboundEntityHeldUpdatePacket(entity.getId(), held));
		}
		else this.isHeld = held;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider)
	{
		CompoundTag tag = new CompoundTag();

		tag.putBoolean("isHeld", this.isHeld);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
	{
		this.isHeld = nbt.getBoolean("isHeld");
	}
}
