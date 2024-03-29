package net.irisshaders.iris.compat.sodium.mixin.block_id;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.compat.sodium.impl.vertex.IrisVertexEncoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkBuildBuffers.class)
public class MixinChunkBuildBuffers implements IrisVertexEncoder {
	@Shadow
	@Final
	private Reference2ReferenceOpenHashMap<TerrainRenderPass, BakedChunkModelBuilder> builders;

	@Override
	public void setBlockPosition(int x, int y, int z) {
		for (var builder : this.builders.values()) {
			for (ModelQuadFacing facing : ModelQuadFacing.VALUES) {
				((IrisVertexEncoder) builder.getVertexBuffer(facing)).setBlockPosition(x, y, z);
			}
		}
	}

	@Override
	public void setBlockInfo(short blockId, short renderType) {
		for (var builder : this.builders.values()) {
			for (ModelQuadFacing facing : ModelQuadFacing.VALUES) {
				((IrisVertexEncoder) builder.getVertexBuffer(facing)).setBlockInfo(blockId, renderType);
			}
		}
	}

	@Override
	public void clearInfo() {
		for (var builder : this.builders.values()) {
			for (ModelQuadFacing facing : ModelQuadFacing.VALUES) {
				((IrisVertexEncoder) builder.getVertexBuffer(facing)).clearInfo();
			}
		}
	}
}
