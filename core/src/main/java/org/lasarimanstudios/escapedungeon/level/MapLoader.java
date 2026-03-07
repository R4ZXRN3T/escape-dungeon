package org.lasarimanstudios.escapedungeon.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import org.json.JSONObject;
import org.lasarimanstudios.escapedungeon.GameAssets;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.entities.enemies.Ghost;
import org.lasarimanstudios.escapedungeon.entities.enemies.Goblin;
import org.lasarimanstudios.escapedungeon.entities.enemies.RgbGhost;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Loads {@link Map} instances from JSON level files under {@code assets/levels/}.
 */
public class MapLoader {

	/**
	 * Loads a map from {@code levels/<mapName>.json}.
	 *
	 * @param mapName map identifier without {@code .json}
	 * @param assets  asset registry used to load enemy textures
	 * @return parsed map instance
	 * @throws RuntimeException if reading or parsing fails
	 */
	public static Map loadMap(String mapName, GameAssets assets) {
		try {
			String jsonText = Gdx.files.internal("levels/" + mapName + ".json").readString();
			JSONObject mapJson = new JSONObject(jsonText);

			String backgroundFile = mapJson.getString("background");
			String backgroundPath = "textures/maps/" + backgroundFile;
			float width = mapJson.getFloat("width");
			float height = mapJson.getFloat("height");
			float startPosX = mapJson.getFloat("startPosX");
			float startPosY = mapJson.getFloat("startPosY");

			Array<Wall> wallArray = getWalls(mapJson, assets);
			Array<Enemy> enemyArray = getEnemies(mapJson, assets);

			return new Map(backgroundPath, wallArray, enemyArray, width, height, startPosX, startPosY);
		} catch (Exception e) {
			throw new RuntimeException("Error reading json: " + e);
		}
	}

	/**
	 * Parses wall objects from the {@code walls} JSON array.
	 *
	 * @param mapJson root map JSON object
	 * @return walls array
	 */
	private static Array<Wall> getWalls(JSONObject mapJson, GameAssets assets) {
		Array<Wall> wallArray = new Array<>();

		for (Object wallValueObject : mapJson.getJSONArray("walls")) {
			JSONObject wallJson = (JSONObject) wallValueObject;
			String wallTexture = wallJson.getString("texture");
			float wallWidth = wallJson.getFloat("width");
			float wallHeight = wallJson.getFloat("height");
			float wallPosX = wallJson.getFloat("posX");
			float wallPosy = wallJson.getFloat("posY");

			Texture tex = assets.getWallTexture(wallTexture);
			wallArray.add(new Wall(tex, wallWidth, wallHeight, wallPosX, wallPosy));
		}

		return wallArray;
	}

	/**
	 * Parses enemy objects from the {@code enemies} JSON array.
	 *
	 * @param mapJson root map JSON object
	 * @param assets  shared asset registry used to load enemy textures
	 * @return enemies array
	 */
	private static Array<Enemy> getEnemies(JSONObject mapJson, GameAssets assets) {
		Array<Enemy> enemyArray = new Array<>();

		for (Object enemyValueObject : mapJson.getJSONArray("enemies")) {
			JSONObject enemyJson = (JSONObject) enemyValueObject;
			String enemyType = enemyJson.getString("enemyType");
			float enemyPosX = enemyJson.getFloat("posX");
			float enemyPosy = enemyJson.getFloat("posY");
			int level = enemyJson.getInt("level");

			Enemy enemy = getNewEnemy(assets, enemyType, enemyPosX, enemyPosy, level);

			enemyArray.add(enemy);
		}

		return enemyArray;
	}

	/**
	 * Factory method for enemies based on {@code enemyType}.
	 *
	 * @throws RuntimeException if {@code enemyType} is unknown
	 */
	private static Enemy getNewEnemy(GameAssets assets, String enemyType, float enemyPosX, float enemyPosY, int level) {
		return switch (enemyType) {
			case "goblin" -> new Goblin(assets, enemyPosX, enemyPosY, level);
			case "ghost" -> new Ghost(assets, enemyPosX, enemyPosY, level);
			case "rgbghost" -> new RgbGhost(assets, enemyPosX, enemyPosY, level);
			default -> throw new RuntimeException("Unknown enemy type: " + enemyType);
		};
	}
}

