package org.lasarimanstudios.escapedungeon.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import org.lasarimanstudios.escapedungeon.BloodPuddle;
import org.lasarimanstudios.escapedungeon.LevelScreen;

import org.lasarimanstudios.escapedungeon.Character;

public abstract class Enemy extends Sprite {

	private int level;
	private float maxHealth;
	private float remainingHealth;
	private float attackDamage;
	private float speed;
	private Character character;

	private LevelScreen levelScreen;

	public Enemy(String texture, float width, float height, float posX, float posY) {
		super(new Texture(Gdx.files.internal("textures/enemy/" + texture)));
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}
	public void setCharacter(Character character){
		this.character = character;
	}

	public Character getCharacter() {
		return character;
	}

	public void setLevelScreen(LevelScreen level) {
		this.levelScreen = level;
	}

	public LevelScreen getLevelScreen() {
		return levelScreen;
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
}
