package net.mistersecret312.aperture_innovations.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.mistersecret312.aperture_innovations.init.AttachmentTypeInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
	@ModifyExpressionValue(method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
	at = @At(value = "CONSTANT", args = "floatValue=0.91"))
	public float dontAir(float original)
	{
		LivingEntity living = (LivingEntity) (Object) this;
		int frictionlessTime = living.getData(AttachmentTypeInit.APERTURE).frictionlessTime;
		if(frictionlessTime > 0)
			return 0.95F;

		return original;
	}
}
