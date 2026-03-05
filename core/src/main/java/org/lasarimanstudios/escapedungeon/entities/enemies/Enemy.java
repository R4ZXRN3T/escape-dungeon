package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.Texture;

import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.Entity;

/**
 * Base type for enemies.
 *
 * <p>An enemy is a renderable sprite with combat stats. Implementations typically update movement/AI
 * in {@link #update(float)} and notify the world about death via {@link #die()} + {@link #notifyDied()}.</p>
 *
 * <p>Many enemies depend on a player {@link org.lasarimanstudios.escapedungeon.entities.Character}
 * reference for following/attacking behavior. The owning world/screen should set it via
 * {@link #setCharacter(org.lasarimanstudios.escapedungeon.entities.Character)}.</p>
 */
public abstract class Enemy extends Entity {

	private int level;
	private float maxHealth;
	private float remainingHealth;
	private float attackDamage;
	private float speed;
	private Character character;

	private EnemyDeathListener deathListener;

	/**
	 * Creates an enemy sprite.
	 *
	 * @param texture sprite texture (must already be loaded)
	 * @param width   sprite width in world units
	 * @param height  sprite height in world units
	 * @param posX    initial x position in world units
	 * @param posY    initial y position in world units
	 */
	public Enemy(Texture texture, float width, float height, float posX, float posY) {
		super(texture);
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}

	/**
	 * @return player character reference used for AI (may be {@code null})
	 */
	public Character getCharacter() {
		return character;
	}

	/**
	 * Sets the player character reference used for AI.
	 *
	 * @param character player character (may be {@code null})
	 */
	public void setCharacter(Character character) {
		this.character = character;
	}

	/**
	 * Sets a listener to be notified when this enemy dies.
	 *
	 * @param deathListener listener to notify (may be {@code null})
	 */
	public void setDeathListener(EnemyDeathListener deathListener) {
		this.deathListener = deathListener;
	}

	/**
	 * Notifies the registered {@link EnemyDeathListener}, if any.
	 */
	protected void notifyDied() {
		if (deathListener != null) deathListener.onEnemyDied(this);
	}

	/**
	 * Applies damage to the enemy.
	 *
	 * @param damage    damage amount
	 * @param knockback knockback strength (interpretation depends on implementation)
	 * @param hitAngle  direction of the hit (implementation-defined; {@link Goblin} expects radians)
	 */
	public abstract void takeDamage(float damage, float knockback, float hitAngle);

	/**
	 * Called when the enemy should be considered dead.
	 *
	 * <p>Implementations should call {@link #notifyDied()} so the owning world can remove the enemy and
	 * spawn effects/loot.</p>
	 */
	public abstract void die();

	/**
	 * Updates the enemy for a frame.
	 *
	 * @param delta time since last frame in seconds
	 */
	public abstract void update(float delta);

	/** @return enemy level used for stat scaling */
	public int getLevel() {
		return level;
	}

	/** @param level enemy level used for stat scaling */
	public void setLevel(int level) {
		this.level = level;
	}

	/** @return maximum health */
	public float getMaxHealth() {
		return maxHealth;
	}

	/** @param maxHealth maximum health */
	public void setMaxHealth(float maxHealth) {
		this.maxHealth = maxHealth;
	}

	/** @return remaining health */
	public float getRemainingHealth() {
		return remainingHealth;
	}

	/** @param remainingHealth remaining health */
	public void setRemainingHealth(float remainingHealth) {
		this.remainingHealth = remainingHealth;
	}

	/** @return attack damage per hit */
	public float getAttackDamage() {
		return attackDamage;
	}

	/** @param attackDamage attack damage per hit */
	public void setAttackDamage(float attackDamage) {
		this.attackDamage = attackDamage;
	}

	/** @return movement speed in world units per second */
	public float getSpeed() {
		return speed;
	}

	/** @param speed movement speed in world units per second */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	/** @return sprite center x coordinate */
	public float getCenterX() {
		return getX() + getWidth() * 0.5f;
	}

	/** @return sprite center y coordinate */
	public float getCenterY() {
		return getY() + getHeight() * 0.5f;
	}
}
