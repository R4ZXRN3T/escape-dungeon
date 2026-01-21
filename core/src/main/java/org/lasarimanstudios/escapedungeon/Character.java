package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Character extends Sprite {

	public Character(String texture, float width, float height) {
		super();
		this.setTexture(new Texture(texture));
		this.setSize(width, height);
		this.setOriginCenter();
	}

	public void run() {
		movement();
	}

	private void movement() {
		float speed = 22f;
		float delta = Gdx.graphics.getDeltaTime();

		float diagonalMultiplier = 2f / 3f;

		if ((Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.W)) || (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.D))) {
			return;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
			this.translateX(speed * delta * diagonalMultiplier);
			this.translateY(-speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
			this.translateX(-speed * delta * diagonalMultiplier);
			this.translateY(speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) {
			this.translateX(speed * delta * diagonalMultiplier);
			this.translateY(speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) {
			this.translateX(-speed * delta * diagonalMultiplier);
			this.translateY(-speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			this.translateX(speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			this.translateX(-speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			this.translateY(speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			this.translateY(-speed * delta);
		}
	}
}
