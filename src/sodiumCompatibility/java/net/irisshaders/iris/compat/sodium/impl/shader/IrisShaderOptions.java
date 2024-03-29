package net.irisshaders.iris.compat.sodium.impl.shader;

import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

import java.util.Collection;

public record IrisShaderOptions(boolean isShadowPass, boolean isTranslucent, boolean usePatches, AlphaTest alpha,
								BlendModeOverride globalOverride, Collection<BufferBlendOverride> localOverrides,
								CustomUniforms customUniforms) {
}
