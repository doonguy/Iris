package net.irisshaders.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.compat.sodium.impl.vertex.IrisVertexEncoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkMeshBufferBuilder.class)
public class MixinChunkMeshBufferBuilder implements IrisVertexEncoder {
	@Shadow
	@Final
	private ChunkVertexEncoder encoder;

	@Override
	public void setBlockPosition(int x, int y, int z) {
		if (this.encoder instanceof IrisVertexEncoder e) {
			e.setBlockPosition(x, y, z);
		}
	}

	@Override
	public void setBlockInfo(short blockId, short renderType) {
		if (this.encoder instanceof IrisVertexEncoder e) {
			e.setBlockInfo(blockId, renderType);
		}
	}

	@Override
	public void clearInfo() {
		if (this.encoder instanceof IrisVertexEncoder e) {
			e.clearInfo();
		}
	}
}
