package net.mistersecret312.aperture_innovations.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.mistersecret312.aperture_innovations.init.AttachmentTypeInit;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
	@ModifyExpressionValue(method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
	at = @At(value = "CONSTANT", args = "floatValue=0.91"))
	public float dontAir(float original)
	{
		LivingEntity living = (LivingEntity) (Object) this;
		int frictionlessTime = living.getData(AttachmentTypeInit.APERTURE).frictionlessTime;
		if(frictionlessTime > 0 && !living.onGround())
			return 0.98F;

		return original;
	}

	@ModifyArg(method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
	at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
	private float modifyFrictionValue(float friction)
	{
		LivingEntity living = (LivingEntity) (Object) this;
		int frictionlessTime = living.getData(AttachmentTypeInit.APERTURE).frictionlessTime;
		if(frictionlessTime > 0 && !living.onGround())
			friction = 0.98F;
		return friction;
	}

}
