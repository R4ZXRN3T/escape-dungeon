package org.lasarimanstudios.escapedungeon;

import org.json.JSONObject;

import com.badlogic.gdx.utils.Array;
import java.io.File;

public class MapLoader {

	public static Map loadMap(String mapName) {
		File mapConfig = new File("levels/" + mapName + ".json");
		JSONObject mapJson = new JSONObject(mapConfig);

		String background = mapJson.getString("background");
		float width = mapJson.getFloat("width");
		float height = mapJson.getFloat("height");
		float startPosX = mapJson.getFloat("startPosX");
		float startPosY = mapJson.getFloat("startPosY");

		Array<Wall> wallArray = getWalls(mapJson);

		return new Map(background, wallArray, width, height, startPosX, startPosY);
	}

	private static Array<Wall> getWalls(JSONObject mapJson) {
		Array<Wall> wallArray = new Array<>();

		for (Object wallValueObject : mapJson.getJSONArray("walls")) {
			String[] wallValueString = wallValueObject.toString().split(" ");
			String wallTexture = wallValueString[0];
			float wallWidth = Float.parseFloat(wallValueString[1]);
			float wallHeight = Float.parseFloat(wallValueString[2]);
			float wallPosX = Float.parseFloat(wallValueString[3]);
			float wallPosy = Float.parseFloat(wallValueString[4]);
			wallArray.add(new Wall(wallTexture, wallWidth, wallHeight, wallPosX, wallPosy));
		}

		return wallArray;
	}
}
