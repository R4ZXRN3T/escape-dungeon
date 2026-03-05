package org.lasarimanstudios.escapedungeon.entities.objects;

import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Entity;

public class BloodPuddle extends Entity {
	private final float duration; // Duration in seconds before the puddle disappears
	private float elapsedTime; // Time elapsed since the puddle was created
	private boolean expired;

	public BloodPuddle(Texture texture, float posX, float posY, float duration) {
		super(texture);
		setBounds(posX, posY, 4, 4);
		setOriginCenter();
		this.duration = duration;
		this.elapsedTime = 0f;
		this.expired = false;
	}

	/**
	 * Updates the blood puddle's state. Should be called every frame.
	 *
	 * @param delta Time in seconds since the last update call.
	 */
	public void update(float delta) {
		if (expired) return;
		elapsedTime += delta;
		if (elapsedTime >= duration) expired = true;
	}

	public boolean isExpired() {
		return expired;
	}
}
