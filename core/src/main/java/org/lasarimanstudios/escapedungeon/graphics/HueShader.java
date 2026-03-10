package org.lasarimanstudios.escapedungeon.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * Utility wrapper that provides a lazily-initialized hue-rotation shader and a convenience
 * method to apply it to a {@link SpriteBatch} for the duration of a drawing callback.
 */
public final class HueShader {
	private static ShaderProgram shader = null;
	private static boolean attemptedInit = false;

	private HueShader() {
	}

	/**
	 * Compiles the hue-rotation shader program on first call.
	 *
	 * @return the compiled shader, or {@code null} if compilation failed
	 */
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
	 * Returns the hue-rotation shader if available (compilation succeeded), or {@code null}
	 * otherwise. The shader is lazily compiled on the first call.
	 *
	 * @return the shader program, or {@code null} if compilation failed
	 */
	public static ShaderProgram get() {
		if (shader != null) return shader;
		return createShader();
	}

	/**
	 * Applies the hue-rotation shader to the provided {@link Batch} for the duration of
	 * {@code drawAction}.
	 *
	 * <p>This currently only supports {@link SpriteBatch}. If the provided batch is not a
	 * {@code SpriteBatch}, the method returns {@code false} without invoking the callback.</p>
	 *
	 * @param batch      drawing batch (expected to be a {@link SpriteBatch})
	 * @param hue        hue offset in the range {@code [0, 1)}
	 * @param drawAction callback that performs draw calls while the shader is active
	 * @return {@code true} if the shader was applied, {@code false} if unavailable or unsupported batch type
	 */
	public static boolean apply(Batch batch, float hue, Runnable drawAction) {
		ShaderProgram s = get();
		if (s == null) return false;
		if (!(batch instanceof SpriteBatch sb)) return false;

		sb.flush();
		ShaderProgram prev = sb.getShader();
		sb.setShader(s);
		s.bind();
		s.setUniformf("u_hue", hue);
		Matrix4 combined = new Matrix4(sb.getProjectionMatrix());
		combined.mul(sb.getTransformMatrix());
		s.setUniformMatrix("u_projTrans", combined);
		s.setUniformi("u_texture", 0);

		try {
			drawAction.run();
		} finally {
			sb.flush();
			sb.setShader(prev);
		}

		return true;
	}
}

