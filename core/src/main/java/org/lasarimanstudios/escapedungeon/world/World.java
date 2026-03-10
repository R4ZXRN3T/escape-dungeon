package org.lasarimanstudios.escapedungeon.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.assets.AssetManager;
import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.entities.enemies.Goblin;
import org.lasarimanstudios.escapedungeon.entities.objects.BloodPuddle;
import org.lasarimanstudios.escapedungeon.entities.objects.Chest;
import org.lasarimanstudios.escapedungeon.level.Map;

/**
 * Runtime world state that owns dynamic entities like puddles/chests and provides update & drawing.
 */
public class World {
	private final Map map;
	private final AssetManager assets;
	private final Array<BloodPuddle> bloodPuddles = new Array<>();
	private final Array<Chest> chests = new Array<>();
	private Character playerCharacter;

	public World(Map map, AssetManager assets) {
		this.map = map;
		this.assets = assets;
	}

	/**
	 * Provides the player character reference used by newly spawned enemies for AI/follow behavior.
	 */
	public void setPlayerCharacter(Character playerCharacter) {
		this.playerCharacter = playerCharacter;
	}

	/**
	 * Adds an enemy to the map and wires required runtime dependencies (player ref + death callback).
	 */
	public void registerEnemy(Enemy enemy) {
		if (enemy == null) return;

		// Ensure dynamically spawned enemies behave consistently with map-loaded enemies.
		enemy.setDeathListener(this::onEnemyDied);
		if (playerCharacter != null) {
			enemy.setCharacter(playerCharacter);
		}

		map.getEnemies().add(enemy);
	}

	public void update(float delta) {
		for (int i = bloodPuddles.size - 1; i >= 0; i--) {
			BloodPuddle p = bloodPuddles.get(i);
			p.update(delta);
			if (p.isExpired()) bloodPuddles.removeIndex(i);
		}

		for (int i = chests.size - 1; i >= 0; i--) {
			Chest c = chests.get(i);
			c.update(delta, playerCharacter);
			if (c.isExpired()) chests.removeIndex(i);
		}

		// Spawn test enemy.
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			Goblin goblin = new Goblin(assets, map.getWalls(), 30, 30, 1);
			registerEnemy(goblin);
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

		bloodPuddles.add(new BloodPuddle(assets.getTexture(AssetManager.TEX_BLOOD_PUDDLE), x, y, 5f));
		map.getEnemies().removeValue(enemy, true);

		if (MathUtils.random(2) == 0) {
			chests.add(new Chest(assets.getTexture(AssetManager.TEX_CHEST_CLOSED), (assets.getTexture(AssetManager.TEX_CHEST_OPEN)), x, y, 20f));
		}
	}

	public void onPlayerDied(Character character) {
		float x = character.getX();
		float y = character.getY();

		bloodPuddles.add(new BloodPuddle(assets.getTexture(AssetManager.TEX_BLOOD_PUDDLE), x, y, 5f));
	}
}
