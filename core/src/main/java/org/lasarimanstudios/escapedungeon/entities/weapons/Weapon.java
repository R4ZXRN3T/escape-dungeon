package org.lasarimanstudios.escapedungeon.entities.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Base class for weapons.
 *
 * <p>A weapon is a {@link com.badlogic.gdx.graphics.g2d.Sprite} that can enter an "attacking" state.
 * Concrete subclasses implement {@link #startAttack(float)} and {@link #update(float)} to animate the
 * weapon and perform hit detection.</p>
 */
public abstract class Weapon extends Sprite {
	private boolean attacking;
	private float attackDamage;
	private float attackSpeed;
	private float range;

	/**
	 * Creates a weapon sprite.
	 *
	 * @param texture      weapon texture (must already be loaded)
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 * @param range        effective range in world units (interpretation depends on weapon)
	 */
	public Weapon(Texture texture, float attackDamage, float attackSpeed, float range) {
		super(texture);
		this.attackDamage = attackDamage;
		this.attackSpeed = attackSpeed;
		this.range = range;
	}

	/** @return whether the weapon is currently attacking */
	public boolean isAttacking() {
		return attacking;
	}

	/**
	 * Sets the current attacking state.
	 *
	 * <p>For subclass use.</p>
	 */
	protected void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	/**
	 * Updates attack animation and hit detection.
	 *
	 * @param delta time since last frame in seconds
	 */
	public abstract void update(float delta);

	/**
	 * Starts an attack.
	 *
	 * @param angle facing angle in degrees (usually the player rotation)
	 */
	public abstract void startAttack(float angle);

	/** @return attack damage per hit */
	public float getAttackDamage() {
		return attackDamage;
	}

	/** @param attackDamage attack damage per hit */
	public void setAttackDamage(float attackDamage) {
		this.attackDamage = attackDamage;
	}

	/** @return attack duration in seconds */
	public float getAttackSpeed() {
		return attackSpeed;
	}

	/** @param attackSpeed attack duration in seconds */
	public void setAttackSpeed(float attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	/** @return effective range in world units */
	public float getRange() {
		return range;
	}

	/** @param range effective range in world units */
	public void setRange(float range) {
		this.range = range;
	}
}
