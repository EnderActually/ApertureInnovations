package net.mistersecret312.aperture_innovations.client.boots;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class LegUVScaleVertexConsumer implements VertexConsumer {
	private final VertexConsumer delegate;
	private final float vScale;
	private final float vTop;    // Top of leg texture (20/64)
	private final float vBottom; // Bottom of leg texture (32/64)

	public LegUVScaleVertexConsumer(VertexConsumer delegate, float vScale) {
		this.delegate = delegate;
		this.vScale = vScale;
		this.vTop = 20f / 64f;     // 0.3125
		this.vBottom = 32f / 64f;  // 0.5
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a) {
		delegate.setColor(r, g, b, a);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		// Shift V up by half the leg texture height to show top half
		float legTextureHeight = 12f / 64f; // Leg is 12 pixels tall in texture
		float offset = legTextureHeight * (1f - vScale); // How much to shift up

		float newV = v - offset;
		delegate.setUv(u, newV);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		delegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		delegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		delegate.setNormal(x, y, z);
		return this;
	}
}