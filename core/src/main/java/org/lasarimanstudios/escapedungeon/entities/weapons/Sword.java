package org.lasarimanstudios.escapedungeon.entities.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;

/**
 * Sword weapon that swings in an arc.
 *
 * <p>During an attack, the sword interpolates its rotation from a start angle to an end angle over
 * {@link #getAttackSpeed()} seconds and damages enemies whose bounding rectangles overlap the sword's
 * bounding rectangle.</p>
 */
public class Sword extends Weapon {

	private static final float ARC_DEG = 180f;
	private final Array<Enemy> enemies;
	private float startAngle;
	private float endAngle;
	private float elapsedTime;

	/**
	 * Creates a sword.
	 *
	 * @param enemies      enemies that can be hit (iterated every frame while attacking)
	 * @param texture      sword texture (must already be loaded)
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 * @param range        effective range (currently unused)
	 */
	public Sword(Array<Enemy> enemies, Texture texture, float attackDamage, float attackSpeed, float range) {
		super(texture, attackDamage, attackSpeed, range);
		setSize(4f, 4f);
		setOrigin(0.5f, getHeight() / 2f);
		this.enemies = enemies;
	}

	/**
	 * Updates the swing animation and applies damage to overlapping enemies.
	 *
	 * @param delta time since last frame in seconds
	 */
	@Override
	public void update(float delta) {
		if (!isAttacking()) return;

		elapsedTime += delta;

		float t = MathUtils.clamp(elapsedTime / getAttackSpeed(), 0f, 1f);
		float angle = MathUtils.lerp(startAngle, endAngle, MathUtils.sin(t * MathUtils.PI));
		setRotation(angle);

		if (t >= 1f) {
			setAttacking(false);
		}

		for (Enemy enemy : enemies) {
			if (enemy.getBoundingRectangle().overlaps(getBoundingRectangle())) {
				enemy.takeDamage(getAttackDamage(), 0f, angle, getAttackInstanceId());
			}
		}
	}

	/**
	 * Starts a swing around the given facing angle.
	 *
	 * @param facingAngle player facing angle in degrees
	 */
	@Override
	public void startAttack(float facingAngle) {
		if (isAttacking()) return;

		beginAttackInstance();

		float halfArc = ARC_DEG * 0.5f;
		this.startAngle = facingAngle + halfArc + 45;
		this.endAngle = facingAngle - halfArc + 45;

		setAttacking(true);
		this.elapsedTime = 0f;

		setRotation(startAngle);
	}

}
