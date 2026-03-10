package org.lasarimanstudios.escapedungeon.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A set of directional idle and walk textures for a single character type.
 *
 * <p>Instances are built by {@link AssetManager#getCharacterSpriteSet(String)} which auto-discovers
 * textures from the loaded asset list using a naming convention:</p>
 *
 * <h3>Naming conventions</h3>
 * <p>Files must live under {@code textures/character/<id>/} and follow the scheme below.
 * The system auto-detects which naming variant is in use.</p>
 *
 * <h4>English names (preferred for new art)</h4>
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

	/**
	 * Creates a new character sprite set from pre-built idle and walk frame maps.
	 *
	 * @param idleFrames map of idle texture regions per direction
	 * @param walkFrames map of walk texture region lists per direction
	 */
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
	 * Builds a {@link CharacterSpriteSet} by scanning the provided asset paths for files matching
	 * the naming conventions described in the class Javadoc.
	 *
	 * @param assets      used to retrieve already-loaded textures
	 * @param characterId folder name under {@code textures/character/}, e.g. {@code "knight"}
	 * @param allPaths    all auto-discovered texture paths (from {@link AssetManager#getLoadedPaths()})
	 * @return the built sprite set (never {@code null})
	 * @throws IllegalArgumentException if no idle frames are found for the given character id
	 */
	public static CharacterSpriteSet build(AssetManager assets, String characterId, List<String> allPaths) {
		String prefix = "textures/character/" + characterId + "/";

		List<String> characterPaths = new ArrayList<>();
		for (String p : allPaths) {
			if (p.startsWith(prefix)) {
				characterPaths.add(p);
			}
		}

		EnumMap<Direction, TextureRegion> idles = new EnumMap<>(Direction.class);
		EnumMap<Direction, List<TextureRegion>> walks = new EnumMap<>(Direction.class);

		for (String path : characterPaths) {
			String filename = path.substring(prefix.length());
			String name = filename.endsWith(".png") ? filename.substring(0, filename.length() - 4) : filename;
			String lower = name.toLowerCase();

			Direction dir = detectDirection(lower);
			if (dir == null) continue;

			boolean isWalk = isWalkFrame(lower);
			boolean isIdle = isIdleFrame(lower);

			if (isIdle) {
				idles.put(dir, new TextureRegion(assets.getTexture(path)));
			} else if (isWalk) {
				walks.computeIfAbsent(dir, d -> new ArrayList<>())
					.add(new TextureRegion(assets.getTexture(path)));
			} else {
				if (!idles.containsKey(dir)) {
					idles.put(dir, new TextureRegion(assets.getTexture(path)));
				}
			}
		}

		if (idles.isEmpty()) {
			throw new IllegalArgumentException("No idle frames found for character '" + characterId
				+ "'. Expected files matching convention under " + prefix);
		}

		return new CharacterSpriteSet(idles, walks);
	}

	/**
	 * Detects the {@link Direction} from a lower-cased filename stem.
	 *
	 * @param lower lower-cased filename without extension
	 * @return the detected direction, or {@code null} if none matched
	 */
	private static Direction detectDirection(String lower) {
		if (lower.contains("front")) return Direction.FRONT;
		if (lower.contains("back")) return Direction.BACK;
		if (lower.contains("left")) return Direction.LEFT;
		if (lower.contains("right")) return Direction.RIGHT;
		return null;
	}

	/**
	 * Returns {@code true} if the filename indicates an idle / standing frame.
	 *
	 * @param lower lower-cased filename without extension
	 * @return {@code true} if the filename contains an idle keyword
	 */
	private static boolean isIdleFrame(String lower) {
		return lower.contains("stehend") || lower.contains("idle") || lower.contains("standing");
	}

	/**
	 * Returns {@code true} if the filename indicates a walk / movement frame.
	 *
	 * @param lower lower-cased filename without extension
	 * @return {@code true} if the filename contains a walk keyword
	 */
	private static boolean isWalkFrame(String lower) {
		return lower.contains("laufen") || lower.contains("walk") || lower.contains("run");
	}

	/**
	 * Returns the idle texture for a given direction, falling back to {@link Direction#FRONT}
	 * if the direction has no dedicated frame.
	 *
	 * @param direction the desired facing direction
	 * @return the idle texture region (never {@code null} if a FRONT idle exists)
	 */
	public TextureRegion getIdle(Direction direction) {
		TextureRegion region = idleFrames.get(direction);
		if (region != null) return region;
		return idleFrames.get(Direction.FRONT);
	}

	/**
	 * Creates a {@link DirectionalAnimationSet} for walking.
	 *
	 * <p>Directions that have no walk frames fall back to the idle frame as a single-frame
	 * animation, so callers don't need to handle the missing-walk-frames case.</p>
	 *
	 * @param frameDurationSeconds duration of each animation frame in seconds
	 * @return a directional animation set for walking
	 */
	public DirectionalAnimationSet createWalkAnimations(float frameDurationSeconds) {
		EnumMap<Direction, Animation<TextureRegion>> map = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			List<TextureRegion> frames = walkFrames.get(dir);
			if (frames != null && !frames.isEmpty()) {
				map.put(dir, new Animation<>(frameDurationSeconds, frames.toArray(new TextureRegion[0])));
			} else {
				map.put(dir, new Animation<>(frameDurationSeconds, getIdle(dir)));
			}
		}
		return new DirectionalAnimationSet(map);
	}
}
