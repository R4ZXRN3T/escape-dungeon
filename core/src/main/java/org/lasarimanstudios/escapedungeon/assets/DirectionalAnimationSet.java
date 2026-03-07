package org.lasarimanstudios.escapedungeon.assets;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

/**
 * Holds one {@link Animation} per {@link Direction}.
 */
public class DirectionalAnimationSet {
	private final EnumMap<Direction, Animation<TextureRegion>> animations;

	public DirectionalAnimationSet(EnumMap<Direction, Animation<TextureRegion>> animations) {
		this.animations = new EnumMap<>(animations);
	}

	public Animation<TextureRegion> get(Direction direction) {
		Animation<TextureRegion> anim = animations.get(direction);
		if (anim == null) throw new IllegalArgumentException("Missing animation for direction: " + direction);
		return anim;
	}

	public TextureRegion getKeyFrame(Direction direction, float stateTimeSeconds, boolean looping) {
		return get(direction).getKeyFrame(stateTimeSeconds, looping);
	}
}

