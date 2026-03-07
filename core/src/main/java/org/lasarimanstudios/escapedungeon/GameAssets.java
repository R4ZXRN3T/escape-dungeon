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

	public static final String TEX_WALL_ORIGINAL = "textures/objects/walls/wall.png";
	public static final String TEX_WALL_A1 = "textures/objects/walls/wall_A1.png";
	public static final String TEX_WALL_A2 = "textures/objects/walls/wall_A2.png";
	public static final String TEX_WALL_A3 = "textures/objects/walls/wall_A3.png";
	public static final String TEX_WALL_A4 = "textures/objects/walls/wall_A4.png";
	public static final String TEX_WALL_H1 = "textures/objects/walls/wall_H1.png";
	public static final String TEX_WALL_H2 = "textures/objects/walls/wall_H2.png";
	public static final String TEX_WALL_V1 = "textures/objects/walls/wall_V1.png";
	public static final String TEX_WALL_V2 = "textures/objects/walls/wall_V2.png";
	public static final String TEX_WALL_V3 = "textures/objects/walls/wall_V3.png";
	public static final String TEX_WALL_V4 = "textures/objects/walls/wall_V4.png";
	public static final String TEX_WALL_V5 = "textures/objects/walls/wall_V5.png";
	public static final String TEX_WALL_V6 = "textures/objects/walls/wall_V6.png";

	// Player + weapon
	public static final String TEX_WEAPON_SWORD_BLUE = "textures/weapons/sword_blue.png";
	public static final String TEX_WEAPON_SWORD_RAINBOW = "textures/weapons/sword_rainbow.png";
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
	public static final String TEX_MAP_02 = "textures/maps/map_02.png";

	private final AssetManager assetManager = new AssetManager();

	private static String normalizeWallTextureRef(String ref) {
		if (ref == null || ref.isBlank()) {
			return TEX_WALL_ORIGINAL;
		}
		String trimmed = ref.trim();
		// Already a full internal assets path.
		if (trimmed.startsWith("textures/")) {
			return trimmed;
		}

		String filename = trimmed;
		// Legacy: "Wall-A1.png" -> "wall_A1.png"
		if (filename.startsWith("Wall-")) {
			filename = "wall_" + filename.substring("Wall-".length());
		}
		// Legacy: "Map-01.png" etc. isn't a wall; but for walls we just normalize case a bit.
		if (filename.equalsIgnoreCase("wall.png")) {
			filename = "wall.png";
		}
		if (filename.toLowerCase().startsWith("wall-") && filename.toLowerCase().endsWith(".png")) {
			// Another possible legacy: "wall-A1.png" -> "wall_A1.png"
			filename = "wall_" + filename.substring("wall-".length());
		}
		return "textures/objects/walls/" + filename;
	}

	/**
	 * Loads all gameplay textures (non-menu UI) and blocks until finished.
	 */
	public void load() {
		// World
		assetManager.load(TEX_BLOOD_PUDDLE, Texture.class);
		assetManager.load(TEX_CHEST_CLOSED, Texture.class);
		assetManager.load(TEX_CHEST_OPEN, Texture.class);

		// Walls
		assetManager.load(TEX_WALL_ORIGINAL, Texture.class);
		assetManager.load(TEX_WALL_A1, Texture.class);
		assetManager.load(TEX_WALL_A2, Texture.class);
		assetManager.load(TEX_WALL_A3, Texture.class);
		assetManager.load(TEX_WALL_A4, Texture.class);
		assetManager.load(TEX_WALL_H1, Texture.class);
		assetManager.load(TEX_WALL_H2, Texture.class);
		assetManager.load(TEX_WALL_V1, Texture.class);
		assetManager.load(TEX_WALL_V2, Texture.class);
		assetManager.load(TEX_WALL_V3, Texture.class);
		assetManager.load(TEX_WALL_V4, Texture.class);
		assetManager.load(TEX_WALL_V5, Texture.class);
		assetManager.load(TEX_WALL_V6, Texture.class);

		// Player + weapons
		assetManager.load(TEX_WEAPON_SWORD_BLUE, Texture.class);
		assetManager.load(TEX_WEAPON_SWORD_RAINBOW, Texture.class);
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
		assetManager.load(TEX_MAP_02, Texture.class);

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
	 * Resolves a wall texture reference coming from level JSON.
	 *
	 * <p>Accepts either:</p>
	 * <ul>
	 *   <li>full internal path (e.g. {@code textures/objects/walls/wall_A1.png})</li>
	 *   <li>filename only (e.g. {@code wall_A1.png} or {@code wall.png})</li>
	 *   <li>legacy filenames (e.g. {@code Wall-A1.png})</li>
	 * </ul>
	 */
	public Texture getWallTexture(String jsonTextureRef) {
		String internalPath = normalizeWallTextureRef(jsonTextureRef);
		if (assetManager.isLoaded(internalPath, Texture.class)) {
			return getTexture(internalPath);
		}
		// In case walls are referenced that aren't part of the preload list yet,
		// keep behavior compatible and load them on demand.
		return createTexture(internalPath);
	}

	/**
	 * Disposes the underlying {@link AssetManager} and all assets loaded through it.
	 */
	public void dispose() {
		assetManager.dispose();
	}
}
