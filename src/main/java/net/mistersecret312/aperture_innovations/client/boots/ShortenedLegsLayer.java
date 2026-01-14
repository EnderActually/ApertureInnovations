package net.mistersecret312.aperture_innovations.client.boots;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;

public class ShortenedLegsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float LEG_V_PIVOT = 20f / 64f;
    private static final float LEG_CUT_RATIO = 0.833f;

    public ShortenedLegsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        
        if (!shouldShortenLegs(player)) return;

        PlayerModel<AbstractClientPlayer> model = getParentModel();
        
        // Get the normal buffer and wrap it with UV scaling
        RenderType renderType = RenderType.entityCutoutNoCull(getTextureLocation(player));
	    // Render each leg separately with fresh wrapped buffers
	    // TODO fix u v offsets
	    renderLeg(model.leftLeg, poseStack, bufferSource.getBuffer(renderType), packedLight);
	    renderLeg(model.rightLeg, poseStack, bufferSource.getBuffer(renderType), packedLight);

	    // Render pants layers too (they have different UV - adjust if needed)
	    //renderLeg(model.leftPants, poseStack, bufferSource.getBuffer(renderType), packedLight);
	    //renderLeg(model.rightPants, poseStack, bufferSource.getBuffer(renderType), packedLight);
    }

	private void renderLeg(net.minecraft.client.model.geom.ModelPart leg, PoseStack poseStack,
	                       VertexConsumer buffer, int packedLight) {
		// Save original state
		boolean wasSkipped = leg.skipDraw;
		boolean wasVisible = leg.visible;

		// Enable rendering
		leg.skipDraw = false;
		leg.visible = true;

		// Wrap buffer with UV scaling
		VertexConsumer scaledBuffer = new LegUVScaleVertexConsumer(buffer, LEG_CUT_RATIO);

		// Render
		leg.render(poseStack, scaledBuffer, packedLight, OverlayTexture.NO_OVERLAY);

		// Restore original state
		leg.skipDraw = wasSkipped;
		leg.visible = wasVisible;
	}

    private boolean shouldShortenLegs(AbstractClientPlayer player) {
        return LongFallBootsItem.isWorn(player);
    }
}