package net.mistersecret312.aperture_innovations.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class PortalRenderTypes extends RenderType
{
	public static final RenderStateShard.ShaderStateShard POSITION_TEX_COLOR_SHADER = new RenderStateShard.ShaderStateShard(
			GameRenderer::getPositionTexColorShader);

	public PortalRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize,
							 boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState,
							 Runnable pClearState)
	{
		super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
	}

	public static RenderType portal(ResourceLocation texture)
	{
		return create("portal", DefaultVertexFormat.POSITION_TEX_COLOR,
				VertexFormat.Mode.QUADS, 256, true, true,
				CompositeState.builder()
										 .setShaderState(POSITION_TEX_COLOR_SHADER)
										 .setTextureState(new TextureStateShard(texture, false, false))
										 .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
										 .createCompositeState(true)
		);
	}

	public static RenderType portalEndMask() {
		return RenderType.create(
				"masked_end_portal",
				DefaultVertexFormat.POSITION,
				VertexFormat.Mode.QUADS,
				256,
				false,
				false,
				RenderType.CompositeState.builder()
										 .setShaderState(RenderStateShard.RENDERTYPE_END_GATEWAY_SHADER)
										 .setTextureState(MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false).build())
										 .setDepthTestState(new RenderStateShard.DepthTestStateShard("==", GL11.GL_EQUAL))
										 .createCompositeState(false)
		);
	}

	public static RenderType portalFrame(ResourceLocation location)
	{
		return create("portal_frame", DefaultVertexFormat.POSITION_TEX_COLOR,
				VertexFormat.Mode.QUADS, 256, true, true,
				CompositeState.builder()
										 .setShaderState(POSITION_TEX_COLOR_SHADER)
										 .setTextureState(new TextureStateShard(location, false, false))
										 .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
										 .createCompositeState(true)
		);
	}

	public static RenderType laserTest(ResourceLocation location)
	{
		return create("laser_test", DefaultVertexFormat.POSITION_TEX_COLOR,
				VertexFormat.Mode.QUADS, 256, true, true,
				CompositeState.builder()
						.setShaderState(POSITION_TEX_COLOR_SHADER)
						.setTextureState(new TextureStateShard(location, false, false))
						.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
						.setCullState(CULL)
						.createCompositeState(true));
	}

	public static RenderType portalVortex(ResourceLocation location)
	{
		return create("portal_vortex", DefaultVertexFormat.POSITION_TEX_COLOR,
				VertexFormat.Mode.QUADS, 256, true, true,
				CompositeState.builder()
										 .setShaderState(POSITION_TEX_COLOR_SHADER)
										 .setTextureState(new TextureStateShard(location, false, false))
										 .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
										 .createCompositeState(true)
		);
	}
}
