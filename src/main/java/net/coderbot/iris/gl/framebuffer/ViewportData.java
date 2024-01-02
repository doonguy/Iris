package net.coderbot.iris.gl.framebuffer;

public record ViewportData(float scale, float viewportX, float viewportY) {
	private static ViewportData DEFAULT = new ViewportData(1.0f, 0.0f, 0.0f);

	public static ViewportData defaultValue() {
		return DEFAULT;
	}
}
