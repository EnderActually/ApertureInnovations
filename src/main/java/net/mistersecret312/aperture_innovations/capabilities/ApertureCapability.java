package net.mistersecret312.aperture_innovations.capabilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.UUID;

public class ApertureCapability implements INBTSerializable<CompoundTag>
{
	public Vector3d distanceVec;
	public Pair<UUID, Boolean> portal;

	public double horizontalDistance;
	public double verticalDistance;

	public void tick(Level level, LivingEntity living)
	{
		if(level.isClientSide())
			return;

		if(living.onGround())
		{
			portal = null;
			this.distanceVec = new Vector3d();
			this.horizontalDistance = 0;
			this.verticalDistance = 0;
		}
		else if(portal != null)
		{
			Vec3 speed = living.getDeltaMovement();
			Vector3f movementVector = new Vector3f(speed.x > 0 ? (float) speed.x : (float) -speed.x,
					speed.y > 0 ? (float) speed.y : (float) -speed.y, speed.z > 0 ? (float) speed.z : (float) -speed.z);
			this.distanceVec.add(movementVector);
		}
	}

	public void updateDistance()
	{
		if(portal == null || this.distanceVec == null)
			return;

		if(distanceVec.lengthSquared() > 0)
		{
			horizontalDistance += Mth.sqrt((float) (Math.pow(distanceVec.x, 2) + Math.pow(distanceVec.z, 2)));
			verticalDistance += Math.sqrt(Math.pow(distanceVec.y, 2));
		}

		distanceVec = new Vector3d();
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();

		CompoundTag distanceTag = new CompoundTag();
		distanceTag.putDouble("x", distanceVec.x);
		distanceTag.putDouble("y", distanceVec.y);
		distanceTag.putDouble("z", distanceVec.z);

		tag.put("distance", distanceTag);
		tag.putDouble("horizontal", horizontalDistance);
		tag.putDouble("vertical", verticalDistance);

		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		Vector3d distanceVec = new Vector3d();

		CompoundTag distanceTag = nbt.getCompound("distance");
		distanceVec.x = distanceTag.getDouble("x");
		distanceVec.y = distanceTag.getDouble("y");
		distanceVec.z = distanceTag.getDouble("z");

		this.distanceVec = distanceVec;

		this.horizontalDistance = nbt.getDouble("horizontal");
		this.verticalDistance = nbt.getDouble("vertical");
	}
}
