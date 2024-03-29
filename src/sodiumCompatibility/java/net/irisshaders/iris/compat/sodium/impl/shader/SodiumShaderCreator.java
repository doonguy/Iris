package net.irisshaders.iris.compat.sodium.impl.shader;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SodiumShaderCreator {
	public static ShaderTypes toType(TerrainRenderPass pass) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (pass == DefaultTerrainRenderPasses.SOLID) {
				return ShaderTypes.SHADOW_SOLID;
			} else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT) {
				return ShaderTypes.SHADOW_TRANSLUCENT;
			} else {
				return ShaderTypes.SHADOW_CUTOUT;
			}
		} else {
			if (pass == DefaultTerrainRenderPasses.SOLID) {
				return ShaderTypes.SOLID;
			} else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT) {
				return ShaderTypes.TRANSLUCENT;
			} else {
				return ShaderTypes.CUTOUT;
			}
		}
	}

	public static GlProgram<IrisShaderInterface> patchAndCreateShader(IrisRenderingPipeline pipeline, ShaderTypes type) {
		ResourceLocation name = new ResourceLocation("iris", type.name().toLowerCase(Locale.ROOT));
		GlProgram.Builder interfac = GlProgram.builder(name);

		Map<PatchShaderType, String> shaders = pipeline.getAndPatchSodiumShader(type.getId(), type.defaultAlpha());

		List<GlShader> shad = new ArrayList<>(5);

		shaders.forEach((patch, src) -> {
			if (src == null) return;
			GlShader s = new GlShader(IrisShaderTypes.fromIris(patch.glShaderType), name, src);
			shad.add(s);
			interfac.attachShader(s);
		});

		interfac.bindAttribute("a_PosId", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID)
			.bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
			.bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE)
			.bindAttribute("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE)
			.bindAttribute("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
			.bindAttribute("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
			.bindAttribute("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
			.bindAttribute("iris_Normal", IrisChunkShaderBindingPoints.NORMAL)
			.bindAttribute("at_midBlock", IrisChunkShaderBindingPoints.MID_BLOCK)
			.bindFragmentData("fragColor", ChunkShaderBindingPoints.FRAG_COLOR);

		try {
			return interfac.link((shaderBindingContext -> new IrisShaderInterface(type.name().toLowerCase(), (ShaderBindingContextExt) shaderBindingContext, new IrisShaderOptions(type.isShadowPass(), type == ShaderTypes.TRANSLUCENT, shaders.get(PatchShaderType.TESS_CONTROL) != null,
                pipeline.getAlphaOrElse(type.getId(), type.defaultAlpha()), pipeline.getBlendOverride(type.getId()), pipeline.getBufferOverrides(type.getId()), pipeline.getCustomUniforms()),
                pipeline, pipeline.getFramebuffer(type.getId(), type.isShadowPass(), !type.isShadowPass(), type == ShaderTypes.TRANSLUCENT))));
		} finally {
			shad.forEach(GlShader::delete);
		}
	}

	public enum ShaderTypes {
		SHADOW_SOLID(ProgramId.ShadowSolid, true, AlphaTests.OFF),
		SHADOW_CUTOUT(ProgramId.ShadowCutout, true, AlphaTests.ONE_TENTH_ALPHA),
		SHADOW_TRANSLUCENT(ProgramId.ShadowSolid, true, AlphaTests.OFF),
		SOLID(ProgramId.TerrainSolid, false, AlphaTests.OFF),
		CUTOUT(ProgramId.TerrainCutout, false, AlphaTests.ONE_TENTH_ALPHA),
		TRANSLUCENT(ProgramId.Water, false, AlphaTests.OFF);

		private final ProgramId id;
		private final boolean isShadow;
		private final AlphaTest defaultAlpha;

		ShaderTypes(ProgramId matchingShader, boolean isShadowPass, AlphaTest defaultAlpha) {
			this.id = matchingShader;
			this.isShadow = isShadowPass;
			this.defaultAlpha = defaultAlpha;
		}

		public ProgramId getId() {
			return id;
		}

		public boolean isShadowPass() {
			return isShadow;
		}

		public AlphaTest defaultAlpha() {
			return defaultAlpha;
		}
	}
}
