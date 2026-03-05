package org.lasarimanstudios.escapedungeon.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.GameAssets;
import org.lasarimanstudios.escapedungeon.entities.objects.BloodPuddle;
import org.lasarimanstudios.escapedungeon.entities.objects.Chest;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.entities.enemies.Goblin;
import org.lasarimanstudios.escapedungeon.level.Map;

/**
 * Runtime world state that owns dynamic entities like puddles/chests and provides update & drawing.
 */
public class World {
	private final Map map;
	private final GameAssets assets;

	private final Array<BloodPuddle> bloodPuddles = new Array<>();
	private final Array<Chest> chests = new Array<>();

	public World(Map map, GameAssets assets) {
		this.map = map;
		this.assets = assets;
	}

	public void update(float delta) {
		for (int i = bloodPuddles.size - 1; i >= 0; i--) {
			BloodPuddle p = bloodPuddles.get(i);
			p.update(delta);
			if (p.isExpired()) bloodPuddles.removeIndex(i);
		}

		for (int i = chests.size - 1; i >= 0; i--) {
			Chest c = chests.get(i);
			c.update(delta);
			if (c.isExpired()) chests.removeIndex(i);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.R)) {
			Goblin goblin = new Goblin("goblin-01-vorne-stehend.png", 3, 5, 30, 30, 1);
			goblin.setCharacter(map.getEnemies().first().getCharacter());
			map.getEnemies().add(goblin);
		}
	}

	public void draw(SpriteBatch batch) {
		for (BloodPuddle p : bloodPuddles) p.draw(batch);
		for (Chest c : chests) c.draw(batch);
		for (Enemy e : map.getEnemies()) e.draw(batch);
	}

	public void onEnemyDied(Enemy enemy) {
		float x = enemy.getX();
		float y = enemy.getY();

		bloodPuddles.add(new BloodPuddle(assets.getTexture(GameAssets.TEX_BLOOD_PUDDLE), x, y, 5f));
		map.getEnemies().removeValue(enemy, true);

		if (MathUtils.random(2) == 0) {
			chests.add(new Chest(assets.getTexture(GameAssets.TEX_CHEST), x, y, 20f));
		}
	}

	public Array<BloodPuddle> getBloodPuddles() {
		return bloodPuddles;
	}

	public Array<Chest> getChests() {
		return chests;
	}

	public Map getMap() {
		return map;
	}
}

