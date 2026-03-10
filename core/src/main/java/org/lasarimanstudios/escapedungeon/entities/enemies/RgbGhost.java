package org.lasarimanstudios.escapedungeon.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.lasarimanstudios.escapedungeon.assets.Direction;
import org.lasarimanstudios.escapedungeon.assets.DirectionalAnimationSet;
import org.lasarimanstudios.escapedungeon.assets.EnemySpriteSet;
import org.lasarimanstudios.escapedungeon.assets.AssetManager;
import org.lasarimanstudios.escapedungeon.graphics.HueShader;

/**
 * A ghost enemy that continuously cycles through hues (RGB rainbow tint) while retaining its
 * collision and behavioral logic.
 *
 * <p>The visual hue is applied by tinting the enemy sprite color during drawing. The enemy
 * supports knockback velocities which are frame-damped and a short damage-invulnerability window.</p>
 */
public class RgbGhost extends Enemy {

	private static final float BASE_HEALTH = 200f;
	private static final float BASE_ATTACK_DAMAGE = 15f;
	private static final float BASE_SPEED = 15f;

	private static final float KNOCKBACK_DAMPING_PER_SECOND = 18f;
	private static final float KNOCKBACK_VELOCITY_EPS = 0.05f;
	private static final float HUE_SPEED = 0.2f;
	private final EnemySpriteSet spriteSet;
	private final DirectionalAnimationSet walkAnimations;
	private float damageInvulnerabilityTime = 0.3f;
	private float knockbackVx = 0f;
	private float knockbackVy = 0f;
	private Direction facing = Direction.FRONT;
	private float stateTimeSeconds = 0f;
	private boolean walking = false;
	private float hueAnim = 0f;

	/**
	 * Creates an RGB-cycling ghost at the specified world position.
	 *
	 * @param assets assets container providing enemy sprites
	 * @param posX   initial X position in world units
	 * @param posY   initial Y position in world units
	 * @param level  enemy level for stat scaling
	 */
	public RgbGhost(AssetManager assets, float posX, float posY, int level) {
		super(assets.getEnemySpriteSet("ghost").getFrontIdleTexture(), 6.46f, 10f, posX, posY);
		this.spriteSet = assets.getEnemySpriteSet("ghost");
		// Ghost only has idle frames per direction, so walk animation falls back to idle.
		this.walkAnimations = spriteSet.createWalkAnimations(0.18f);
		applyVisualRegion(spriteSet.getIdle(Direction.FRONT));
		setLevel(level);
		setMaxHealth((float) (BASE_HEALTH * Math.pow(1.2, level)));
		setRemainingHealth(getMaxHealth());
		setAttackDamage((float) (BASE_ATTACK_DAMAGE * Math.pow(1.2, level)));
		setSpeed(BASE_SPEED);
	}

	/**
	 * Applies damage to this enemy and accumulates knockback velocity.
	 *
	 * <p>Damage from the same attack instance is ignored after the first hit; a short
	 * invulnerability window prevents repeated immediate damage.</p>
	 *
	 * @param damage           damage amount to subtract from health
	 * @param knockback        knockback impulse magnitude to add to the current velocity
	 * @param hitAngle         angle of the hit in radians (0 = +X axis)
	 * @param attackInstanceId id of the attacking weapon animation instance
	 */
	@Override
	public void takeDamage(float damage, float knockback, float hitAngle, int attackInstanceId) {
		if (!shouldAcceptDamageFromAttack(attackInstanceId)) return;
		if (damageInvulnerabilityTime > 0f) return;

		markDamagedByAttack(attackInstanceId);
		setRemainingHealth(getRemainingHealth() - damage);
		damageInvulnerabilityTime = 0.3f;

		float dx = (float) Math.cos(hitAngle);
		float dy = (float) Math.sin(hitAngle);
		knockbackVx += dx * knockback;
		knockbackVy += dy * knockback;

		if (getRemainingHealth() <= 0f) die();
	}

	/**
	 * Per-frame update: advances hue animation, applies knockback with exponential damping,
	 * and otherwise performs follow behavior.
	 *
	 * @param delta time since last frame in seconds
	 */
	@Override
	public void update(float delta) {
		stateTimeSeconds += delta;
		damageInvulnerabilityTime -= delta;

		// advance hue animation
		hueAnim = (hueAnim + delta * HUE_SPEED) % 1f;

		if (Math.abs(knockbackVx) > 0f || Math.abs(knockbackVy) > 0f) {
			setX(getX() + knockbackVx * delta);
			setY(getY() + knockbackVy * delta);

			float decay = (float) Math.exp(-KNOCKBACK_DAMPING_PER_SECOND * delta);
			knockbackVx *= decay;
			knockbackVy *= decay;

			if (Math.abs(knockbackVx) < KNOCKBACK_VELOCITY_EPS) knockbackVx = 0f;
			if (Math.abs(knockbackVy) < KNOCKBACK_VELOCITY_EPS) knockbackVy = 0f;
			walking = false;
		} else {
			following(delta);
		}

		updateVisual();
	}

	/**
	 * Updates the sprite region based on the current facing direction and walking state.
	 */
	private void updateVisual() {
		if (spriteSet == null) return;
		TextureRegion region;
		if (walking) {
			region = walkAnimations.getKeyFrame(facing, stateTimeSeconds, true);
		} else {
			region = spriteSet.getIdle(facing);
		}
		applyVisualRegion(region);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void die() {
		notifyDied();
	}

	/**
	 * Moves toward the configured player character when within aggro range.
	 *
	 * @param delta frame time in seconds
	 */
	public void following(float delta) {
		float oldX = getX();
		float oldY = getY();

		float diffX = getCharacter().getX() - getX();
		float diffY = getCharacter().getY() - getY();
		float length = (float) Math.sqrt(diffX * diffX + diffY * diffY);

		walking = false;
		if (length > 0 && length < 35) {
			float dirX = diffX / length;
			float dirY = diffY / length;

			setX(getX() + dirX * getSpeed() * delta);
			setY(getY() + dirY * getSpeed() * delta);
			walking = true;
			updateFacing(getX() - oldX, getY() - oldY);
		}
	}

	/**
	 * Updates the facing direction based on the movement delta.
	 *
	 * @param dx horizontal movement since last frame
	 * @param dy vertical movement since last frame
	 */
	private void updateFacing(float dx, float dy) {
		if (dx == 0f && dy == 0f) return;
		if (Math.abs(dx) > Math.abs(dy)) {
			facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
		} else {
			facing = dy > 0 ? Direction.BACK : Direction.FRONT;
		}
	}

	/**
	 * Draws the enemy using a hue-tinted sprite. The sprite color is temporarily changed to the
	 * desired HSV->RGB color and restored after drawing to avoid affecting other draw calls.
	 *
	 * @param batch sprite batch used for drawing
	 */
	@Override
	public void draw(Batch batch) {
		HueShader.apply(batch, hueAnim, () -> super.draw(batch));
	}
}
