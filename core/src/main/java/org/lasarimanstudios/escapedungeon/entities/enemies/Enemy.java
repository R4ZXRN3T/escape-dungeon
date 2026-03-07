package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

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
	private final Rectangle collisionBounds = new Rectangle();
	private final float visualBaseHeight;

	private EnemyDeathListener deathListener;

	/**
	 * Remembers which player attack instance last successfully damaged this enemy.
	 *
	 * <p>This prevents taking damage every frame while a weapon collider overlaps the enemy during a
	 * single swing.</p>
	 */
	private int lastDamagingAttackInstanceId = -1;

	/**
	 * Creates an enemy sprite.
	 *
	 * @param texture sprite texture (must already be loaded)
	 * @param width   logical collision width in world units
	 * @param height  logical collision / base visual height in world units
	 * @param posX    initial x position in world units
	 * @param posY    initial y position in world units
	 */
	public Enemy(Texture texture, float width, float height, float posX, float posY) {
		super(texture);
		this.visualBaseHeight = height;
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}

	/**
	 * Applies a frame while preserving its aspect ratio.
	 *
	 * <p>The enemy keeps a stable collision rectangle and base visual height. Only the rendered width
	 * changes to match the frame's aspect ratio, anchored bottom-center so the sprite does not appear
	 * to wobble sideways when switching directions.</p>
	 */
	protected void applyVisualRegion(TextureRegion region) {
		if (region == null) return;

		setRegion(region);

		float regionWidth = Math.max(1, region.getRegionWidth());
		float regionHeight = Math.max(1, region.getRegionHeight());
		float aspectRatio = regionWidth / regionHeight;
		float visualWidth = visualBaseHeight * aspectRatio;
		float visualX = collisionBounds.x + (collisionBounds.width - visualWidth) * 0.5f;

		super.setBounds(visualX, collisionBounds.y, visualWidth, visualBaseHeight);
		setOriginCenter();
	}

	/**
	 * @return stable collision rectangle used for damage and pushback checks
	 */
	public Rectangle getCollisionBounds() {
		return collisionBounds;
	}

	@Override
	public Rectangle getBoundingRectangle() {
		return collisionBounds;
	}

	@Override
	public void setBounds(float x, float y, float width, float height) {
		collisionBounds.set(x, y, width, height);
		super.setBounds(x, y, width, height);
	}

	@Override
	public void setX(float x) {
		float dx = x - super.getX();
		collisionBounds.x += dx;
		super.setX(x);
	}

	@Override
	public void setY(float y) {
		float dy = y - super.getY();
		collisionBounds.y += dy;
		super.setY(y);
	}

	@Override
	public void setPosition(float x, float y) {
		float dx = x - super.getX();
		float dy = y - super.getY();
		collisionBounds.x += dx;
		collisionBounds.y += dy;
		super.setPosition(x, y);
	}

	@Override
	public void translate(float xAmount, float yAmount) {
		collisionBounds.x += xAmount;
		collisionBounds.y += yAmount;
		super.translate(xAmount, yAmount);
	}

	@Override
	public void translateX(float xAmount) {
		collisionBounds.x += xAmount;
		super.translateX(xAmount);
	}

	@Override
	public void translateY(float yAmount) {
		collisionBounds.y += yAmount;
		super.translateY(yAmount);
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
	 * Returns whether this enemy should accept damage for the given attack instance.
	 *
	 * <p>If the same weapon attack calls damage multiple times (e.g., once per frame while overlapping),
	 * only the first call will be accepted.</p>
	 */
	protected boolean shouldAcceptDamageFromAttack(int attackInstanceId) {
		return attackInstanceId != lastDamagingAttackInstanceId;
	}

	/**
	 * Marks the given attack instance as the last one that successfully damaged this enemy.
	 */
	protected void markDamagedByAttack(int attackInstanceId) {
		this.lastDamagingAttackInstanceId = attackInstanceId;
	}

	/**
	 * Applies damage to the enemy.
	 *
	 * @param damage           damage amount
	 * @param knockback        knockback strength (interpretation depends on implementation)
	 * @param hitAngle         direction of the hit (implementation-defined; {@link Goblin} expects radians)
	 * @param attackInstanceId id that identifies the attacker weapon's current attack animation
	 */
	public abstract void takeDamage(float damage, float knockback, float hitAngle, int attackInstanceId);

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

	/**
	 * @return enemy level used for stat scaling
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level enemy level used for stat scaling
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return maximum health
	 */
	public float getMaxHealth() {
		return maxHealth;
	}

	/**
	 * @param maxHealth maximum health
	 */
	public void setMaxHealth(float maxHealth) {
		this.maxHealth = maxHealth;
	}

	/**
	 * @return remaining health
	 */
	public float getRemainingHealth() {
		return remainingHealth;
	}

	/**
	 * @param remainingHealth remaining health
	 */
	public void setRemainingHealth(float remainingHealth) {
		this.remainingHealth = remainingHealth;
	}

	/**
	 * @return attack damage per hit
	 */
	public float getAttackDamage() {
		return attackDamage;
	}

	/**
	 * @param attackDamage attack damage per hit
	 */
	public void setAttackDamage(float attackDamage) {
		this.attackDamage = attackDamage;
	}

	/**
	 * @return movement speed in world units per second
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed movement speed in world units per second
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	/**
	 * @return sprite center x coordinate
	 */
	public float getCenterX() {
		return collisionBounds.x + collisionBounds.width * 0.5f;
	}

	/**
	 * @return sprite center y coordinate
	 */
	public float getCenterY() {
		return collisionBounds.y + collisionBounds.height * 0.5f;
	}
}
