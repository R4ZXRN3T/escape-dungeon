// core/src/main/java/org/lasarimanstudios/escapedungeon/Character.java
package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import org.lasarimanstudios.escapedungeon.weapons.Sword;
import org.lasarimanstudios.escapedungeon.weapons.Weapon;

/**
 * Player-controlled character sprite with keyboard movement, mouse-facing rotation, and simple AABB
 * collision against a set of {@link Wall} rectangles.
 *
 * <p>Movement uses sub-stepping to reduce tunneling through thin walls. Collision is performed with an
 * axis-aligned {@link Rectangle} collider that intentionally ignores sprite rotation.</p>
 */
public class Character extends Sprite {
	private static final float FRONT_ANGLE_OFFSET_DEG = -90f;

	private static final float SPEED = 22f;                     // Character speed in units per second.
	private static final float DIAGONAL_MULTIPLIER = 2f / 3f;   // To keep diagonal speed consistent with axial speed.
	private static final float MAX_STEP_DISTANCE = 0.25f;       // Max distance per movement sub-step to avoid tunneling.
	private static final float MAX_DELTA = 1f / 30f;            // Cap delta time to avoid large steps on frame drops.

	private final Vector3 mouseWorld = new Vector3();
	private final Array<Wall> wallArray;

	// Stable collider that ignores sprite rotation.
	private final Rectangle collider = new Rectangle();

	private static int KEY_FORWARD;
	private static int KEY_BACKWARD;
	private static int KEY_LEFT;
	private static int KEY_RIGHT;
	private static int BUTTON_ATTACK;

	private final Weapon weapon;
	private final Vector2 weaponOffset = new Vector2(0.5f, -3f);

	/**
	 * Creates a character sprite using a texture from {@code textures/characters/}, sets its size and origin,
	 * and initializes its axis-aligned collider.
	 *
	 * @param wallArray walls used for collision checks (AABB vs AABB)
	 * @param texture   character texture file name (relative to {@code textures/characters/})
	 * @param width     sprite width in world units
	 * @param height    sprite height in world units
	 */
	public Character(Array<Wall> wallArray, String texture, float width, float height) {
		super(new Texture(Gdx.files.internal("textures/characters/" + texture)));
		setSize(width, height);
		this.wallArray = wallArray;
		setOriginCenter();
		updateCollider();

		KEY_FORWARD = ConfigManager.getInt(ConfigManager.ConfigKey.FORWARD_KEY, 0, 255);
		KEY_BACKWARD = ConfigManager.getInt(ConfigManager.ConfigKey.BACKWARD_KEY, 0, 255);
		KEY_LEFT = ConfigManager.getInt(ConfigManager.ConfigKey.LEFT_KEY, 0, 255);
		KEY_RIGHT = ConfigManager.getInt(ConfigManager.ConfigKey.RIGHT_KEY, 0, 255);
		BUTTON_ATTACK = ConfigManager.getInt(ConfigManager.ConfigKey.ATTACK_KEY, 0, 255);


		// Create the sword once; LevelScreen will draw it.
		this.weapon = new Sword("sword1.png", 10f, 0.20f, 1.5f);
		attachWeapon();
	}

	public Weapon getWeapon() {
		return weapon;
	}

	/**
	 * Updates character state for the current frame: applies movement input and rotates the sprite to face
	 * the mouse cursor in world space.
	 *
	 * @param camera camera used to unproject mouse screen coordinates into world coordinates
	 */
	public void run(OrthographicCamera camera) {
		movement();
		rotateToMouse(camera);

		if (Gdx.input.isButtonJustPressed(BUTTON_ATTACK)) {
			weapon.startAttack(getRotation());
		}

		// Keep sword attached and animate attacks.
		if (!weapon.isAttacking()) {
			attachWeapon();
		}

		weapon.attack();
	}

	/**
	 * Rotates the sprite so its "front" points toward the current mouse position.
	 *
	 * <p>The mouse screen coordinates are unprojected via the provided {@link OrthographicCamera}.</p>
	 *
	 * @param camera camera used to unproject mouse screen coordinates into world coordinates
	 */
	private void rotateToMouse(OrthographicCamera camera) {
		camera.unproject(mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0));

		float cx = getX() + getWidth() * 0.5f;
		float cy = getY() + getHeight() * 0.5f;

		float dx = mouseWorld.x - cx;
		float dy = mouseWorld.y - cy;

		float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx)) + FRONT_ANGLE_OFFSET_DEG;
		setRotation(angleDeg);
	}

	private void attachWeapon() {
		float cx = getX() + getWidth() * 0.5f;
		float cy = getY() + getHeight() * 0.5f;

		// Place slightly in front of the character based on facing.
		float rad = (float) Math.toRadians(getRotation() - FRONT_ANGLE_OFFSET_DEG);
		float ox = weaponOffset.x * (float) Math.cos(rad) - weaponOffset.y * (float) Math.sin(rad);
		float oy = weaponOffset.x * (float) Math.sin(rad) + weaponOffset.y * (float) Math.cos(rad);

		weapon.setPosition(cx - weapon.getWidth() * 0.5f + ox, cy - weapon.getHeight() * 0.5f + oy);

		// If you want the sword to follow facing even when not attacking:
		//if (!(weapon instanceof Sword)) {
		if (!weapon.isAttacking()) {
			weapon.setRotation(getRotation());
		}
		//}
	}

	private void movement() {
		float delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_DELTA);

		float moveX = 0f;
		float moveY = 0f;

		if (Gdx.input.isKeyPressed(KEY_FORWARD)) moveY += 1f;
		if (Gdx.input.isKeyPressed(KEY_BACKWARD)) moveY -= 1f;
		if (Gdx.input.isKeyPressed(KEY_RIGHT)) moveX += 1f;
		if (Gdx.input.isKeyPressed(KEY_LEFT)) moveX -= 1f;

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

	/**
	 * Attempts to move the character by the given delta while resolving collisions against walls.
	 *
	 * <p>Resolution is done per-axis (X then Y). If a move causes overlap with any wall, that axis movement
	 * is reverted.</p>
	 *
	 * @param dx movement delta on X axis (world units)
	 * @param dy movement delta on Y axis (world units)
	 */
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

	/**
	 * Updates the axis-aligned collider to match the sprite's current position and size.
	 *
	 * <p>Rotation does not affect the collider (AABB).</p>
	 */
	private void updateCollider() {
		collider.set(getX(), getY(), getWidth(), getHeight());
	}

	/**
	 * Checks whether the character collider overlaps any wall collider.
	 *
	 * @return {@code true} if overlapping at least one wall; {@code false} otherwise
	 */
	private boolean overlapsAnyWall() {
		for (Wall wall : wallArray) {
			// Wall collider is also an AABB rectangle.
			if (collider.overlaps(wall.getBoundingRectangle())) return true;
		}
		return false;
	}
}
