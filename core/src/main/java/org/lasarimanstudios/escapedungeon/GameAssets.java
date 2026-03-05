package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Small wrapper around LibGDX's {@link com.badlogic.gdx.assets.AssetManager}.
 *
 * <p>This centralizes texture loading so gameplay code can request textures by internal asset path.
 * Textures returned by this class are owned by the underlying {@link AssetManager} and must not be
 * disposed by callers. Call {@link #dispose()} when the owning screen/game no longer needs the assets.</p>
 */
public class GameAssets {
	public static final String TEX_BLOOD_PUDDLE = "textures/enemy/blood-puddle/blood-puddle.png";
	public static final String TEX_CHEST = "textures/objects/chest/chest_closed.png";

	private final AssetManager assetManager = new AssetManager();

	/**
	 * Loads the currently registered base textures and blocks until finished.
	 */
	public void load() {
		assetManager.load(TEX_BLOOD_PUDDLE, Texture.class);
		assetManager.load(TEX_CHEST, Texture.class);
		assetManager.finishLoading();
	}

	/**
	 * Returns a previously loaded texture.
	 *
	 * @param internalPath internal asset path (e.g. {@code textures/...png})
	 * @return loaded texture
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException if the asset isn't loaded
	 */
	public Texture getTexture(String internalPath) {
		return assetManager.get(internalPath, Texture.class);
	}

	/**
	 * Convenience method that ensures a texture is loaded and then returns it.
	 *
	 * <p>This is useful during development as a safe fallback for assets that weren't preloaded.</p>
	 *
	 * @param internalPath internal asset path
	 * @return loaded texture
	 */
	public Texture createTexture(String internalPath) {
		// Minimal, safe transition helper: if a texture isn't preloaded, load it now.
		if (!assetManager.isLoaded(internalPath, Texture.class)) {
			assetManager.load(internalPath, Texture.class);
			assetManager.finishLoading();
		}
		return getTexture(internalPath);
	}

	/**
	 * Disposes the underlying {@link AssetManager} and all assets loaded through it.
	 */
	public void dispose() {
		assetManager.dispose();
	}
}
