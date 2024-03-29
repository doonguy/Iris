package net.irisshaders.iris.compat.sodium.mixin.vertex_format.fast_path;

import me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.irisshaders.iris.compat.sodium.impl.vertex.DirectEntityWriter;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	@Shadow
	@Final
	private static Vector3f[][] VERTEX_POSITIONS_MIRRORED;

	@Shadow
	@Final
	private static Vector3f[][] VERTEX_POSITIONS;

	@Shadow
	@Final
	private static Vector2f[][] VERTEX_TEXTURES_MIRRORED;

	@Shadow
	@Final
	private static Vector2f[][] VERTEX_TEXTURES;

	@Shadow
	@Final
	private static int[] CUBE_NORMALS_MIRRORED;

	@Shadow
	@Final
	private static int[] CUBE_NORMALS;

	@Shadow
	@Final
	private static long SCRATCH_BUFFER;

	@Shadow
	@Final
	private static int NUM_CUBE_FACES;

	@Shadow
	private static int emitQuads(ModelCuboid cuboid, int color, int overlay, int light) {
		throw new AssertionError();
	}

	@Redirect(method = "renderCuboids", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/immediate/model/EntityRenderer;emitQuads(Lme/jellysquid/mods/sodium/client/render/immediate/model/ModelCuboid;III)I"))
	private static int changeIrisRender(ModelCuboid quadIndex, int cuboid, int color, int overlay) {
		if (WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return emitQuadsIris(quadIndex, cuboid, color, overlay);
		} else {
			return emitQuads(quadIndex, cuboid, color, overlay);
		}
	}

	@Redirect(method = "renderCuboids", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/api/vertex/format/common/ModelVertex;FORMAT:Lnet/caffeinemc/mods/sodium/api/vertex/format/VertexFormatDescription;"))
	private static VertexFormatDescription changeIrisRender2() {
		if (WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return DirectEntityWriter.FORMAT;
		} else {
			return ModelVertex.FORMAT;
		}
	}

	@Unique
	private static int emitQuadsIris(ModelCuboid cuboid, int color, int overlay, int light) {
		final var positions = cuboid.mirror ? VERTEX_POSITIONS_MIRRORED : VERTEX_POSITIONS;
		final var textures = cuboid.mirror ? VERTEX_TEXTURES_MIRRORED : VERTEX_TEXTURES;
		final var normals = cuboid.mirror ? CUBE_NORMALS_MIRRORED :  CUBE_NORMALS;

		var vertexCount = 0;

		long ptr = SCRATCH_BUFFER;


		for (int quadIndex = 0; quadIndex < NUM_CUBE_FACES; quadIndex++) {
			if (!cuboid.shouldDrawFace(quadIndex)) {
				continue;
			}

			float midU = textures[quadIndex][0].x + textures[quadIndex][1].x + textures[quadIndex][2].x + textures[quadIndex][3].x;
			float midV = textures[quadIndex][0].y + textures[quadIndex][1].y + textures[quadIndex][2].y + textures[quadIndex][3].y;
			int tangent = NormalHelper.computeTangent(NormI8.unpackX(normals[quadIndex]), NormI8.unpackY(normals[quadIndex]), NormI8.unpackZ(normals[quadIndex]),
				positions[quadIndex][0].x, positions[quadIndex][0].y, positions[quadIndex][0].z, textures[quadIndex][0].x, textures[quadIndex][0].y,
				positions[quadIndex][1].x, positions[quadIndex][1].y, positions[quadIndex][1].z, textures[quadIndex][1].x, textures[quadIndex][1].y,
				positions[quadIndex][2].x, positions[quadIndex][2].y, positions[quadIndex][2].z, textures[quadIndex][2].x, textures[quadIndex][2].y);

			midU *= 0.25f;
			midV *= 0.25f;

			emitVertexIris(ptr, positions[quadIndex][0], color, textures[quadIndex][0], midU, midV, overlay, light, normals[quadIndex], tangent);
			ptr += DirectEntityWriter.STRIDE;

			emitVertexIris(ptr, positions[quadIndex][1], color, textures[quadIndex][1], midU, midV, overlay, light, normals[quadIndex], tangent);
			ptr += DirectEntityWriter.STRIDE;

			emitVertexIris(ptr, positions[quadIndex][2], color, textures[quadIndex][2], midU, midV, overlay, light, normals[quadIndex], tangent);
			ptr += DirectEntityWriter.STRIDE;

			emitVertexIris(ptr, positions[quadIndex][3], color, textures[quadIndex][3], midU, midV, overlay, light, normals[quadIndex], tangent);
			ptr += DirectEntityWriter.STRIDE;

			vertexCount += 4;
		}

		return vertexCount;
	}

	@Unique
	private static void emitVertexIris(long ptr, Vector3f pos, int color, Vector2f tex, float midU, float midV, int overlay, int light, int normal, int tangent) {
		DirectEntityWriter.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, midU, midV, overlay, light, normal, tangent);
	}
}
