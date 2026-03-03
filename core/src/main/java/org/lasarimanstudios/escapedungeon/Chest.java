package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Chest extends Sprite {

	private final float duration;
	private float elapsedTime;

	private final LevelScreen levelScreen;


	public Chest(float posX, float posY, float duration, LevelScreen levelScreen) {
		super(new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("textures/objects/chest.png")));
		setBounds(posX, posY, 4, 4);
		setOriginCenter();
		this.duration = duration;
		this.elapsedTime = 0f;
		this.levelScreen = levelScreen;
	}





	public void update(float delta) {
			elapsedTime += delta;
			if (elapsedTime >= duration) this.levelScreen.getChest().removeValue(this, true);

	}
}
