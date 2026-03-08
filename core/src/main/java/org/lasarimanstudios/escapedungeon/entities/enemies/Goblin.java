package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.lasarimanstudios.escapedungeon.assets.Direction;
import org.lasarimanstudios.escapedungeon.assets.DirectionalAnimationSet;
import org.lasarimanstudios.escapedungeon.assets.EnemySpriteSet;
import org.lasarimanstudios.escapedungeon.assets.GameAssets;


/**
 * Basic enemy that follows the player character.
 *
 * <p>Stats are scaled by level (using {@code 1.2^level}). When hit, the goblin becomes briefly
 * invulnerable and receives knockback velocity that decays over time.</p>
 */
public class Goblin extends Enemy {

	private static final float BASE_HEALTH = 30f;
	private static final float BASE_ATTACK_DAMAGE = 10f;
	private static final float BASE_SPEED = 10f;

	private static final float KNOCKBACK_DAMPING_PER_SECOND = 18f;
	private static final float KNOCKBACK_VELOCITY_EPS = 0.05f;
	private final EnemySpriteSet spriteSet;
	private final DirectionalAnimationSet walkAnimations;
	private float damageInvulnerabilityTime = 0.3f;
	private float knockbackVx = 0f;
	private float knockbackVy = 0f;
	private Direction facing = Direction.FRONT;
	private float stateTimeSeconds = 0f;
	private boolean walking = false;

	/**
	 * Constructor: uses {@link GameAssets} to auto-discover goblin sprites via {@link EnemySpriteSet}.
	 */
	public Goblin(GameAssets assets, float posX, float posY, int level) {
		super(assets.getEnemySpriteSet("goblin_01").getFrontIdleTexture(), 3.23f, 5f, posX, posY);
		this.spriteSet = assets.getEnemySpriteSet("goblin_01");
		this.walkAnimations = spriteSet.createWalkAnimations(0.18f);
		applyVisualRegion(spriteSet.getIdle(Direction.FRONT));
		setLevel(level);
		setMaxHealth((float) (BASE_HEALTH * Math.pow(1.2, level)));
		setRemainingHealth(getMaxHealth());
		setAttackDamage((float) (BASE_ATTACK_DAMAGE * Math.pow(1.2, level)));
		setSpeed(BASE_SPEED);
	}

	/**
	 * Applies damage and knockback if not invulnerable.
	 *
	 * @param damage           damage amount
	 * @param knockback        knockback strength
	 * @param hitAngle         hit direction in radians
	 * @param attackInstanceId id of the current weapon attack; used to avoid multi-hits per swing
	 */
	@Override
	public void takeDamage(float damage, float knockback, float hitAngle, int attackInstanceId) {
		// Ensure "only once per attack" even if overlap is detected across multiple frames.
		if (!shouldAcceptDamageFromAttack(attackInstanceId)) return;

		// Optional additional i-frames across separate attacks.
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
	 * Updates invulnerability/knockback and performs follow movement.
	 *
	 * @param delta time since last frame in seconds
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
	 * Notifies the death listener.
	 */
	@Override
	public void die() {
		notifyDied();
	}

	/**
	 * Moves toward the configured {@link #getCharacter()}.
	 *
	 * <p>This requires that {@link #setCharacter(org.lasarimanstudios.escapedungeon.entities.Character)}
	 * was called; otherwise {@link #getCharacter()} may be {@code null}.</p>
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

	private void updateFacing(float dx, float dy) {
		if (dx == 0f && dy == 0f) return;
		if (Math.abs(dx) > Math.abs(dy)) {
			facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
		} else {
			facing = dy > 0 ? Direction.BACK : Direction.FRONT;
		}
	}
}
