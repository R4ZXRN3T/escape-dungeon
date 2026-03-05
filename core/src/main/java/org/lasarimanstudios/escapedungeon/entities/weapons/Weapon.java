package org.lasarimanstudios.escapedungeon.entities.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class Weapon extends Sprite {
	private boolean attacking;
	private float attackDamage;
	private float attackSpeed;
	private float range;

	public Weapon(Texture texture, float attackDamage, float attackSpeed, float range) {
		super(texture);
		this.attackDamage = attackDamage;
		this.attackSpeed = attackSpeed;
		this.range = range;
	}

	public boolean isAttacking() {
		return attacking;
	}

	protected void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public abstract void update(float delta);

	public abstract void startAttack(float angle);

	public float getAttackDamage() {
		return attackDamage;
	}

	public void setAttackDamage(float attackDamage) {
		this.attackDamage = attackDamage;
	}

	public float getAttackSpeed() {
		return attackSpeed;
	}

	public void setAttackSpeed(float attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public float getRange() {
		return range;
	}

	public void setRange(float range) {
		this.range = range;
	}
}
