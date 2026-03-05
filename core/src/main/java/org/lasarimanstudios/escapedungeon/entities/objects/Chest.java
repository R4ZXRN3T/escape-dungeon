package org.lasarimanstudios.escapedungeon.entities.objects;

import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Entity;

public class Chest extends Entity {

	private final float duration;
	private float elapsedTime;
	private boolean expired;

	public Chest(Texture texture, float posX, float posY, float duration) {
		super(texture);
		setBounds(posX, posY, 4, 4);
		setOriginCenter();
		this.duration = duration;
		this.elapsedTime = 0f;
		this.expired = false;
	}

	public void update(float delta) {
		if (expired) return;
		elapsedTime += delta;
		if (elapsedTime >= duration) expired = true;
	}

	public boolean isExpired() {
		return expired;
	}
}
