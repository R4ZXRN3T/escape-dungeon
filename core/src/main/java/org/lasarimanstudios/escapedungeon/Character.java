package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Character extends Sprite {
	private static final float FRONT_ANGLE_OFFSET_DEG = -90f;

	private static final float SPEED = 22f;						// Character speed in units per second.
	private static final float DIAGONAL_MULTIPLIER = 2f / 3f;	// To keep diagonal speed consistent with axial speed.
	private static final float MAX_STEP_DISTANCE = 0.25f;		// Max distance per movement sub-step to avoid tunneling.
	private static final float MAX_DELTA = 1f / 30f;			// Cap delta time to avoid large steps on frame drops.

	private final Vector3 mouseWorld = new Vector3();
	private final Array<Wall> wallArray;

	// Stable collider that ignores sprite rotation.
	private final Rectangle collider = new Rectangle();

	public Character(Array<Wall> wallArray, String texture, float width, float height) {
		super(new Texture(Gdx.files.internal("textures/characters/" + texture)));
		setSize(width, height);
		this.wallArray = wallArray;
		setOriginCenter();
		updateCollider();
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
		float delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_DELTA);

		float moveX = 0f;
		float moveY = 0f;

		if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1f;
		if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1f;
		if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1f;
		if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1f;

		if (moveX != 0f && moveY != 0f) {
			moveX *= DIAGONAL_MULTIPLIER;
			moveY *= DIAGONAL_MULTIPLIER;
		}

		float totalDx = moveX * SPEED * delta;
		float totalDy = moveY * SPEED * delta;

		// Sub-step to avoid tunneling and corner embedding.
		float distance = (float) Math.sqrt(totalDx * totalDx + totalDy * totalDy);
		int steps = Math.max(1, (int) Math.ceil(distance / MAX_STEP_DISTANCE));
		float stepDx = totalDx / steps;
		float stepDy = totalDy / steps;

		for (int i = 0; i < steps; i++) {
			moveWithCollisions(stepDx, stepDy);
		}
	}

	private void moveWithCollisions(float dx, float dy) {
		// X axis
		if (dx != 0f) {
			float oldX = getX();
			setX(oldX + dx);
			updateCollider();

			if (overlapsAnyWall()) {
				setX(oldX);
				updateCollider();
			}
		}

		// Y axis
		if (dy != 0f) {
			float oldY = getY();
			setY(oldY + dy);
			updateCollider();

			if (overlapsAnyWall()) {
				setY(oldY);
				updateCollider();
			}
		}
	}

	private void updateCollider() {
		collider.set(getX(), getY(), getWidth(), getHeight());
	}

	private boolean overlapsAnyWall() {
		for (Wall wall : wallArray) {
			// Wall collider is also an AABB rectangle.
			if (collider.overlaps(wall.getBoundingRectangle())) return true;
		}
		return false;
	}
}
