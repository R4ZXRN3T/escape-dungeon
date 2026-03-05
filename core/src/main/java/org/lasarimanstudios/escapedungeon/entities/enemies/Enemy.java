package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.Entity;

public abstract class Enemy extends Entity {

	private int level;
	private float maxHealth;
	private float remainingHealth;
	private float attackDamage;
	private float speed;
	private Character character;

	private EnemyDeathListener deathListener;

	public Enemy(Texture texture, float width, float height, float posX, float posY) {
		super(texture);
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}

	public Character getCharacter() {
		return character;
	}

	public void setCharacter(Character character) {
		this.character = character;
	}

	public void setDeathListener(EnemyDeathListener deathListener) {
		this.deathListener = deathListener;
	}

	protected void notifyDied() {
		if (deathListener != null) deathListener.onEnemyDied(this);
	}

	public abstract void takeDamage(float damage, float knockback, float hitAngle);

	public abstract void die();

	public abstract void update(float delta);

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public float getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(float maxHealth) {
		this.maxHealth = maxHealth;
	}

	public float getRemainingHealth() {
		return remainingHealth;
	}

	public void setRemainingHealth(float remainingHealth) {
		this.remainingHealth = remainingHealth;
	}

	public float getAttackDamage() {
		return attackDamage;
	}

	public void setAttackDamage(float attackDamage) {
		this.attackDamage = attackDamage;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getCenterX() {
		return getX() + getWidth() * 0.5f;
	}

	public float getCenterY() {
		return getY() + getHeight() * 0.5f;
	}
}
