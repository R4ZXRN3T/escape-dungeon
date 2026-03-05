package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Central asset registry for GPU resources.
 *
 * <p>Ownership rule: {@link GameAssets} loads and disposes textures. Other classes should only
 * hold references and must not dispose textures.</p>
 */
public class GameAssets {
	public static final String TEX_BLOOD_PUDDLE = "textures/enemy/blood-puddle/blood-puddle.png";
	public static final String TEX_CHEST = "textures/objects/chest/chest_closed.png";

	private final AssetManager assetManager = new AssetManager();

	public void load() {
		assetManager.load(TEX_BLOOD_PUDDLE, Texture.class);
		assetManager.load(TEX_CHEST, Texture.class);
		assetManager.finishLoading();
	}

	public Texture getTexture(String internalPath) {
		return assetManager.get(internalPath, Texture.class);
	}

	public Texture createTexture(String internalPath) {
		// Minimal, safe transition helper: if a texture isn't preloaded, load it now.
		if (!assetManager.isLoaded(internalPath, Texture.class)) {
			assetManager.load(internalPath, Texture.class);
			assetManager.finishLoading();
		}
		return getTexture(internalPath);
	}

	public void dispose() {
		assetManager.dispose();
	}
}

