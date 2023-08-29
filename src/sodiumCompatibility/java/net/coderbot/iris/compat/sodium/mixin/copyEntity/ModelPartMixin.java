package net.coderbot.iris.compat.sodium.mixin.copyEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.vertex.VertexConsumerUtils;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.coderbot.iris.compat.sodium.impl.entities.IrisModelCuboid;
import net.coderbot.iris.compat.sodium.impl.entities.IrisModelCuboidAccessor;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.NormI8;
import net.coderbot.iris.vertices.NormalHelper;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin {
	@Shadow public float x;
	@Shadow public float y;
	@Shadow public float z;

	@Shadow public float yRot;
	@Shadow public float xRot;
	@Shadow public float zRot;

	@Shadow public float xScale;
	@Shadow public float yScale;
	@Shadow public float zScale;

	@Unique
	private IrisModelCuboid[] sodium$cuboids;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(List<ModelPart.Cube> cuboids, Map<String, ModelPart> children, CallbackInfo ci) {
		var copies = new IrisModelCuboid[cuboids.size()];

		for (int i = 0; i < cuboids.size(); i++) {
			var accessor = (IrisModelCuboidAccessor) cuboids.get(i);
			copies[i] = accessor.iris$copy();
		}

		this.sodium$cuboids = copies;
	}

	private boolean extend() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.renderWithExtendedVertexFormat;
	}
	private static short encodeTexture(float value) {
		return (short) (Math.min(0.99999997F, value) * 65536);
	}

	/**
	 * @author JellySquid
	 * @reason Use optimized vertex writer, avoid allocations, use quick matrix transformations
	 */
	@Inject(method = "compile", at = @At("HEAD"), cancellable = true)
	private void renderCuboidsFast(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
		var writer = VertexConsumerUtils.convertOrLog(vertexConsumer);
		if(writer == null) {
			return;
		}

		ci.cancel();

		int color = ColorABGR.pack(red, green, blue, alpha);

		for (IrisModelCuboid cuboid : this.sodium$cuboids) {
			cuboid.updateVertices(matrices.pose());

			boolean extend = extend();

			try (MemoryStack stack = MemoryStack.stackPush()) {
				long buffer = stack.nmalloc(4 * 6 * (extend ? EntityVertex.STRIDE : ModelVertex.STRIDE));
				long ptr = buffer;

				int count = 0;

				for (IrisModelCuboid.Quad quad : cuboid.quads) {
					if (quad == null) continue;

					var normal = quad.getNormal(matrices.normal());

					float midU = 0, midV = 0;
					int tangent = 0;

					if (extend) {
						for (int i = 0; i < 4; i++) {
							midU += quad.textures[i].x;
							midV += quad.textures[i].y;
						}

						midU *= 0.25;
						midV *= 0.25;

						tangent = NormalHelper.computeTangent(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal), quad.positions[0].x, quad.positions[0].y, quad.positions[0].z, quad.textures[0].x, quad.textures[0].y,
								quad.positions[1].x, quad.positions[1].y, quad.positions[1].z, quad.textures[1].x, quad.textures[1].y,
								quad.positions[2].x, quad.positions[2].y, quad.positions[2].z, quad.textures[2].x, quad.textures[2].y
						);
					}

					if (extend) {
						short midUFinal = encodeTexture(midU);
						short midVFinal = encodeTexture(midV);
						for (int i = 0; i < 4; i++) {
							var pos = quad.positions[i];
							var pos2 = quad.prevPositions[i];
							var tex = quad.textures[i];

							EntityVertex.writeWithVelocity(ptr, pos.x, pos.y, pos.z, pos2.x, pos2.y, pos2.z, color, tex.x, tex.y, midUFinal, midVFinal, light, overlay, normal, tangent);

							ptr += EntityVertex.STRIDE;
						}
					} else {
						for (int i = 0; i < 4; i++) {
							var pos = quad.positions[i];
							var tex = quad.textures[i];

							ModelVertex.write(ptr, pos.x, pos.y, pos.z, color, tex.x, tex.y, overlay, light, normal);

							ptr += ModelVertex.STRIDE;
						}
					}

					count += 4;
				}

				writer.push(stack, buffer, count, extend ? EntityVertex.FORMAT : ModelVertex.FORMAT);
			}
		}
	}

	/**
	 * @author JellySquid
	 * @reason Apply transform more quickly
	 */
	@Overwrite
	public void translateAndRotate(PoseStack matrices) {
		matrices.translate(this.x * (1.0F / 16.0F), this.y * (1.0F / 16.0F), this.z * (1.0F / 16.0F));

		if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
			MatrixHelper.rotateZYX(matrices.last(), this.zRot, this.yRot, this.xRot);
		}

		if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
			matrices.scale(this.xScale, this.yScale, this.zScale);
		}
	}
}
