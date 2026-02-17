package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class BloodPuddle extends Sprite {
	private final float duration; // Duration in seconds before the puddle disappears
	private float elapsedTime; // Time elapsed since the puddle was created

	private final LevelScreen levelScreen;

	public BloodPuddle(float posX, float posY, float duration, LevelScreen levelScreen) {
		super(new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("textures/enemy/blood-puddle/blood-puddle.png")));
		setBounds(posX, posY, 4, 4);
		setOriginCenter();
		this.duration = duration;
		this.elapsedTime = 0f;
		this.levelScreen = levelScreen;
	}

	/**
	 * Updates the blood puddle's state. Should be called every frame.
	 *
	 * @param delta Time in seconds since the last update call.
	 */
	public void update(float delta) {
		elapsedTime += delta;
		if (elapsedTime >= duration) this.levelScreen.getBloodPuddles().removeValue(this, true);
	}
}
