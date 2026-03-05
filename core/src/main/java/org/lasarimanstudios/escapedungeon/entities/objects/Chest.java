package org.lasarimanstudios.escapedungeon.entities.objects;

import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Entity;

/**
 * A chest entity that exists for a limited time.
 *
 * <p>The chest is currently a non-interactive world object. It expires after a configured duration.
 * The owning world/system is responsible for calling {@link #update(float)} every frame and removing
 * the chest once {@link #isExpired()} returns {@code true}.</p>
 */
public class Chest extends Entity {

	private final float duration;
	private float elapsedTime;
	private boolean expired;

	/**
	 * Creates a chest sprite at the given position.
	 *
	 * <p>The chest uses a fixed sprite size of {@code 4x4} world units.</p>
	 *
	 * @param texture  chest texture (must already be loaded)
	 * @param posX     x position in world units
	 * @param posY     y position in world units
	 * @param duration lifetime in seconds
	 */
	public Chest(Texture texture, float posX, float posY, float duration) {
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
	 * Returns whether this chest should be considered removed from the world.
	 *
	 * @return {@code true} once its lifetime has elapsed
	 */
	public boolean isExpired() {
		return expired;
	}
}
