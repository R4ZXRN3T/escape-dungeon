package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.graphics.HueShader;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * A sword that continuously cycles through rainbow hues while being drawn.
 *
 * <p>Uses the existing {@link HueShader} to apply a hue-rotation effect, just like
 * {@link org.lasarimanstudios.escapedungeon.entities.enemies.RgbGhost}.</p>
 */
public class RgbSaber extends Sword {

	private static final float HUE_SPEED = 0.2f;
	private float hueAnim = 0f;

	/**
	 * Creates an RGB saber.
	 *
	 * @param enemies      enemies that can be hit
	 * @param walls        walls used for line-of-sight checks
	 * @param texture      sword texture
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 * @param range        effective range (uniformly scales the sprite)
	 * @param knockback    knockback strength
	 * @param arcTexture   arc trail texture, or {@code null} for a generated fallback
	 */
	public RgbSaber(Array<Enemy> enemies, Array<Wall> walls, Texture texture, float attackDamage, float attackSpeed, float range, float knockback, Texture arcTexture) {
		super(enemies, walls, texture, attackDamage, attackSpeed, range, knockback, arcTexture);
	}

	/**
	 * Advances the hue animation and delegates to the standard sword update.
	 *
	 * @param delta time since last frame in seconds
	 */
	@Override
	public void update(float delta) {
		hueAnim = (hueAnim + delta * HUE_SPEED) % 1f;
		super.update(delta);
	}

	/**
	 * Draws the sword with a cycling hue-rotation shader applied.
	 * Falls back to normal rendering if the shader is unavailable.
	 *
	 * @param batch the sprite batch to draw with
	 */
	@Override
	public void draw(Batch batch) {
		boolean applied = HueShader.apply(batch, hueAnim, () -> super.draw(batch));
		if (!applied) {
			super.draw(batch);
		}
	}
}
