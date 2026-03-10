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
import org.lasarimanstudios.escapedungeon.assets.AssetManager;
import org.lasarimanstudios.escapedungeon.assets.CharacterSpriteSet;
import org.lasarimanstudios.escapedungeon.assets.Direction;
import org.lasarimanstudios.escapedungeon.assets.DirectionalAnimationSet;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.roguelike.PlayerStats;
import org.lasarimanstudios.escapedungeon.weapons.SwordType;
import org.lasarimanstudios.escapedungeon.weapons.Weapon;
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
	private static final float SPEED = 22f;
	private static final float DIAGONAL_MULTIPLIER = 2f / 3f;
	private static final float MAX_STEP_DISTANCE = 0.25f;
	private static final float MAX_DELTA = 1f / 30f;
	private static int KEY_FORWARD;
	private static int KEY_BACKWARD;
	private static int KEY_LEFT;
	private static int KEY_RIGHT;
	private static int BUTTON_ATTACK;
	private final Vector3 mouseWorld = new Vector3();
	private final Array<Wall> wallArray;
	private final Array<Enemy> enemyArray;
	private final Rectangle collider = new Rectangle();
	private final Weapon weapon;
	private final Vector2 weaponOffsetLocal = new Vector2(0f, 0f);
	private final Vector2 weaponOffsetWorld = new Vector2();
	private final AssetManager assets;
	private float maxHealth;
	private float remainingHealth;
	private float knockbackVx = 0f;
	private float knockbackVy = 0f;
	private float damageInvulnerabilityTime = 1f;
	private boolean isDead;
	private DeathListener deathListener;
	private Direction facing = Direction.FRONT;
	private float stateTimeSeconds = 0f;
	private boolean walking = false;
	private float currentAttackAngleDeg = 0f;

	private CharacterSpriteSet playerSpriteSet;
	private DirectionalAnimationSet playerWalkAnimations;
	private final PlayerStats playerStats;

	// ── dash state ──
	private static final float DASH_DURATION = 0.15f;
	private static final float DASH_SPEED_MULTIPLIER = 5f;
	private static final float DASH_COOLDOWN = 1.0f;
	private float dashTimeRemaining = 0f;
	private float dashCooldownRemaining = 0f;
	private float dashDirX = 0f;
	private float dashDirY = 0f;

	/**
	 * Creates a player character with visuals provided via {@link AssetManager} and per-run stats.
	 *
	 * @param wallArray   walls used for collision detection
	 * @param enemyArray  enemies used for contact-damage checks
	 * @param assets      game asset registry providing character sprites
	 * @param swordType   the type of sword the character starts with
	 * @param maxHealth   initial maximum health
	 * @param playerStats per-run stat modifiers from roguelike perks
	 */
	public Character(Array<Wall> wallArray, Array<Enemy> enemyArray, AssetManager assets, SwordType swordType, float maxHealth, PlayerStats playerStats) {
		super(assets.getCharacterSpriteSet("character_01").getIdle(Direction.FRONT));
		this.assets = assets;
		this.playerStats = playerStats;
		setMaxHealth(maxHealth);
		setRemainingHealth(getMaxHealth());
		setSize(4.24f, 6f);
		this.wallArray = wallArray;
		this.enemyArray = enemyArray;
		setOriginCenter();
		updateCollider();

		KEY_FORWARD = ConfigManager.getInt(ConfigManager.ConfigKey.FORWARD_KEY, 0, 255);
		KEY_BACKWARD = ConfigManager.getInt(ConfigManager.ConfigKey.BACKWARD_KEY, 0, 255);
		KEY_LEFT = ConfigManager.getInt(ConfigManager.ConfigKey.LEFT_KEY, 0, 255);
		KEY_RIGHT = ConfigManager.getInt(ConfigManager.ConfigKey.RIGHT_KEY, 0, 255);
		BUTTON_ATTACK = ConfigManager.getInt(ConfigManager.ConfigKey.ATTACK_KEY, 0, 255);

		this.weapon = swordType.create(enemyArray, wallArray, assets);
		this.weapon.setPlayerStats(playerStats);
		attachWeapon();
	}

	/**
	 * Registers a listener that is notified when the character dies.
	 *
	 * @param deathListener the listener to register
	 */
	public void setDeathListener(DeathListener deathListener) {
		this.deathListener = deathListener;
	}


	/**
	 * Attempts to set the player's visuals to use the sprite set of a character type.
	 * If the named sprite set isn't available, this logs a warning and leaves the default
	 * visuals intact.
	 *
	 * @param characterId folder name under {@code textures/character/} (e.g. {@code "character_01"})
	 */
	public void setPlayerSprite(String characterId) {
		if (assets == null || characterId == null) return;
		try {
			CharacterSpriteSet set = assets.getCharacterSpriteSet(characterId);
			setPlayerSpriteSet(set);
		} catch (IllegalArgumentException e) {
			Gdx.app.log("Character", "Could not set player sprite from character '" + characterId + "': " + e.getMessage());
		}
	}

	/**
	 * Directly assigns a {@link CharacterSpriteSet} to the player and builds walk animations.
	 * Pass {@code null} to reset to the default player artwork.
	 *
	 * @param set the sprite set to use, or {@code null} to reset
	 */
	public void setPlayerSpriteSet(CharacterSpriteSet set) {
		this.playerSpriteSet = set;
		if (set != null) {
			float playerWalkFrameDurationSeconds = 0.16f;
			this.playerWalkAnimations = set.createWalkAnimations(playerWalkFrameDurationSeconds);
		} else {
			this.playerWalkAnimations = null;
		}
		this.stateTimeSeconds = 0f;
	}

	/**
	 * Returns the equipped weapon instance.
	 *
	 * @return the equipped weapon (never {@code null})
	 */
	public Weapon getWeapon() {
		return weapon;
	}

	/**
	 * Applies damage to the player and pushes them away from the given enemy.
	 *
	 * <p>Damage is ignored during the invulnerability window. If health reaches zero the
	 * character dies.</p>
	 *
	 * @param enemy     the damaging enemy
	 * @param damage    damage amount
	 * @param knockback knockback strength applied as velocity
	 */
	public void takeDamage(Enemy enemy, float damage, float knockback) {
		if (damageInvulnerabilityTime > 0f) return;
		if (dashTimeRemaining > 0f) return; // invulnerable during dash

		float effectiveDamage = playerStats.applyDefense(damage);
		setRemainingHealth(getRemainingHealth() - effectiveDamage);
		damageInvulnerabilityTime = 1f;

		// Thorns: reflect a portion of the received damage back to the attacker
		if (playerStats.hasThorns()) {
			float reflected = effectiveDamage * playerStats.getThornsPercent();
			enemy.takeDamage(reflected, 0f, 0f, -1);
		}

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
	 * Updates the player for a single frame.
	 *
	 * <p>Handles movement, rotation, weapon attacks, and enemy contact damage.</p>
	 *
	 * @param delta  time since last frame in seconds
	 * @param camera camera used to unproject mouse screen coordinates into world coordinates
	 */
	public void update(float delta, OrthographicCamera camera) {
		stateTimeSeconds += delta;

		// ── dash logic ──
		dashCooldownRemaining -= delta;
		if (dashTimeRemaining > 0f) {
			dashTimeRemaining -= delta;
			float dashSpeed = SPEED * DASH_SPEED_MULTIPLIER;
			moveWithCollisions(dashDirX * dashSpeed * delta, dashDirY * dashSpeed * delta, false);
		} else {
			movement(delta);
		}

		// Trigger dash on SHIFT press
		if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && dashCooldownRemaining <= 0f) {
			startDash();
		}

		rotateToMouse(camera);

		updateVisual();

		if (Gdx.input.isButtonJustPressed(BUTTON_ATTACK)) {
			weapon.startAttack(currentAttackAngleDeg);
		}
		attachWeapon();

		// Remember enemy health before weapon update for lifesteal
		float totalEnemyHealthBefore = 0f;
		if (playerStats.hasLifesteal()) {
			for (Enemy enemy : enemyArray) {
				totalEnemyHealthBefore += enemy.getRemainingHealth();
			}
		}

		weapon.update(delta);

		// Lifesteal: heal based on damage dealt this frame
		if (playerStats.hasLifesteal()) {
			float totalEnemyHealthAfter = 0f;
			for (Enemy enemy : enemyArray) {
				totalEnemyHealthAfter += enemy.getRemainingHealth();
			}
			float damageDealt = totalEnemyHealthBefore - totalEnemyHealthAfter;
			if (damageDealt > 0f) {
				float heal = damageDealt * playerStats.getLifestealPercent();
				setRemainingHealth(Math.min(getRemainingHealth() + heal, getMaxHealth()));
			}
		}

		damageInvulnerabilityTime -= delta;
		if (dashTimeRemaining <= 0f) {
			for (Enemy enemy : enemyArray) {
				if (collider.overlaps(enemy.getBoundingRectangle())) {
					takeDamage(enemy, enemy.getAttackDamage(), 150);
					break;
				}
			}
		}
	}

	/**
	 * Initiates a dash in the current movement direction (or facing direction if stationary).
	 */
	private void startDash() {
		float moveX = 0f;
		float moveY = 0f;
		if (Gdx.input.isKeyPressed(KEY_FORWARD)) moveY += 1f;
		if (Gdx.input.isKeyPressed(KEY_BACKWARD)) moveY -= 1f;
		if (Gdx.input.isKeyPressed(KEY_RIGHT)) moveX += 1f;
		if (Gdx.input.isKeyPressed(KEY_LEFT)) moveX -= 1f;

		// If no direction keys are pressed, dash in the facing direction
		if (moveX == 0f && moveY == 0f) {
			switch (facing) {
				case FRONT -> moveY = -1f;
				case BACK -> moveY = 1f;
				case LEFT -> moveX = -1f;
				case RIGHT -> moveX = 1f;
			}
		}

		float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
		if (len != 0f) {
			dashDirX = moveX / len;
			dashDirY = moveY / len;
		}
		dashTimeRemaining = DASH_DURATION;
		dashCooldownRemaining = DASH_COOLDOWN;
	}

	/**
	 * Updates the sprite region based on the current facing direction and walking state.
	 */
	private void updateVisual() {
		if (assets == null) return;

		if (playerSpriteSet != null) {
			if (walking && playerWalkAnimations != null) {
				TextureRegion frame = playerWalkAnimations.getKeyFrame(facing, stateTimeSeconds, true);
				setRegion(frame);
			} else {
				setRegion(playerSpriteSet.getIdle(facing));
			}
			return;
		}

		TextureRegion idle = assets.getCharacterSpriteSet("character_01").getIdle(facing);
		setRegion(idle);
	}

	/**
	 * Calculates the angle from the character's center to the mouse cursor in world space
	 * and updates the attack angle and (optionally) the facing direction.
	 *
	 * @param camera the game camera for unprojecting screen coordinates
	 */
	private void rotateToMouse(OrthographicCamera camera) {
		camera.unproject(mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0));

		float characterX = getX() + getWidth() * 0.5f;
		float characterY = getY() + getHeight() * 0.5f;

		float dx = mouseWorld.x - characterX;
		float dy = mouseWorld.y - characterY;

		double degrees = Math.toDegrees(Math.atan2(dy, dx));
		currentAttackAngleDeg = (float) degrees + FRONT_ANGLE_OFFSET_DEG;
	}

	/**
	 * Positions and rotates the weapon relative to the character's center.
	 */
	private void attachWeapon() {
		float characterX = getX() + getWidth() * 0.5f;
		float characterY = getY() + getHeight() * 0.5f;

		weaponOffsetWorld.set(weaponOffsetLocal).rotateDeg(currentAttackAngleDeg);

		weapon.setOriginBasedPosition(
			characterX + weaponOffsetWorld.x,
			characterY + weaponOffsetWorld.y
		);

		if (!weapon.isAttacking()) weapon.setRotation(currentAttackAngleDeg + 45f);
	}

	/**
	 * Handles keyboard-driven movement and knockback decay for a single frame.
	 *
	 * @param delta time since last frame in seconds
	 */
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

			float totalDx = moveX * playerStats.applySpeed(SPEED) * delta;
			float totalDy = moveY * playerStats.applySpeed(SPEED) * delta;

			float distance = (float) Math.sqrt(totalDx * totalDx + totalDy * totalDy);
			int steps = Math.max(1, (int) Math.ceil(distance / MAX_STEP_DISTANCE));
			float stepDx = totalDx / steps;
			float stepDy = totalDy / steps;

			for (int i = 0; i < steps; i++) moveWithCollisions(stepDx, stepDy, true);

			walking = (moveX != 0f || moveY != 0f);
			if (walking) {
				if (Math.abs(moveX) > Math.abs(moveY)) facing = moveX > 0 ? Direction.RIGHT : Direction.LEFT;
				else facing = moveY > 0 ? Direction.BACK : Direction.FRONT;
			}
		}
	}

	/**
	 * Attempts to move the character by the given delta while resolving collisions against walls.
	 *
	 * <p>Resolution is done per-axis (X then Y). If a move causes overlap with any wall, that
	 * axis movement is reverted. Optionally checks enemy overlap on the Y-axis step.</p>
	 *
	 * @param dx           movement delta on X axis (world units)
	 * @param dy           movement delta on Y axis (world units)
	 * @param checkEnemies whether to test for enemy overlap after the Y-axis step
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
	 */
	private void updateCollider() {
		collider.set(getX(), getY(), getWidth(), getHeight());
	}

	/**
	 * Checks whether the character collider overlaps any wall collider.
	 *
	 * @return {@code true} if overlapping at least one wall
	 */
	private boolean overlapsAnyWall() {
		for (Wall wall : wallArray) if (collider.overlaps(wall.getBoundingRectangle())) return true;
		return false;
	}

	/**
	 * Returns the first enemy whose bounding rectangle overlaps the character collider.
	 *
	 * @return the overlapping enemy, or {@code null} if none
	 */
	private Enemy getOverlappingEnemy() {
		for (Enemy enemy : enemyArray) if (collider.overlaps(enemy.getBoundingRectangle())) return enemy;
		return null;
	}

	/**
	 * Marks the character as dead and notifies the registered {@link DeathListener}.
	 */
	private void die() {
		if (isDead) return;
		isDead = true;
		if (deathListener != null) deathListener.onDied(this);
	}

	/**
	 * Returns the maximum health.
	 *
	 * @return maximum health
	 */
	public float getMaxHealth() {
		return maxHealth;
	}

	/**
	 * Sets the maximum health.
	 *
	 * @param maxHealth maximum health value
	 */
	public void setMaxHealth(float maxHealth) {
		this.maxHealth = maxHealth;
	}

	/**
	 * Returns the current remaining health.
	 *
	 * @return remaining health
	 */
	public float getRemainingHealth() {
		return remainingHealth;
	}

	/**
	 * Sets the current remaining health.
	 *
	 * @param remainingHealth new remaining health value
	 */
	public void setRemainingHealth(float remainingHealth) {
		this.remainingHealth = remainingHealth;
	}

	/**
	 * Returns the X coordinate of the sprite's center.
	 *
	 * @return center X in world units
	 */
	public float getCenterX() {
		return getX() + getWidth() * 0.5f;
	}

	/**
	 * Returns the Y coordinate of the sprite's center.
	 *
	 * @return center Y in world units
	 */
	public float getCenterY() {
		return getY() + getHeight() * 0.5f;
	}

	/**
	 * Returns whether the character is dead.
	 *
	 * @return {@code true} if the character has died
	 */
	public boolean isDead() {
		return isDead;
	}

	/**
	 * Listener notified when the player character dies.
	 */
	@FunctionalInterface
	public interface DeathListener {
		/**
		 * Called when the character dies.
		 *
		 * @param character the character that died
		 */
		void onDied(Character character);
	}
}
