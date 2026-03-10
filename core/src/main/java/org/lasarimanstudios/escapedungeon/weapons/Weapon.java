package org.lasarimanstudios.escapedungeon.weapons;

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
	private final float attackDamage;
	private final float attackSpeed;
	private boolean attacking;
	/**
	 * Monotonically increasing id that identifies the current/most recent attack.
	 *
	 * <p>This is used to ensure enemies are only hit once per attack animation, even though hit
	 * detection may run every frame.</p>
	 */
	private int attackInstanceId = 0;

	/**
	 * Creates a weapon sprite.
	 *
	 * @param texture      weapon texture (must already be loaded)
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 */
	public Weapon(Texture texture, float attackDamage, float attackSpeed) {
		super(texture);
		setOrigin(getAttachmentOriginX(), getAttachmentOriginY());
		this.attackDamage = attackDamage;
		this.attackSpeed = attackSpeed;
	}

	/**
	 * @return whether the weapon is currently attacking
	 */
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
	 * Starts a new logical attack instance.
	 *
	 * <p>Subclasses should call this exactly once when they begin an attack animation. The returned id
	 * should be passed along with damage calls so targets can ignore duplicate hits within the same
	 * attack.</p>
	 */
	protected void beginAttackInstance() {
		// Avoid returning 0 as a valid id (0 can be treated as "no instance" by callers if needed).
		attackInstanceId++;
		if (attackInstanceId == 0) attackInstanceId = 1;
	}

	/**
	 * @return id that identifies the current/most recent attack instance
	 */
	public int getAttackInstanceId() {
		return attackInstanceId;
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

	/**
	 * @return attack damage per hit
	 */
	public float getAttackDamage() {
		return attackDamage;
	}

	/**
	 * @return attack duration in seconds
	 */
	public float getAttackSpeed() {
		return attackSpeed;
	}

	/**
	 * Attachment-origin helpers:
	 * By default these return -0.5 * current width/height which matches the previous behaviour
	 * (origin placed to the left/below the sprite center). Subclasses can override these to provide
	 * a fixed origin that doesn't change when the visual size (width/height) changes.
	 */
	public float getAttachmentOriginX() {
		return -0.5f * getWidth();
	}

	public float getAttachmentOriginY() {
		return -0.5f * getHeight();
	}

}
