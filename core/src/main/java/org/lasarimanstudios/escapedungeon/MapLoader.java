package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import org.json.JSONObject;
import org.lasarimanstudios.escapedungeon.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.enemies.Goblin;

/**
 * Utility for loading {@link Map} instances from JSON level files located under {@code levels/}.
 */
public class MapLoader {

	/**
	 * Loads a map from {@code assets/levels/<mapName>.json}.
	 *
	 * <p>Expected JSON keys include: {@code background}, {@code width}, {@code height}, {@code startPosX},
	 * {@code startPosY}, and {@code walls}.</p>
	 *
	 * @param mapName map identifier without {@code .json} extension
	 * @return loaded {@link Map}
	 * @throws RuntimeException if the file cannot be read or the JSON format is invalid
	 */
	public static Map loadMap(String mapName) {
		try {

			String jsonText = Gdx.files.internal("levels/" + mapName + ".json").readString();
			JSONObject mapJson = new JSONObject(jsonText);

			String background = mapJson.getString("background");
			float width = mapJson.getFloat("width");
			float height = mapJson.getFloat("height");
			float startPosX = mapJson.getFloat("startPosX");
			float startPosY = mapJson.getFloat("startPosY");

			Array<Wall> wallArray = getWalls(mapJson);
			Array<Enemy> enemyArray = getEnemies(mapJson);

			return new Map(background, wallArray, enemyArray, width, height, startPosX, startPosY);
		} catch (Exception e) {
			throw new RuntimeException("Error reading json: " + e);
		}
	}

	/**
	 * Parses the {@code walls} array from the given JSON object.
	 *
	 * <p>Each wall entry is expected to be a space-separated string:
	 * {@code "<texture> <width> <height> <posX> <posY>"}.</p>
	 *
	 * @param mapJson root map JSON object containing a {@code walls} array
	 * @return array of parsed {@link Wall} instances
	 */
	private static Array<Wall> getWalls(JSONObject mapJson) {
		Array<Wall> wallArray = new Array<>();

		for (Object wallValueObject : mapJson.getJSONArray("walls")) {
			JSONObject wallJson = (JSONObject) wallValueObject;
			String wallTexture = wallJson.getString("texture");
			float wallWidth = wallJson.getFloat("width");
			float wallHeight = wallJson.getFloat("height");
			float wallPosX = wallJson.getFloat("posX");
			float wallPosy = wallJson.getFloat("posY");
			wallArray.add(new Wall(wallTexture, wallWidth, wallHeight, wallPosX, wallPosy));
		}

		return wallArray;
	}

	private static Array<Enemy> getEnemies(JSONObject mapJson) {
		Array<Enemy> enemyArray = new Array<>();

		for (Object enemyValueObject : mapJson.getJSONArray("enemies")) {
			JSONObject enemyJson = (JSONObject) enemyValueObject;
			String enemyType = enemyJson.getString("enemyType");
			String enemyTexture = enemyJson.getString("texture");
			float enemyWidth = enemyJson.getFloat("width");
			float enemyHeight = enemyJson.getFloat("height");
			float enemyPosX = enemyJson.getFloat("posX");
			float enemyPosy = enemyJson.getFloat("posY");
			int level = enemyJson.getInt("level");

			Enemy enemy = getNewEnemy(enemyType, enemyTexture, enemyWidth, enemyHeight, enemyPosX, enemyPosy, level);

			enemyArray.add(enemy);
		}

		return enemyArray;

	}

	private static Enemy getNewEnemy(String enemyType, String enemyTexture, float enemyWidth, float enemyHeight, float enemyPosX, float enemyPosy, int level) {
		return switch(enemyType) {
			case "goblin" -> new Goblin(enemyTexture, enemyWidth, enemyHeight, enemyPosX, enemyPosy, level);
			default -> throw new RuntimeException("Unknown enemy type: " + enemyType);
		};
	}
}


