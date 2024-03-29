package net.irisshaders.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.irisshaders.iris.compat.sodium.impl.shader.IrisShaderInterface;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShaderInterface.class)
public class MixinChunkShaderInterface {
	@Shadow
	@Final
	private GlUniformMatrix4f uniformProjectionMatrix;

	@Shadow
	@Final
	private GlUniformMatrix4f uniformModelViewMatrix;

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/shader/ShaderBindingContext;bindUniform(Ljava/lang/String;Ljava/util/function/IntFunction;)Lme/jellysquid/mods/sodium/client/gl/shader/uniform/GlUniform;"))
	private String bindUniform(String name) {
		if (name.startsWith("u_") && (((Object) this) instanceof IrisShaderInterface)) {
			return name.replace("u_", "iris_");
		}

		return name;
	}

	@Inject(method = "setProjectionMatrix", at = @At("HEAD"), cancellable = true)
	private void cancelIfNull(Matrix4fc matrix, CallbackInfo ci) {
		if (this.uniformProjectionMatrix == null) ci.cancel();
	}

	@Inject(method = "setModelViewMatrix", at = @At("HEAD"), cancellable = true)
	private void cancelIfNull2(Matrix4fc matrix, CallbackInfo ci) {
		if (this.uniformModelViewMatrix == null) ci.cancel();
	}
}
