package net.irisshaders.iris.compat.sodium.impl.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import me.jellysquid.mods.sodium.client.util.TextureUtil;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL32C;

import java.util.function.IntFunction;

/**
 * A shader pack override for chunks.
 */
public class IrisShaderInterface extends ChunkShaderInterface {
	private static final ChunkShaderOptions DEFAULT_OPTIONS = new ChunkShaderOptions(ChunkFogMode.NONE, DefaultTerrainRenderPasses.CUTOUT, ChunkMeshFormats.COMPACT);
	@Nullable
	private final GlUniformMatrix4f uniformModelViewMatrixInverse;
	@Nullable
	private final GlUniformMatrix4f uniformProjectionMatrixInverse;
	@Nullable
	private final GlUniformMatrix3f uniformNormalMatrix;
	private final IrisShaderOptions options;
	private final String name;

	private final ProgramUniforms uniforms;
	private final GlFramebuffer framebuffer;
	private ProgramSamplers samplers;
	private ProgramImages images;

	public IrisShaderInterface(String name, ShaderBindingContextExt context, IrisShaderOptions shaderOptions, IrisRenderingPipeline pipeline, GlFramebuffer framebuffer) {
		super(new ShaderBindingContext() {
			@Override
			public <U extends GlUniform<?>> U bindUniform(String name, IntFunction<U> factory) {
				return context.bindUniformOrNull(name, factory);
			}

			@Override
			public GlUniformBlock bindUniformBlock(String name, int bindingPoint) {
				return context.bindUniformBlockOrNull(name, bindingPoint);
			}
		}, DEFAULT_OPTIONS);

		this.name = name;
		this.options = shaderOptions;
		this.framebuffer = framebuffer;

		ProgramUniforms.Builder uniforms = ProgramUniforms.builder(name, ((GlProgram<?>) context).handle());

		CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
		shaderOptions.customUniforms().assignTo(uniforms);

		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		shaderOptions.customUniforms().mapPassToObject(uniforms, this);

		this.uniforms = uniforms.buildUniforms();

		createSamplers(((GlProgram<?>) context).handle(), pipeline);

		this.uniformModelViewMatrixInverse = context.bindUniformOrNull("iris_ModelViewMatrixInverse", GlUniformMatrix4f::new);
		this.uniformProjectionMatrixInverse = context.bindUniformOrNull("iris_ProjectionMatrixInverse", GlUniformMatrix4f::new);
		this.uniformNormalMatrix = context.bindUniformOrNull("iris_NormalMatrix", GlUniformMatrix3f::new);
	}

	private void createSamplers(int handle, IrisRenderingPipeline pipeline) {
		ProgramSamplers.Builder samplerHolder = ProgramSamplers.builder(handle, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		ProgramImages.Builder imageHolder = ProgramImages.builder(handle);

		pipeline.addGbufferOrShadowSamplers(samplerHolder, imageHolder, pipeline.getFlipState(!options.isShadowPass(), !options.isShadowPass() && options.isTranslucent()), options.isShadowPass(), true, true, false);

		this.samplers = samplerHolder.build();
		this.images = imageHolder.build();
	}

	@Override
	public void setupState() {
		// See IrisSamplers#addLevelSamplers
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.ALBEDO_TEXTURE_UNIT, TextureUtil.getBlockTextureId());
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, TextureUtil.getLightTextureId());
		// This is what is expected by the rest of rendering state, failure to do this will cause blurry textures on particles.
		GlStateManager._activeTexture(GL32C.GL_TEXTURE0 + IrisSamplers.LIGHTMAP_TEXTURE_UNIT);
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(options.alpha().reference());

		if (options.globalOverride() != null) {
			options.globalOverride().apply();
		}

		ImmediateState.usingTessellation = options.usePatches();

		options.localOverrides().forEach(BufferBlendOverride::apply);

		options.customUniforms().push(this);

		uniforms.update();
		samplers.update();
		images.update();

		framebuffer.bind();
	}

	public void clearState() {
		ImmediateState.usingTessellation = false;

		BlendModeOverride.restore();

		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
	}

	@Override
	public void setProjectionMatrix(Matrix4fc matrix) {
		super.setProjectionMatrix(matrix);

		if (uniformProjectionMatrixInverse != null) {
			uniformProjectionMatrixInverse.set(matrix.invert(new Matrix4f()));
		}
	}

	@Override
	public void setModelViewMatrix(Matrix4fc matrix) {
		super.setModelViewMatrix(matrix);

		Matrix4f inverted = matrix.invert(new Matrix4f());

		if (uniformModelViewMatrixInverse != null) {
			uniformModelViewMatrixInverse.set(inverted);
		}

		if (uniformNormalMatrix != null) {
			uniformNormalMatrix.set(inverted.transpose3x3(new Matrix3f()));
		}
	}
}
