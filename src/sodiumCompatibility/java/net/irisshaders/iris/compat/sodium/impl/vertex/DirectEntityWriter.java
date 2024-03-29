package net.irisshaders.iris.compat.sodium.impl.vertex;

import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.OverlayAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatRegistry;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class DirectEntityWriter {
	public static final VertexFormatDescription FORMAT = VertexFormatRegistry.instance()
		.get(IrisVertexFormats.ENTITY);
	public static final long STRIDE = FORMAT.stride();

	private static final int OFFSET_POSITION = 0;
	private static final int OFFSET_COLOR = 12;
	private static final int OFFSET_TEXTURE = 16;
	private static final int OFFSET_OVERLAY = 24;
	private static final int OFFSET_LIGHT = 28;
	private static final int OFFSET_NORMAL = 32;
	private static final int OFFSET_ENTITYID = 36;
	private static final int OFFSET_MIDTEX = 42;
	private static final int OFFSET_TANGENT = 50;

	public static void write(long ptr,
							 float x, float y, float z, int color, float u, float v, float midU, float midV, int overlay, int light, int normal, int tangent) {
		PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		OverlayAttribute.set(ptr + OFFSET_OVERLAY, overlay);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		NormalAttribute.set(ptr + OFFSET_NORMAL, normal);

		MemoryUtil.memPutShort(ptr + 36, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
		MemoryUtil.memPutShort(ptr + 38, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
		MemoryUtil.memPutShort(ptr + 40, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
		MemoryUtil.memPutFloat(ptr + 42, midU);
		MemoryUtil.memPutFloat(ptr + 46, midV);
		MemoryUtil.memPutInt(ptr + 50, tangent);
	}
}
