package org.lasarimanstudios.escapedungeon.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.Entity;

/**
 * A chest entity that exists for a limited time.
 *
 * <p>The chest can be opened by the player when nearby, restoring some health.
 * It expires after a configured duration. The owning world/system is responsible for calling
 * {@link #update(float, Character)} every frame and removing the chest once {@link #isExpired()}
 * returns {@code true}.</p>
 */
public class Chest extends Entity {

	private final float duration;
	private final Texture openTexture;
	private float elapsedTime;
	private boolean expired;
	private boolean opened;

	/**
	 * Creates a chest sprite at the given position.
	 *
	 * <p>The chest uses a fixed sprite size of {@code 4×4} world units.</p>
	 *
	 * @param closedTexture closed chest texture (must already be loaded)
	 * @param openTexture   open chest texture (must already be loaded)
	 * @param posX          X position in world units
	 * @param posY          Y position in world units
	 * @param duration      lifetime in seconds
	 */
	public Chest(Texture closedTexture, Texture openTexture, float posX, float posY, float duration) {
		super(closedTexture);
		this.openTexture = openTexture;
		setBounds(posX, posY, 4, 4);
		setOriginCenter();
		this.duration = duration;
		this.elapsedTime = 0f;
		this.expired = false;
	}

	/**
	 * Opens the chest and restores a small amount of the player's health.
	 *
	 * <p>Has no effect if the chest is already open.</p>
	 *
	 * @param player the player character to heal
	 */
	public void open(Character player) {
		if (opened) return;

		opened = true;
		setTexture(openTexture);
		if (player.getRemainingHealth() < 100) {
			player.setRemainingHealth(player.getRemainingHealth() + 10);
		}
	}

	/**
	 * Advances the internal lifetime timer and opens the chest if the player is nearby
	 * and pressing the interact key.
	 *
	 * @param delta  time since last frame in seconds
	 * @param player the player character
	 */
	public void update(float delta, Character player) {
		if (expired) return;

		elapsedTime += delta;

		if (elapsedTime >= duration) {
			expired = true;
		}

		if (!opened && isPlayerNear(player)) {
			if (Gdx.input.isKeyPressed(Input.Keys.E)) {
				open(player);
			}
		}
	}

	/**
	 * Returns whether the player is within interaction range of this chest.
	 *
	 * @param player the player character
	 * @return {@code true} if the distance to the player is less than 5 world units
	 */
	public boolean isPlayerNear(Character player) {
		float dx = getX() - player.getX();
		float dy = getY() - player.getY();
		float distance = (float) Math.sqrt(dx * dx + dy * dy);
		return distance < 5f;
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
