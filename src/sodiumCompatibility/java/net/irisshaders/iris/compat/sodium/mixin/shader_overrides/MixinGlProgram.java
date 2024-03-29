package net.irisshaders.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.irisshaders.iris.compat.sodium.impl.shader.ShaderBindingContextExt;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.IntFunction;

@Mixin(GlProgram.class)
public abstract class MixinGlProgram extends GlObject implements ShaderBindingContextExt {
	@Override
	public <U extends GlUniform<?>> U bindUniformOrNull(String name, IntFunction<U> factory) {
		int index = GL20C.glGetUniformLocation(this.handle(), name);

		if (index < 0) {
			return null;
		}

		return factory.apply(index);
	}

	@Override
	public GlUniformBlock bindUniformBlockOrNull(String name, int bindingPoint) {
		int index = GL32C.glGetUniformBlockIndex(this.handle(), name);

		if (index < 0) {
			return null;
		}

		GL32C.glUniformBlockBinding(this.handle(), index, bindingPoint);

		return new GlUniformBlock(bindingPoint);
	}
}
