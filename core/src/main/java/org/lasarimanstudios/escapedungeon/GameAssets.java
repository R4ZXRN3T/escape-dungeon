package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.lasarimanstudios.escapedungeon.assets.Direction;
import org.lasarimanstudios.escapedungeon.assets.DirectionalAnimationSet;

import java.util.EnumMap;

/**
 * Small wrapper around LibGDX's {@link com.badlogic.gdx.assets.AssetManager}.
 *
 * <p>This centralizes texture loading so gameplay code can request textures by internal asset path.
 * Textures returned by this class are owned by the underlying {@link AssetManager} and must not be
 * disposed by callers. Call {@link #dispose()} when the owning screen/game no longer needs the assets.</p>
 */
public class GameAssets {
	// World / entities
	public static final String TEX_BLOOD_PUDDLE = "textures/objects/blood-puddle.png";
	public static final String TEX_CHEST_CLOSED = "textures/objects/chest/chest_closed.png";
	public static final String TEX_CHEST_OPEN = "textures/objects/chest/chest_open.png";
	public static final String TEX_WALL = "textures/objects/wall.png";

	// Player + weapon
	public static final String TEX_WEAPON_SWORD_1 = "textures/weapons/sword1.png";
	public static final String TEX_PLAYER_LEFT_IDLE = "textures/characters/character-01/character-01-links-stehend.png";
	// (we only have a single player frame right now; reuse it for all directions until more art exists)

	// Goblin frames
	public static final String TEX_GOBLIN_FRONT_IDLE = "textures/enemy/goblin-01/goblin-01-vorne-stehend.png";
	public static final String TEX_GOBLIN_FRONT_WALK_1 = "textures/enemy/goblin-01/goblin-01-vorne-laufen-1.png";
	public static final String TEX_GOBLIN_FRONT_WALK_2 = "textures/enemy/goblin-01/goblin-01-vorne-laufen-2.png";
	public static final String TEX_GOBLIN_BACK_IDLE = "textures/enemy/goblin-01/goblin-01-hinten-stehend.png";
	public static final String TEX_GOBLIN_BACK_WALK_1 = "textures/enemy/goblin-01/goblin-01-hinten-laufen-1.png";
	public static final String TEX_GOBLIN_BACK_WALK_2 = "textures/enemy/goblin-01/goblin-01-hinten-laufen-2.png";
	public static final String TEX_GOBLIN_LEFT_IDLE = "textures/enemy/goblin-01/goblin-01-links-stehend.png";
	public static final String TEX_GOBLIN_LEFT_WALK_1 = "textures/enemy/goblin-01/goblin-01-links-laufen-1.png";
	public static final String TEX_GOBLIN_LEFT_WALK_2 = "textures/enemy/goblin-01/goblin-01-links-laufen-2.png";
	public static final String TEX_GOBLIN_RIGHT_IDLE = "textures/enemy/goblin-01/goblin-01-rechts-stehend.png";
	public static final String TEX_GOBLIN_RIGHT_WALK_1 = "textures/enemy/goblin-01/goblin-01-rechts-laufen-1.png";
	public static final String TEX_GOBLIN_RIGHT_WALK_2 = "textures/enemy/goblin-01/goblin-01-rechts-laufen-2.png";

	// Maps
	public static final String TEX_MAP_TEST = "textures/maps/test.png";

	private final AssetManager assetManager = new AssetManager();

	/**
	 * Loads all gameplay textures (non-menu UI) and blocks until finished.
	 */
	public void load() {
		// World
		assetManager.load(TEX_BLOOD_PUDDLE, Texture.class);
		assetManager.load(TEX_CHEST_CLOSED, Texture.class);
		assetManager.load(TEX_CHEST_OPEN, Texture.class);
		assetManager.load(TEX_WALL, Texture.class);

		// Player + weapons
		assetManager.load(TEX_WEAPON_SWORD_1, Texture.class);
		assetManager.load(TEX_PLAYER_LEFT_IDLE, Texture.class);

		// Enemies
		assetManager.load(TEX_GOBLIN_FRONT_IDLE, Texture.class);
		assetManager.load(TEX_GOBLIN_FRONT_WALK_1, Texture.class);
		assetManager.load(TEX_GOBLIN_FRONT_WALK_2, Texture.class);
		assetManager.load(TEX_GOBLIN_BACK_IDLE, Texture.class);
		assetManager.load(TEX_GOBLIN_BACK_WALK_1, Texture.class);
		assetManager.load(TEX_GOBLIN_BACK_WALK_2, Texture.class);
		assetManager.load(TEX_GOBLIN_LEFT_IDLE, Texture.class);
		assetManager.load(TEX_GOBLIN_LEFT_WALK_1, Texture.class);
		assetManager.load(TEX_GOBLIN_LEFT_WALK_2, Texture.class);
		assetManager.load(TEX_GOBLIN_RIGHT_IDLE, Texture.class);
		assetManager.load(TEX_GOBLIN_RIGHT_WALK_1, Texture.class);
		assetManager.load(TEX_GOBLIN_RIGHT_WALK_2, Texture.class);

		// Maps
		assetManager.load(TEX_MAP_TEST, Texture.class);

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

	public TextureRegion getRegion(String internalPath) {
		return new TextureRegion(getTexture(internalPath));
	}

	public DirectionalAnimationSet createGoblinWalkAnimations(float frameDurationSeconds) {
		EnumMap<Direction, Animation<TextureRegion>> map = new EnumMap<>(Direction.class);
		map.put(Direction.FRONT, new Animation<>(frameDurationSeconds,
			getRegion(TEX_GOBLIN_FRONT_WALK_1),
			getRegion(TEX_GOBLIN_FRONT_WALK_2)
		));
		map.put(Direction.BACK, new Animation<>(frameDurationSeconds,
			getRegion(TEX_GOBLIN_BACK_WALK_1),
			getRegion(TEX_GOBLIN_BACK_WALK_2)
		));
		map.put(Direction.LEFT, new Animation<>(frameDurationSeconds,
			getRegion(TEX_GOBLIN_LEFT_WALK_1),
			getRegion(TEX_GOBLIN_LEFT_WALK_2)
		));
		map.put(Direction.RIGHT, new Animation<>(frameDurationSeconds,
			getRegion(TEX_GOBLIN_RIGHT_WALK_1),
			getRegion(TEX_GOBLIN_RIGHT_WALK_2)
		));

		return new DirectionalAnimationSet(map);
	}

	public TextureRegion getGoblinIdle(Direction direction) {
		return switch (direction) {
			case FRONT -> getRegion(TEX_GOBLIN_FRONT_IDLE);
			case BACK -> getRegion(TEX_GOBLIN_BACK_IDLE);
			case LEFT -> getRegion(TEX_GOBLIN_LEFT_IDLE);
			case RIGHT -> getRegion(TEX_GOBLIN_RIGHT_IDLE);
		};
	}

	public TextureRegion getPlayerIdle(Direction direction) {
		// Until there are directional player assets, just reuse the left-standing image.
		return getRegion(TEX_PLAYER_LEFT_IDLE);
	}

	/**
	 * Disposes the underlying {@link AssetManager} and all assets loaded through it.
	 */
	public void dispose() {
		assetManager.dispose();
	}
}
