package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
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
public class DevSword extends Sword {

	private boolean highRange = true;

	public DevSword(Array<Enemy> enemies, Array<Wall> walls, Texture texture, float attackDamage, float attackSpeed, float range, Texture arcTexture) {
		super(enemies, walls, texture, attackDamage, attackSpeed, range, arcTexture);
	}

	@Override
	public void update(float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			setRange(highRange ? 20f : 2f);
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
		float angle;

		float swingT = t / forwardPortion;
		angle = MathUtils.lerp(startAngle, endAngle,
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
