package org.lasarimanstudios.escapedungeon.enemies;

import com.badlogic.gdx.math.MathUtils;

public class Goblin extends Enemy {

	private static final float BASE_HEALTH = 10;
	private static final float BASE_ATTACK_DAMAGE = 10f;
	private static final float BASE_SPEED = 10f;

	private static final float KNOCKBACK_DAMPING_PER_SECOND = 18f;
	private static final float KNOCKBACK_VELOCITY_EPS = 0.05f;
	private float damageInvulnerabilityTime = 0.3f;

	private float knockbackVx = 0f;
	private float knockbackVy = 0f;

	public Goblin(String texture, float width, float height, float posX, float posY, int level) {
		super("goblin-01/" + texture, width, height, posX, posY);
		setLevel(level);
		setMaxHealth((float) (BASE_HEALTH * Math.pow(1.2, level)));
		setRemainingHealth(getMaxHealth());
		setAttackDamage((float) (BASE_ATTACK_DAMAGE * Math.pow(1.2, level)));
		setSpeed(BASE_SPEED);
	}

	@Override
	public void takeDamage(float damage, float knockback, float hitAngle) {
		if (damageInvulnerabilityTime > 0f) return;
		setRemainingHealth(getRemainingHealth() - damage);
		damageInvulnerabilityTime = 0.3f;

		float dx = (float) Math.cos(hitAngle);
		float dy = (float) Math.sin(hitAngle);

		knockbackVx += dx * knockback;
		knockbackVy += dy * knockback;

		if (getRemainingHealth() <= 0f) die();
	}

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

	@Override
	public void die() {
		getLevelScreen().addBloodPuddle(getX(), getY());
		getLevelScreen().getMap().getEnemies().removeValue(this, true);
		if (MathUtils.random(2) == 0) {
			getLevelScreen().addChest(getX(), getY());
		}
	}


	public void following(float delta) {

		float diffX = getCharacter().getX() - getX();
		float diffY = getCharacter().getY() - getY();

		float length = (float) Math.sqrt(diffX * diffX + diffY * diffY);

		if (length > 0) {
			float dirX = diffX / length;
			float dirY = diffY / length;

			setX(getX() + dirX * getSpeed() * delta);
			setY(getY() + dirY * getSpeed() * delta);
		}
	}

	public float getDamageInvulnerabilityTime() {
		return damageInvulnerabilityTime;
	}

	public void setDamageInvulnerabilityTime(float damageInvulnerabilityTime) {
		this.damageInvulnerabilityTime = damageInvulnerabilityTime;
	}
}
