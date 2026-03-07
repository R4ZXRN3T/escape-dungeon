package org.lasarimanstudios.escapedungeon.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.Entity;

/**
 * A chest entity that exists for a limited time.
 *
 * <p>The chest is currently a non-interactive world object. It expires after a configured duration.
 * The owning world/system is responsible for calling {@link #update(float, Character)} every frame and removing
 * the chest once {@link #isExpired()} returns {@code true}.</p>
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
	 * <p>The chest uses a fixed sprite size of {@code 4x4} world units.</p>
	 *
	 * @param closedTexture chest texture (must already be loaded)
	 * @param openTexture   chest texture (must already be loaded)
	 * @param posX          x position in world units
	 * @param posY          y position in world units
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

	public void open() {
		if (opened) return;

		opened = true;
		setTexture(openTexture);

	}

	public void update(float delta, Character player) {
		if (expired) return;

		elapsedTime += delta;

		if (elapsedTime >= duration) {
			expired = true;
		}


		if (!opened && isPlayerNear(player)) {

			if (Gdx.input.isKeyPressed(Input.Keys.E)) {
				open();
			}
		}
	}

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
