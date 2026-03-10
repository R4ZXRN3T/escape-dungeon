package org.lasarimanstudios.escapedungeon.entities.objects;

import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Entity;

/**
 * A temporary blood puddle spawned as a death effect.
 *
 * <p>The puddle expires after {@code duration} seconds. The owning world/system should call
 * {@link #update(float)} every frame and remove the puddle when {@link #isExpired()} becomes
 * {@code true}.</p>
 */
public class BloodPuddle extends Entity {
	private final float duration;
	private float elapsedTime;
	private boolean expired;

	/**
	 * Creates a blood puddle sprite at the given position.
	 *
	 * <p>The puddle uses a fixed sprite size of {@code 4x4} world units.</p>
	 *
	 * @param texture  puddle texture (must already be loaded)
	 * @param posX     x position in world units
	 * @param posY     y position in world units
	 * @param duration lifetime in seconds
	 */
	public BloodPuddle(Texture texture, float posX, float posY, float duration) {
		super(texture);
		setBounds(posX, posY, 4, 4);
		setOriginCenter();
		this.duration = duration;
		this.elapsedTime = 0f;
		this.expired = false;
	}

	/**
	 * Advances the internal lifetime timer.
	 *
	 * @param delta time since last frame in seconds
	 */
	public void update(float delta) {
		if (expired) return;
		elapsedTime += delta;
		if (elapsedTime >= duration) expired = true;
	}

	/**
	 * @return {@code true} once its lifetime has elapsed
	 */
	public boolean isExpired() {
		return expired;
	}
}
