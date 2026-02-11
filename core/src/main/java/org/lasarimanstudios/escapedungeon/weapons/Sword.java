package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;

public class Sword extends Weapon {

	private static final float ARC_DEG = 110f;     // Total swing arc.
	private static final float WINDUP_DEG = 55f;   // Small pre-swing offset.
	private static final float LUNGE_UNITS = 0.6f; // Small forward push.

	// If the sword texture "points" diagonally up-right at rotation 0,
	// compensate so that facingAngle lines up with the blade's forward direction.
	// Tweak this value until the swing matches the player's facing.
	private static final float SPRITE_FORWARD_OFFSET_DEG = -45f;

	private float startAngle;
	private float endAngle;
	private float lungeAngle;

	public boolean attacking = false;
	private float elapsedTime;

	private float baseX;
	private float baseY;

	public Sword(String texture, float attackDamage, float attackSpeed, float range) {
		super(texture, attackDamage, attackSpeed, range);
		setSize(3f, 3f);
		setOrigin(getWidth() * 0.15f, getHeight() * 0.15f);
	}


	@Override
	public void attack() {
		if (!attacking) return;

		float dt = Gdx.graphics.getDeltaTime();
		elapsedTime += dt;

		float t = elapsedTime / getAttackSpeed();
		if (t >= 1f) {
			t = 1f;
			attacking = false;
		}

		float eased = Interpolation.sine.apply(t);

		float currentAngle = startAngle + (endAngle - startAngle) * eased;
		setRotation(currentAngle);

		float lungeT = (t <= 0.5f) ? (t / 0.5f) : (1f - (t - 0.5f) / 0.5f);
		float lunge = Interpolation.pow2Out.apply(lungeT) * LUNGE_UNITS;

		float rad = (float) Math.toRadians(lungeAngle);
		setPosition(baseX + (float) Math.cos(rad) * lunge, baseY + (float) Math.sin(rad) * lunge);

		if (!attacking) {
			setPosition(baseX, baseY);
			elapsedTime = 0f;
		}
	}

	@Override
	public void startAttack(float facingAngle) {
		if (attacking) return;

		baseX = getX();
		baseY = getY();

		float visualFacing = facingAngle + SPRITE_FORWARD_OFFSET_DEG;

		float halfArc = ARC_DEG * 0.5f;
		this.startAngle = visualFacing - halfArc - WINDUP_DEG;
		this.endAngle = visualFacing + halfArc;

		// Lunge should be along the facing direction, not the swing angle.
		this.lungeAngle = visualFacing;

		this.attacking = true;
		this.elapsedTime = 0f;

		setRotation(startAngle);
	}
}
