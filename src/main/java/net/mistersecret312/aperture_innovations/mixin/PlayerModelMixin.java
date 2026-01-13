package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin {

    @Unique private static final float LEG_V_PIVOT = 20f / 64f; // Top of leg in player texture
    @Unique private static final float LEG_CUT_RATIO = 0.5f;    // Show top 50%

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void shortenLegs(net.minecraft.world.entity.LivingEntity entity, float limbSwing, 
                             float limbSwingAmount, float ageInTicks, float netHeadYaw, 
                             float headPitch, CallbackInfo ci) {
        PlayerModel<?> self = (PlayerModel<?>) (Object) this;

		boolean shortypants = shouldShortenLegs(entity);
        if (shortypants) {
            // Scale geometry to half height
            self.leftLeg.yScale = LEG_CUT_RATIO;
            self.rightLeg.yScale = LEG_CUT_RATIO;
            self.leftPants.yScale = LEG_CUT_RATIO;
            self.rightPants.yScale = LEG_CUT_RATIO;
        }

	    // Skip default rendering - we'll render manually with UV scaling
	    self.leftLeg.skipDraw = shortypants;
	    self.rightLeg.skipDraw = shortypants;
	    self.leftPants.skipDraw = shortypants;
	    self.rightPants.skipDraw = shortypants;
    }

    @Unique
    private boolean shouldShortenLegs(net.minecraft.world.entity.LivingEntity entity) {
        // Your condition here
        return true;
    }
}