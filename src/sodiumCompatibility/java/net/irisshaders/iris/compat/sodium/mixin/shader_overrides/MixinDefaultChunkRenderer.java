package net.irisshaders.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.shader.ShaderChunkRendererExt;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinDefaultChunkRenderer extends ShaderChunkRenderer {
	public MixinDefaultChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;activeProgram:Lme/jellysquid/mods/sodium/client/gl/shader/GlProgram;"))
	private GlProgram changeActiveProgram(DefaultChunkRenderer instance, ChunkRenderMatrices matrices,
										  CommandList commandList,
										  ChunkRenderListIterable renderLists,
										  TerrainRenderPass renderPass) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			if (instance instanceof ShaderChunkRendererExt ext) {
				return ext.getProgram(renderPass);
			}
		}

		return activeProgram;
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useBlockFaceCulling:Z"), remap = false)
	private boolean iris$disableBlockFaceCullingInShadowPass(SodiumGameOptions.PerformanceSettings instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
		return instance.useBlockFaceCulling;
	}
}
