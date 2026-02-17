// core/src/main/java/org/lasarimanstudios/escapedungeon/weapons/Sword.java
package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import org.lasarimanstudios.escapedungeon.enemies.Enemy;

public class Sword extends Weapon {

	private static final float ARC_DEG = 180f;

	private float startAngle;
	private float endAngle;

	public boolean attacking = false;
	private float elapsedTime;

	private final Array<Enemy> enemies;

	public Sword(Array<Enemy> enemies, String texture, float attackDamage, float attackSpeed, float range) {
		super(texture, attackDamage, attackSpeed, range);
		setSize(4f, 4f);
		setOrigin(0, 0);
		this.enemies = enemies;
	}

	@Override
	public void update() {
		if (!attacking) return;

		float dt = Gdx.graphics.getDeltaTime();
		elapsedTime += dt;

		float t = MathUtils.clamp(elapsedTime / getAttackSpeed(), 0f, 1f);
		float angle = MathUtils.lerp(startAngle, endAngle, t);
		setRotation(angle);

		if (t >= 1f) {
			attacking = false;
		}

		for (Enemy enemy : enemies)
			if (enemy.getBoundingRectangle().overlaps(getBoundingRectangle()))
				enemy.takeDamage(getAttackDamage(), 0f, angle);
	}

	@Override
	public void startAttack(float facingAngle) {
		if (attacking) return;

		float halfArc = ARC_DEG * 0.5f;
		this.startAngle = facingAngle + halfArc;
		this.endAngle = facingAngle - halfArc;

		this.attacking = true;
		this.elapsedTime = 0f;

		setRotation(startAngle);
	}
}
