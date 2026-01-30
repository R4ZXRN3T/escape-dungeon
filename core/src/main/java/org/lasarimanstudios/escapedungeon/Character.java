package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Character extends Sprite {
	private static final float FRONT_ANGLE_OFFSET_DEG = -90f;

	private final Vector2 mouseWorld = new Vector2();

	private Array<Wall> wallArray;

	public Character(Array<Wall> wallArray, String texture, float width, float height) {
		super(new Texture(Gdx.files.internal("textures/characters/" + texture)));
		setSize(width, height);
		this.wallArray = wallArray;
		setOriginCenter();
	}

	public void run(Viewport viewport) {
		movement();
		rotateToMouse(viewport);
	}

	private void rotateToMouse(Viewport viewport) {
		viewport.unproject(mouseWorld.set(Gdx.input.getX(), Gdx.input.getY()));

		float cx = getX() + getWidth() * 0.5f;
		float cy = getY() + getHeight() * 0.5f;

		float dx = mouseWorld.x - cx;
		float dy = mouseWorld.y - cy;

		float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx)) + FRONT_ANGLE_OFFSET_DEG;
		setRotation(angleDeg);
	}

	private void movement() {
		float speed = 22f;
		float delta = Gdx.graphics.getDeltaTime();
		float diagonalMultiplier = 2f / 3f;
		float oldX = this.getX();
		float oldY = this.getY();

		if ((Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.W)) ||
			(Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.D))) {
			return;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
			translateX(speed * delta * diagonalMultiplier);
			translateY(-speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
			translateX(-speed * delta * diagonalMultiplier);
			translateY(speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) {
			translateX(speed * delta * diagonalMultiplier);
			translateY(speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) {
			translateX(-speed * delta * diagonalMultiplier);
			translateY(-speed * delta * diagonalMultiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			translateX(speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			translateX(-speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			translateY(speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			translateY(-speed * delta);
		}

		for(Wall wall : this.wallArray) {
			if(this.getBoundingRectangle().overlaps(wall.getBoundingRectangle())) {
				if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
					setPosition(oldX, oldY);
				} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
					setPosition(oldX, oldY);
				}
			}
		}
	}
}
