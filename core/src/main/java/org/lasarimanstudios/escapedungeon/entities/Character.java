package org.lasarimanstudios.escapedungeon.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.ConfigManager;
import org.lasarimanstudios.escapedungeon.assets.Direction;
import org.lasarimanstudios.escapedungeon.assets.DirectionalAnimationSet;
import org.lasarimanstudios.escapedungeon.assets.EnemySpriteSet;
import org.lasarimanstudios.escapedungeon.assets.GameAssets;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.entities.weapons.SwordType;
import org.lasarimanstudios.escapedungeon.entities.weapons.Weapon;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;


/**
 * Player-controlled character.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Movement via configured input keys</li>
 *   <li>Facing/rotation toward the mouse cursor</li>
 *   <li>Simple AABB collision against {@link Wall} instances</li>
 *   <li>Owning and updating the currently equipped {@link Weapon}</li>
 * </ul>
 *
 * <p>Collision uses an axis-aligned {@link Rectangle} collider that intentionally ignores sprite
 * rotation.</p>
 */
public class Character extends Entity {
	private static final float KNOCKBACK_VELOCITY_EPS = 0.05f;
	private static final float KNOCKBACK_DAMPING_PER_SECOND = 18f;
	private static final float FRONT_ANGLE_OFFSET_DEG = -90f;
	private static final float SPEED = 22f;                     // Character speed in units per second.
	private static final float DIAGONAL_MULTIPLIER = 2f / 3f;   // To keep diagonal speed consistent with axial speed.
	private static final float MAX_STEP_DISTANCE = 0.25f;       // Max distance per movement sub-step to avoid tunneling.
	private static final float MAX_DELTA = 1f / 30f;            // Cap delta time to avoid large steps on frame drops.
	private static int KEY_FORWARD;
	private static int KEY_BACKWARD;
	private static int KEY_LEFT;
	private static int KEY_RIGHT;
	private static int BUTTON_ATTACK;
	private final Vector3 mouseWorld = new Vector3();
	private final Array<Wall> wallArray;
	private final Array<Enemy> enemyArray;

	// Stable collider that ignores sprite rotation.
	private final Rectangle collider = new Rectangle();
	private final Weapon weapon;
	private final Vector2 weaponOffsetLocal = new Vector2(0f, 0f);
	private final Vector2 weaponOffsetWorld = new Vector2();
	private final GameAssets assets;
	private float MaxHealth;
	private float RemainingHealth;
	private float knockbackVx = 0f;
	private float knockbackVy = 0f;
	private float damageInvulnerabilityTime = 1f;
	private boolean isDead;
	private DeathListener deathListener;
	private Direction facing = Direction.FRONT;
	private float stateTimeSeconds = 0f;
	private boolean walking = false;

	private float currentAttackAngleDeg = 0f;

	// Toggle: when true the player sprite faces the mouse cursor instead of movement direction.
	// This is intended as a temporary testing hook and is easy to remove.
	private boolean useMouseFacing = false;

	// Optional: allow the player to reuse an enemy's sprite set (e.g. goblin) for visuals.
	private EnemySpriteSet playerSpriteSet;
	private DirectionalAnimationSet playerWalkAnimations;

	/**
	 * New constructor: character visuals are provided via {@link GameAssets} so we can swap frames.
	 */
	public Character(Array<Wall> wallArray, Array<Enemy> enemyArray, GameAssets assets, SwordType swordType, float width, float height, float MaxHealth) {
		super(assets.getPlayerIdle());
		this.assets = assets;
		setMaxHealth(MaxHealth);
		setRemainingHealth(getMaxHealth());
		setSize(width, height);
		this.wallArray = wallArray;
		this.enemyArray = enemyArray;
		setOriginCenter();
		updateCollider();

		KEY_FORWARD = ConfigManager.getInt(ConfigManager.ConfigKey.FORWARD_KEY, 0, 255);
		KEY_BACKWARD = ConfigManager.getInt(ConfigManager.ConfigKey.BACKWARD_KEY, 0, 255);
		KEY_LEFT = ConfigManager.getInt(ConfigManager.ConfigKey.LEFT_KEY, 0, 255);
		KEY_RIGHT = ConfigManager.getInt(ConfigManager.ConfigKey.RIGHT_KEY, 0, 255);
		BUTTON_ATTACK = ConfigManager.getInt(ConfigManager.ConfigKey.ATTACK_KEY, 0, 255);

		this.weapon = swordType.create(enemyArray, assets);
		attachWeapon();
	}

	public void setDeathListener(DeathListener deathListener) {
		this.deathListener = deathListener;
	}

	/**
	 * Attempts to set the player's visuals to use the sprite set of an enemy type.
	 * If the named enemy sprite set isn't available, this logs and leaves the default player
	 * visuals intact.
	 *
	 * @param enemyId folder name under textures/enemy/ (e.g. "goblin_01")
	 */
	public void setPlayerSpriteFromEnemy(String enemyId) {
		if (assets == null || enemyId == null) return;
		try {
			EnemySpriteSet set = assets.getEnemySpriteSet(enemyId);
			setPlayerSpriteSet(set);
		} catch (IllegalArgumentException e) {
			Gdx.app.log("Character", "Could not set player sprite from enemy '" + enemyId + "': " + e.getMessage());
		}
	}

	/**
	 * Directly assigns an {@link EnemySpriteSet} to the player and builds walk animations.
	 * Pass {@code null} to reset to the default player artwork.
	 */
	public void setPlayerSpriteSet(EnemySpriteSet set) {
		this.playerSpriteSet = set;
		if (set != null) {
			float playerWalkFrameDurationSeconds = 0.16f;
			this.playerWalkAnimations = set.createWalkAnimations(playerWalkFrameDurationSeconds);
		} else {
			this.playerWalkAnimations = null;
		}
		// Reset animation time so newly assigned sprites start consistently.
		this.stateTimeSeconds = 0f;
	}

	/**
	 * @return the equipped weapon instance (never {@code null})
	 */
	public Weapon getWeapon() {
		return weapon;
	}

	/**
	 * Applies damage to the player and pushes them away from the given enemy.
	 *
	 * <p>Damage is ignored during the invulnerability window. If health reaches zero, the application
	 * exits immediately.</p>
	 *
	 * @param enemy     damaging enemy
	 * @param damage    damage amount
	 * @param knockback knockback strength applied as velocity
	 */
	public void takeDamage(Enemy enemy, float damage, float knockback) {
		if (damageInvulnerabilityTime > 0f) return;

		setRemainingHealth(getRemainingHealth() - damage);
		damageInvulnerabilityTime = 1f;

		float dx = getCenterX() - enemy.getCenterX();
		float dy = getCenterY() - enemy.getCenterY();

		float len = (float) Math.sqrt(dx * dx + dy * dy);

		if (len != 0f) {
			dx /= len;
			dy /= len;
		}

		knockbackVx = dx * knockback;
		knockbackVy = dy * knockback;

		if (getRemainingHealth() <= 0) {
			die();
		}
	}

	/**
	 * Updates the player for a frame.
	 *
	 * <p>Handles movement, rotation, weapon attacks, and enemy contact damage.</p>
	 *
	 * @param delta  time since last frame in seconds
	 * @param camera camera used to unproject mouse screen coordinates into world coordinates
	 */
	public void update(float delta, OrthographicCamera camera) {
		stateTimeSeconds += delta;
		movement(delta);
		rotateToMouse(camera);
		// Toggle facing mode with key 'F' for quick testing. This is intentionally
		// added as a small, easy-to-remove testing hook.
		if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
			useMouseFacing = !useMouseFacing;
			Gdx.app.log("Character", "useMouseFacing = " + useMouseFacing);
		}
		updateVisual();

		if (Gdx.input.isButtonJustPressed(BUTTON_ATTACK)) {
			weapon.startAttack(currentAttackAngleDeg);
		}
		attachWeapon();

		weapon.update(delta);
		damageInvulnerabilityTime -= delta;
		for (Enemy enemy : enemyArray) {
			if (collider.overlaps(enemy.getBoundingRectangle())) {
				takeDamage(enemy, 10, 150);
				break;
			}
		}
	}

	private void updateVisual() {
		if (assets == null) return;

		// If the player has been assigned an enemy sprite set, prefer using it.
		if (playerSpriteSet != null) {
			if (walking && playerWalkAnimations != null) {
				TextureRegion frame = playerWalkAnimations.getKeyFrame(facing, stateTimeSeconds, true);
				setRegion(frame);
			} else setRegion(playerSpriteSet.getIdle(facing));
			return;
		}

		// Fallback: use the default player texture provided by GameAssets.
		TextureRegion idle = assets.getPlayerIdle();
		setRegion(idle);
	}

	private void rotateToMouse(OrthographicCamera camera) {
		camera.unproject(mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0));

		float characterX = getX() + getWidth() * 0.5f;
		float characterY = getY() + getHeight() * 0.5f;

		float dx = mouseWorld.x - characterX;
		float dy = mouseWorld.y - characterY;

		double degrees = Math.toDegrees(Math.atan2(dy, dx));
		currentAttackAngleDeg = (float) degrees + FRONT_ANGLE_OFFSET_DEG;

		// Determine a coarse 4-way facing from the raw angle (no offset). This is used
		// only when useMouseFacing is enabled so the player 'looks' toward the cursor.
		if (useMouseFacing) {
			float absDx = Math.abs(dx);
			float absDy = Math.abs(dy);

			// XD wtf is this
			// nested ternary operators are funny
			facing = absDx > absDy ? dx > 0 ? Direction.RIGHT : Direction.LEFT : dy > 0 ? Direction.BACK : Direction.FRONT;
		}
	}

	private void attachWeapon() {
		float characterX = getX() + getWidth() * 0.5f;
		float characterY = getY() + getHeight() * 0.5f;

		weaponOffsetWorld.set(weaponOffsetLocal).rotateDeg(currentAttackAngleDeg);

		weapon.setOrigin(-0.5f * weapon.getWidth(), -0.5f * weapon.getHeight());
		weapon.setOriginBasedPosition(
			characterX + weaponOffsetWorld.x,
			characterY + weaponOffsetWorld.y
		);

		if (!weapon.isAttacking()) weapon.setRotation(currentAttackAngleDeg + 45f);
	}

	private void movement(float delta) {
		delta = Math.min(delta, MAX_DELTA);

		if (Math.abs(knockbackVx) > 0f || Math.abs(knockbackVy) > 0f) {
			moveWithCollisions(knockbackVx * delta, knockbackVy * delta, false);

			float decay = (float) Math.exp(-KNOCKBACK_DAMPING_PER_SECOND * delta);
			knockbackVx *= decay;
			knockbackVy *= decay;

			if (Math.abs(knockbackVx) < KNOCKBACK_VELOCITY_EPS) knockbackVx = 0f;
			if (Math.abs(knockbackVy) < KNOCKBACK_VELOCITY_EPS) knockbackVy = 0f;
		}
		if (knockbackVx == 0f && knockbackVy == 0f) {
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

			float distance = (float) Math.sqrt(totalDx * totalDx + totalDy * totalDy);
			int steps = Math.max(1, (int) Math.ceil(distance / MAX_STEP_DISTANCE));
			float stepDx = totalDx / steps;
			float stepDy = totalDy / steps;

			for (int i = 0; i < steps; i++) moveWithCollisions(stepDx, stepDy, true);

			walking = (moveX != 0f || moveY != 0f);
			// Only update facing from movement if mouse-facing mode is disabled.
			if (!useMouseFacing && walking) {
				if (Math.abs(moveX) > Math.abs(moveY)) facing = moveX > 0 ? Direction.RIGHT : Direction.LEFT;
				else facing = moveY > 0 ? Direction.BACK : Direction.FRONT;
			}
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
	private void moveWithCollisions(float dx, float dy, boolean checkEnemies) {
		boolean ignoreEnemyCollision = Math.abs(knockbackVx) > 0f || Math.abs(knockbackVy) > 0f;

		if (dx != 0f) {
			float oldX = getX();
			setX(oldX + dx);
			updateCollider();

			if (overlapsAnyWall()) {
				setX(oldX);
				updateCollider();
			}
		}

		if (dy != 0f) {
			float oldY = getY();
			setY(oldY + dy);
			updateCollider();

			if (overlapsAnyWall()) {
				setY(oldY);
				updateCollider();
			} else if (checkEnemies && !ignoreEnemyCollision) {
				Enemy enemy = getOverlappingEnemy();
				if (enemy != null) {
					takeDamage(enemy, 10, 150);
				}
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
		// Wall collider is also an AABB rectangle.
		for (Wall wall : wallArray) if (collider.overlaps(wall.getBoundingRectangle())) return true;
		return false;
	}

	private Enemy getOverlappingEnemy() {
		for (Enemy enemy : enemyArray) if (collider.overlaps(enemy.getBoundingRectangle())) return enemy;
		return null;
	}

	private void die() {
		if (isDead) return;
		isDead = true;
		if (deathListener != null) deathListener.onDied(this);
	}

	/**
	 * @return maximum health
	 */
	public float getMaxHealth() {
		return MaxHealth;
	}

	/**
	 * @param maxHealth maximum health
	 */
	public void setMaxHealth(float maxHealth) {
		MaxHealth = maxHealth;
	}

	/**
	 * @return current remaining health
	 */
	public float getRemainingHealth() {
		return RemainingHealth;
	}

	/**
	 * @param remainingHealth new remaining health
	 */
	public void setRemainingHealth(float remainingHealth) {
		RemainingHealth = remainingHealth;
	}

	/**
	 * @return sprite center X coordinate
	 */
	public float getCenterX() {
		return getX() + getWidth() * 0.5f;
	}

	/**
	 * @return sprite center Y coordinate
	 */
	public float getCenterY() {
		return getY() + getHeight() * 0.5f;
	}

	public boolean isDead() {
		return isDead;
	}

	@FunctionalInterface
	public interface DeathListener {
		void onDied(Character character);
	}
}
