package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	@Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
	private void cameraTarget(CallbackInfoReturnable<RenderTarget> cir)
	{
		if(PortalRenderer.LIVE_FEED_BEING_RENDERED != null)
			cir.setReturnValue(PortalRenderer.LIVE_FEED_BEING_RENDERED);
	}
}
