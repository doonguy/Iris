package net.irisshaders.iris.compat.sodium.impl.shader;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;

/**
 * Initialized by {@link net.irisshaders.iris.compat.sodium.mixin.shader_overrides.MixinShaderType}
 */
public class IrisShaderTypes {
	public static ShaderType GEOMETRY;
	public static ShaderType TESS_CONTROL;
	public static ShaderType TESS_EVAL;

	public static ShaderType fromIris(net.irisshaders.iris.gl.shader.ShaderType glShaderType) {
		switch (glShaderType) {
			case VERTEX -> {
				return ShaderType.VERTEX;
			}
			case GEOMETRY -> {
				return GEOMETRY;
			}
			case FRAGMENT -> {
				return ShaderType.FRAGMENT;
			}
			case COMPUTE -> {
				// Compute isn't valid for a Sodium shader
				return null;
			}
			case TESSELATION_CONTROL -> {
				return TESS_CONTROL;
			}
			case TESSELATION_EVAL -> {
				return TESS_EVAL;
			}
			default -> {
				return null;
			}
		}
	}
}
