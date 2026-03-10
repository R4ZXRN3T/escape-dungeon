package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Developer / debug sword with togglable range and infinite damage.
 *
 * <p>Pressing {@code Q} toggles between the default high range and a minimal range of 2.
 * Unlike the standard {@link Sword}, this variant ignores line-of-sight checks and applies
 * zero knockback.</p>
 */
public class DevSword extends Sword {

	private boolean highRange = true;

	/**
	 * Creates a dev sword.
	 *
	 * @param enemies      enemies that can be hit
	 * @param walls        walls (unused for line-of-sight in this variant)
	 * @param texture      sword texture
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 * @param range        initial range (uniformly scales the sprite)
	 * @param knockback    knockback strength
	 * @param arcTexture   arc trail texture, or {@code null} for a generated fallback
	 */
	public DevSword(Array<Enemy> enemies, Array<Wall> walls, Texture texture, float attackDamage, float attackSpeed, float range, float knockback, Texture arcTexture) {
		super(enemies, walls, texture, attackDamage, attackSpeed, range, knockback, arcTexture);
	}

	/**
	 * Updates the swing animation. Pressing {@code Q} toggles between high and low range.
	 * Damage is applied without line-of-sight or knockback.
	 *
	 * @param delta time since last frame in seconds
	 */
	@Override
	public void update(float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			setRange(highRange ? 2f : 20f);
			highRange = !highRange;
		}

		if (!isAttacking()) {
			showArc = false;
			return;
		}

		elapsedTime += delta;

		float totalDuration = getAttackSpeed();
		float t = MathUtils.clamp(elapsedTime / totalDuration, 0f, 1f);

		float forwardPortion = 3f / 5f;

		float swingT = t / forwardPortion;
		float angle = MathUtils.lerp(startAngle, endAngle,
			MathUtils.sin(swingT * MathUtils.PI / 2f));
		showArc = true;

		setRotation(angle);

		if (t >= 1f) {
			setAttacking(false);
			showArc = false;
		}

		for (Enemy enemy : enemies)
			if (enemy.getBoundingRectangle().overlaps(getBoundingRectangle()))
				enemy.takeDamage(getAttackDamage(), 0f, angle, getAttackInstanceId());
	}
}
