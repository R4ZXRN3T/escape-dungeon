package org.lasarimanstudios.escapedungeon.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * Utility wrapper that provides a lazily-initialized hue-rotation shader and an "apply" helper
 * that sets up the shader on a SpriteBatch, invokes a drawing callback, and restores the
 * previous shader.
 */
public final class HueShader {
	private static ShaderProgram shader = null;
	private static boolean attemptedInit = false;

	private HueShader() { /* no instances */ }

	private static synchronized ShaderProgram createShader() {
		if (attemptedInit) return shader;
		attemptedInit = true;

		ShaderProgram.pedantic = false;
		final String vertex =
			"""
				attribute vec4 a_position;
				attribute vec4 a_color;
				attribute vec2 a_texCoord0;
				uniform mat4 u_projTrans;
				varying vec4 v_color;
				varying vec2 v_texCoords;
				void main() {
				    v_color = a_color;
				    v_texCoords = a_texCoord0;
				    gl_Position = u_projTrans * a_position;
				}
				""";

		final String fragment =
			"""
				#ifdef GL_ES
				precision mediump float;
				#endif
				varying vec4 v_color;
				varying vec2 v_texCoords;
				uniform sampler2D u_texture;
				uniform float u_hue;
				vec3 hsv2rgb(vec3 c) {
				    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
				    return c.z * mix(vec3(1.0), rgb, c.y);
				}
				void main() {
				    vec4 tex = texture2D(u_texture, v_texCoords) * v_color;
				    float value = max(max(tex.r, tex.g), tex.b);
				    vec3 recolor = hsv2rgb(vec3(mod(u_hue, 1.0), 1.0, value));
				    gl_FragColor = vec4(recolor, tex.a);
				}
				""";

		shader = new ShaderProgram(vertex, fragment);
		if (!shader.isCompiled()) {
			System.err.println("Hue shader compile error: " + shader.getLog());
			shader = null;
		} else {
			System.out.println("Hue shader compiled successfully");
		}

		return shader;
	}

	/**
	 * Returns the shader instance if available (compilation succeeded), or {@code null} otherwise.
	 */
	public static ShaderProgram get() {
		if (shader != null) return shader;
		return createShader();
	}

	/**
	 * Applies the hue shader to the provided Batch for the duration of the drawAction. This
	 * currently only supports SpriteBatch; if the provided batch is not a SpriteBatch the method
	 * returns false and does not invoke the shader.
	 *
	 * @param batch      drawing batch (expected to be a SpriteBatch)
	 * @param hue        hue offset in the range [0,1)
	 * @param drawAction callback that performs the draw calls while the shader is active
	 * @return true if the shader was used, false if the shader is unavailable or the batch type isn't supported
	 */
	public static boolean apply(Batch batch, float hue, Runnable drawAction) {
		ShaderProgram s = get();
		if (s == null) return false;
		if (!(batch instanceof SpriteBatch sb)) return false;

		// Flush pending geometry before switching shaders
		sb.flush();
		ShaderProgram prev = sb.getShader();
		sb.setShader(s);
		s.bind();
		s.setUniformf("u_hue", hue);
		Matrix4 combined = new Matrix4(sb.getProjectionMatrix());
		combined.mul(sb.getTransformMatrix());
		s.setUniformMatrix("u_projTrans", combined);
		// texture unit 0
		s.setUniformi("u_texture", 0);

		// Invoke drawing while shader is active
		try {
			drawAction.run();
		} finally {
			// Flush and restore previous shader
			sb.flush();
			sb.setShader(prev);
		}

		return true;
	}
}

