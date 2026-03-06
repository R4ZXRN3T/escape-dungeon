package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.Texture;

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
	private float damageInvulnerabilityTime = 0.3f;

	private float knockbackVx = 0f;
	private float knockbackVy = 0f;

	/**
	 * Creates a goblin at the given position.
	 *
	 * @param texture sprite texture (must already be loaded)
	 * @param width   sprite width in world units
	 * @param height  sprite height in world units
	 * @param posX    initial x position in world units
	 * @param posY    initial y position in world units
	 * @param level   difficulty level used for stat scaling
	 */
	public Goblin(Texture texture, float width, float height, float posX, float posY, int level) {
		super(texture, width, height, posX, posY);
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
		damageInvulnerabilityTime -= delta;
		if (Math.abs(knockbackVx) > 0f || Math.abs(knockbackVy) > 0f) {
			setX(getX() + knockbackVx * delta);
			setY(getY() + knockbackVy * delta);

			float decay = (float) Math.exp(-KNOCKBACK_DAMPING_PER_SECOND * delta);
			knockbackVx *= decay;
			knockbackVy *= decay;

			if (Math.abs(knockbackVx) < KNOCKBACK_VELOCITY_EPS) knockbackVx = 0f;
			if (Math.abs(knockbackVy) < KNOCKBACK_VELOCITY_EPS) knockbackVy = 0f;
		} else {
			following(delta);
		}
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

		float diffX = getCharacter().getX() - getX();
		float diffY = getCharacter().getY() - getY();

		float length = (float) Math.sqrt(diffX * diffX + diffY * diffY);

		if (length > 0 && length < 35) {
			float dirX = diffX / length;
			float dirY = diffY / length;

			setX(getX() + dirX * getSpeed() * delta);
			setY(getY() + dirY * getSpeed() * delta);
		}
	}

	/**
	 * @return remaining invulnerability time in seconds
	 */
	public float getDamageInvulnerabilityTime() {
		return damageInvulnerabilityTime;
	}

	/**
	 * @param damageInvulnerabilityTime remaining invulnerability time in seconds
	 */
	public void setDamageInvulnerabilityTime(float damageInvulnerabilityTime) {
		this.damageInvulnerabilityTime = damageInvulnerabilityTime;
	}
}
