package org.lasarimanstudios.escapedungeon.entities.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.graphics.HueShader;

/**
 * A sword that continuously cycles through rainbow hues while being drawn.
 *
 * <p>Uses the existing {@link HueShader} to apply a hue-rotation effect, just like
 * {@link org.lasarimanstudios.escapedungeon.entities.enemies.RgbGhost}.</p>
 */
public class RgbSaber extends Sword {

	private static final float HUE_SPEED = 0.2f;
	private float hueAnim = 0f;

	public RgbSaber(Array<Enemy> enemies, Texture texture, float attackDamage, float attackSpeed, float range) {
		super(enemies, texture, attackDamage, attackSpeed, range);
	}

	@Override
	public void update(float delta) {
		hueAnim = (hueAnim + delta * HUE_SPEED) % 1f;
		super.update(delta);
	}

	@Override
	public void draw(Batch batch) {
		boolean applied = HueShader.apply(batch, hueAnim, () -> super.draw(batch));
		if (!applied) {
			super.draw(batch);
		}
	}
}

