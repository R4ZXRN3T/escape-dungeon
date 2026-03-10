package org.lasarimanstudios.escapedungeon.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

/**
 * Holds one {@link Animation} per {@link Direction}.
 */
public class DirectionalAnimationSet {
	private final EnumMap<Direction, Animation<TextureRegion>> animations;

	/**
	 * Creates a new directional animation set from the given map.
	 *
	 * @param animations map of animations keyed by direction
	 */
	public DirectionalAnimationSet(EnumMap<Direction, Animation<TextureRegion>> animations) {
		this.animations = new EnumMap<>(animations);
	}

	/**
	 * Returns the animation for the given direction.
	 *
	 * @param direction the facing direction
	 * @return the animation for that direction
	 * @throws IllegalArgumentException if no animation exists for the given direction
	 */
	public Animation<TextureRegion> get(Direction direction) {
		Animation<TextureRegion> anim = animations.get(direction);
		if (anim == null) throw new IllegalArgumentException("Missing animation for direction: " + direction);
		return anim;
	}

	/**
	 * Returns the key frame for the given direction at the specified time.
	 *
	 * @param direction        the facing direction
	 * @param stateTimeSeconds elapsed animation time in seconds
	 * @param looping          whether the animation should loop
	 * @return the texture region for the current frame
	 */
	public TextureRegion getKeyFrame(Direction direction, float stateTimeSeconds, boolean looping) {
		return get(direction).getKeyFrame(stateTimeSeconds, looping);
	}
}
