package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Character extends Sprite {
	private static final float FRONT_ANGLE_OFFSET_DEG = -90f;

	private final Vector3 mouseWorld = new Vector3();

	private final Array<Wall> wallArray;

	public Character(Array<Wall> wallArray, String texture, float width, float height) {
		super(new Texture(Gdx.files.internal("textures/characters/" + texture)));
		setSize(width, height);
		this.wallArray = wallArray;
		setOriginCenter();
		setPosition(960, 540);
	}

	public void run(OrthographicCamera camera) {
		movement();
		rotateToMouse(camera);
	}

	private void rotateToMouse(OrthographicCamera camera) {
		camera.unproject(mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0));

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

		float moveX = 0f;
		float moveY = 0f;

		if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1;
		if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1;
		if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1;
		if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1;

		if (moveX != 0 && moveY != 0) {
			moveX *= diagonalMultiplier;
			moveY *= diagonalMultiplier;
		}

		float oldX = getX();
		translateX(moveX * speed * delta);

		for (Wall wall : wallArray) {
			if (getBoundingRectangle().overlaps(wall.getBoundingRectangle())) {
				setX(oldX);
				break;
			}
		}

		float oldY = getY();
		translateY(moveY * speed * delta);

		for (Wall wall : wallArray) {
			if (getBoundingRectangle().overlaps(wall.getBoundingRectangle())) {
				setY(oldY);
				break;
			}
		}
	}
}
