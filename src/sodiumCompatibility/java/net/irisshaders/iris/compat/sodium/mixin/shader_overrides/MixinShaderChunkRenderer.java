package net.irisshaders.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.shader.IrisShaderInterface;
import net.irisshaders.iris.compat.sodium.impl.shader.ShaderChunkRendererExt;
import net.irisshaders.iris.compat.sodium.impl.shader.SodiumShaderCreator;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@Mixin(ShaderChunkRenderer.class)
public abstract class MixinShaderChunkRenderer implements ShaderChunkRendererExt {
	@Shadow
	protected GlProgram<ChunkShaderInterface> activeProgram;
	@Shadow
	@Final
	protected ChunkVertexType vertexType;
	private final EnumMap<SodiumShaderCreator.ShaderTypes, GlProgram<IrisShaderInterface>> interfac = new EnumMap<>(SodiumShaderCreator.ShaderTypes.class);
	private int version;

	@Shadow
	protected abstract GlProgram<ChunkShaderInterface> compileProgram(ChunkShaderOptions options);

	@Inject(method = "begin", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;startDrawing()V", shift = At.Shift.AFTER), cancellable = true)
	private void overrideShaders(TerrainRenderPass pass, CallbackInfo ci) {
		if (version != Iris.getPipelineManager().getVersionCounterForSodiumShaderReload()) {
			version = Iris.getPipelineManager().getVersionCounterForSodiumShaderReload();
			interfac.values().forEach(GlProgram::delete);
			interfac.clear();
		}

		if (activeProgram == null) {
			ChunkShaderOptions options = new ChunkShaderOptions(ChunkFogMode.SMOOTH, pass, this.vertexType);

			this.activeProgram = this.compileProgram(options);
		}

		if (IrisApi.getInstance().isShaderPackInUse()) {
			GlProgram<IrisShaderInterface> program = interfac.computeIfAbsent(SodiumShaderCreator.toType(pass), a -> SodiumShaderCreator.patchAndCreateShader((IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable(), SodiumShaderCreator.toType(pass)));

			if (program != null) {
				ci.cancel();
				program.bind();
				program.getInterface().setupState();
			}
		}
	}

	@Inject(method = "end", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;endDrawing()V", shift = At.Shift.AFTER), cancellable = true)
	private void overrideShaders2(TerrainRenderPass pass, CallbackInfo ci) {
		if (version != Iris.getPipelineManager().getVersionCounterForSodiumShaderReload()) {
			version = Iris.getPipelineManager().getVersionCounterForSodiumShaderReload();
			interfac.values().forEach(GlProgram::delete);
			interfac.clear();
		}

		if (IrisApi.getInstance().isShaderPackInUse()) {
			GlProgram<IrisShaderInterface> program = interfac.computeIfAbsent(SodiumShaderCreator.toType(pass), a -> SodiumShaderCreator.patchAndCreateShader((IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable(), SodiumShaderCreator.toType(pass)));

			if (program != null) {
				ci.cancel();
				program.getInterface().clearState();
				program.unbind();
			}
		}
	}

	@Override
	public GlProgram<IrisShaderInterface> getProgram(TerrainRenderPass pass) {
		return interfac.computeIfAbsent(SodiumShaderCreator.toType(pass), a -> SodiumShaderCreator.patchAndCreateShader((IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable(), SodiumShaderCreator.toType(pass)));
	}
}
