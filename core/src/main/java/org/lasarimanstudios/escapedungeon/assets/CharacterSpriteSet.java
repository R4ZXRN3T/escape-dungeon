package org.lasarimanstudios.escapedungeon.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A set of directional idle and walk textures for a single enemy type.
 *
 * <p>Instances are built by {@link GameAssets#getEnemySpriteSet(String)} which auto-discovers
 * textures from the loaded asset list using a naming convention:</p>
 *
 * <h3>Naming conventions</h3>
 * <p>Files must live under {@code textures/enemy/<id>/} and follow <strong>one</strong> of two
 * schemes. The system auto-detects which one is in use.</p>
 *
 * <h4>Scheme B – English names (preferred for new art)</h4>
 * <pre>
 *   &lt;id&gt;_front.png           → FRONT idle
 *   &lt;id&gt;_front_walk_1.png    → FRONT walk frame 1
 *   &lt;id&gt;_back.png            → BACK idle
 *   &lt;id&gt;_left.png            → LEFT idle
 *   &lt;id&gt;_right.png           → RIGHT idle
 *   … etc.
 * </pre>
 */
public class CharacterSpriteSet {

	private final EnumMap<Direction, TextureRegion> idleFrames;
	private final EnumMap<Direction, List<TextureRegion>> walkFrames;

	public CharacterSpriteSet(
		EnumMap<Direction, TextureRegion> idleFrames,
		EnumMap<Direction, List<TextureRegion>> walkFrames
	) {
		this.idleFrames = new EnumMap<>(idleFrames);
		this.walkFrames = new EnumMap<>(Direction.class);
		for (var entry : walkFrames.entrySet()) {
			this.walkFrames.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
	}

	/**
	 * Builds an {@link EnemySpriteSet} by scanning the provided asset paths for files matching
	 * the naming conventions described in the class Javadoc.
	 *
	 * @param assets      used to retrieve already-loaded textures
	 * @param characterId folder name under {@code textures/enemy/}, e.g. {@code "goblin-01"} or {@code "ghost"}
	 * @param allPaths    all auto-discovered texture paths (from {@link GameAssets#getLoadedPaths()})
	 */
	public static CharacterSpriteSet build(GameAssets assets, String characterId, List<String> allPaths) {
		String prefix = "textures/character/" + characterId + "/";

		// Collect only paths belonging to this enemy.
		List<String> characterPaths = new ArrayList<>();
		for (String p : allPaths) {
			if (p.startsWith(prefix)) {
				characterPaths.add(p);
			}
		}

		EnumMap<Direction, TextureRegion> idles = new EnumMap<>(Direction.class);
		EnumMap<Direction, List<TextureRegion>> walks = new EnumMap<>(Direction.class);

		for (String path : characterPaths) {
			// Strip prefix and extension: "goblin-01-vorne-stehend.png" -> "goblin-01-vorne-stehend"
			String filename = path.substring(prefix.length());
			String name = filename.endsWith(".png") ? filename.substring(0, filename.length() - 4) : filename;
			String lower = name.toLowerCase();

			Direction dir = detectDirection(lower);
			if (dir == null) continue; // Can't map this file; skip.

			boolean isWalk = isWalkFrame(lower);
			boolean isIdle = isIdleFrame(lower);

			if (isIdle) {
				idles.put(dir, new TextureRegion(assets.getTexture(path)));
			} else if (isWalk) {
				walks.computeIfAbsent(dir, d -> new ArrayList<>())
					.add(new TextureRegion(assets.getTexture(path)));
			} else {
				// Single-frame per direction (e.g. ghost_front.png with no _idle / _stehend suffix) → treat as idle.
				if (!idles.containsKey(dir)) {
					idles.put(dir, new TextureRegion(assets.getTexture(path)));
				}
			}
		}

		if (idles.isEmpty()) {
			throw new IllegalArgumentException("No idle frames found for character '" + characterId
				+ "'. Expected files matching convention under " + prefix);
		}

		// Walk frames are already in deterministic order because assets.txt is sorted alphabetically.
		return new CharacterSpriteSet(idles, walks);
	}

	/**
	 * Detects the {@link Direction} from a lower-cased filename stem.
	 */
	private static Direction detectDirection(String lower) {
		// English naming
		if (lower.contains("front")) return Direction.FRONT;
		if (lower.contains("back")) return Direction.BACK;
		if (lower.contains("left")) return Direction.LEFT;
		if (lower.contains("right")) return Direction.RIGHT;
		return null;
	}

	/**
	 * Returns true if the filename indicates an idle / standing frame.
	 */
	private static boolean isIdleFrame(String lower) {
		return lower.contains("stehend") || lower.contains("idle") || lower.contains("standing");
	}

	/**
	 * Returns true if the filename indicates a walk / movement frame.
	 */
	private static boolean isWalkFrame(String lower) {
		return lower.contains("laufen") || lower.contains("walk") || lower.contains("run");
	}

	// ── Builder: turns a flat list of loaded paths into an EnemySpriteSet ──────────

	/**
	 * Returns the idle texture for a given direction, falling back to FRONT if the direction
	 * has no dedicated frame.
	 */
	public TextureRegion getIdle(Direction direction) {
		TextureRegion region = idleFrames.get(direction);
		if (region != null) return region;
		// Fall back to FRONT (always present).
		return idleFrames.get(Direction.FRONT);
	}

	// ── Private helpers ───────────────────────────────────────────────────────────

	/**
	 * Returns the idle texture for FRONT.
	 */
	public Texture getFrontIdleTexture() {
		return getIdle(Direction.FRONT).getTexture();
	}

	/**
	 * Creates a {@link DirectionalAnimationSet} for walking.
	 *
	 * <p>Directions that have no walk frames fall back to the idle frame as a single-frame
	 * animation, so callers don't need to handle the missing-walk-frames case.</p>
	 */
	public DirectionalAnimationSet createWalkAnimations(float frameDurationSeconds) {
		EnumMap<Direction, Animation<TextureRegion>> map = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			List<TextureRegion> frames = walkFrames.get(dir);
			if (frames != null && !frames.isEmpty()) {
				map.put(dir, new Animation<>(frameDurationSeconds, frames.toArray(new TextureRegion[0])));
			} else {
				// Fall back: single-frame animation from idle.
				map.put(dir, new Animation<>(frameDurationSeconds, getIdle(dir)));
			}
		}
		return new DirectionalAnimationSet(map);
	}
}
