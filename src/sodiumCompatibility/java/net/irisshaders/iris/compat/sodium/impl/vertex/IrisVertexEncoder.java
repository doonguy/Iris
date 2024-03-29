package net.irisshaders.iris.compat.sodium.impl.vertex;

import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.world.level.block.state.BlockState;

public interface IrisVertexEncoder {
	void setBlockPosition(int x, int y, int z);

	void setBlockInfo(short blockId, short renderType);

	default void setBlockInfo(BlockState blockState, short renderType) {
		setBlockInfo((short) WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(blockState), renderType);
	}

	void clearInfo();
}
