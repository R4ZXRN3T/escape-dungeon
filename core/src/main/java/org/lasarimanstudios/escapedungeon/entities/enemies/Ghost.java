package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.lasarimanstudios.escapedungeon.assets.AssetManager;
import org.lasarimanstudios.escapedungeon.assets.Direction;
import org.lasarimanstudios.escapedungeon.assets.DirectionalAnimationSet;
import org.lasarimanstudios.escapedungeon.assets.EnemySpriteSet;

/**
 * Ghost enemy that follows the player character.
 *
 * <p>Uses the same follow/knockback logic as {@link Goblin} but with its own sprite set
 * (auto-discovered from {@code textures/enemy/ghost/}).</p>
 */
public class Ghost extends Enemy {

	private static final float BASE_HEALTH = 20f;
	private static final float BASE_ATTACK_DAMAGE = 8f;
	private static final float BASE_SPEED = 12f;

	private static final float KNOCKBACK_DAMPING_PER_SECOND = 18f;
	private static final float KNOCKBACK_VELOCITY_EPS = 0.05f;

	private static final float WIDTH = 3.23f;
	private static final float HEIGHT = 5f;

	private final EnemySpriteSet spriteSet;
	private final DirectionalAnimationSet walkAnimations;
	private float damageInvulnerabilityTime = 0.3f;
	private float knockbackVx = 0f;
	private float knockbackVy = 0f;
	private Direction facing = Direction.FRONT;
	private float stateTimeSeconds = 0f;
	private boolean walking = false;

	/**
	 * Creates a ghost enemy at the specified world position.
	 *
	 * @param assets assets container providing enemy sprites
	 * @param posX   initial X position in world units
	 * @param posY   initial Y position in world units
	 * @param level  enemy level for stat scaling
	 */
	public Ghost(AssetManager assets, float posX, float posY, int level) {
		super(assets.getEnemySpriteSet("ghost").getFrontIdleTexture(), WIDTH, HEIGHT, posX, posY);
		this.spriteSet = assets.getEnemySpriteSet("ghost");
		this.walkAnimations = spriteSet.createWalkAnimations(0.18f);
		applyVisualRegion(spriteSet.getIdle(Direction.FRONT));
		setLevel(level);
		setMaxHealth((float) (BASE_HEALTH * Math.pow(1.2, level)));
		setRemainingHealth(getMaxHealth());
		setAttackDamage((float) (BASE_ATTACK_DAMAGE * Math.pow(1.2, level)));
		setSpeed(BASE_SPEED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void takeDamage(float damage, float knockback, float hitAngle, int attackInstanceId) {
		if (!shouldAcceptDamageFromAttack(attackInstanceId)) return;
		if (damageInvulnerabilityTime > 0f) return;

		markDamagedByAttack(attackInstanceId);
		setRemainingHealth(getRemainingHealth() - damage);
		damageInvulnerabilityTime = 0.3f;

		float dx = (float) Math.cos(hitAngle);
		float dy = (float) Math.sin(hitAngle);
		knockbackVx += dx * knockback;
		knockbackVy += dy * knockback;

		if (getRemainingHealth() <= 0f) die();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(float delta) {
		stateTimeSeconds += delta;
		damageInvulnerabilityTime -= delta;

		if (Math.abs(knockbackVx) > 0f || Math.abs(knockbackVy) > 0f) {
			setX(getX() + knockbackVx * delta);
			setY(getY() + knockbackVy * delta);

			float decay = (float) Math.exp(-KNOCKBACK_DAMPING_PER_SECOND * delta);
			knockbackVx *= decay;
			knockbackVy *= decay;

			if (Math.abs(knockbackVx) < KNOCKBACK_VELOCITY_EPS) knockbackVx = 0f;
			if (Math.abs(knockbackVy) < KNOCKBACK_VELOCITY_EPS) knockbackVy = 0f;
			walking = false;
		} else {
			following(delta);
		}

		updateVisual();
	}

	/**
	 * Updates the sprite region based on the current facing direction and walking state.
	 */
	private void updateVisual() {
		if (spriteSet == null) return;
		TextureRegion region;
		if (walking) {
			region = walkAnimations.getKeyFrame(facing, stateTimeSeconds, true);
		} else {
			region = spriteSet.getIdle(facing);
		}
		applyVisualRegion(region);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void die() {
		notifyDied();
	}

	/**
	 * Moves toward the configured player character when within aggro range.
	 *
	 * @param delta time since last frame in seconds
	 */
	public void following(float delta) {
		float oldX = getX();
		float oldY = getY();

		float diffX = getCharacter().getX() - getX();
		float diffY = getCharacter().getY() - getY();
		float length = (float) Math.sqrt(diffX * diffX + diffY * diffY);

		walking = false;
		if (length > 0 && length < 35) {
			float dirX = diffX / length;
			float dirY = diffY / length;

			setX(getX() + dirX * getSpeed() * delta);
			setY(getY() + dirY * getSpeed() * delta);
			walking = true;
			updateFacing(getX() - oldX, getY() - oldY);
		}
	}

	/**
	 * Updates the facing direction based on the movement delta.
	 *
	 * @param dx horizontal movement since last frame
	 * @param dy vertical movement since last frame
	 */
	private void updateFacing(float dx, float dy) {
		if (dx == 0f && dy == 0f) return;
		if (Math.abs(dx) > Math.abs(dy)) {
			facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
		} else {
			facing = dy > 0 ? Direction.BACK : Direction.FRONT;
		}
	}
}

