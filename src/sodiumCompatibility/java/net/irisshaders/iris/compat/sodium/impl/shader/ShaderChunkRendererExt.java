package net.irisshaders.iris.compat.sodium.impl.shader;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

public interface ShaderChunkRendererExt {
	GlProgram<IrisShaderInterface> getProgram(TerrainRenderPass pass);
}
