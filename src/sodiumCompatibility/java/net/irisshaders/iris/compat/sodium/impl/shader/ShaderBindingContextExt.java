package net.irisshaders.iris.compat.sodium.impl.shader;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

import java.util.function.IntFunction;

public interface ShaderBindingContextExt extends ShaderBindingContext {
	<U extends GlUniform<?>> U bindUniformOrNull(String name, IntFunction<U> factory);

	GlUniformBlock bindUniformBlockOrNull(String name, int bindingPoint);
}
