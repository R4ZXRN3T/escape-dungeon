package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads sword prices from {@code prices.json} in the assets directory.
 */
public final class PriceLoader {

	private static Map<String, Integer> prices;

	/**
	 * Loads and caches prices from {@code prices.json}.
	 * Subsequent calls return the cached map.
	 *
	 * @return unmodifiable map of sword type name → price
	 */
	public static Map<String, Integer> load() {
		if (prices != null) return prices;

		prices = new HashMap<>();
		try {
			FileHandle file = Gdx.files.internal("prices.json");
			JSONObject json = new JSONObject(file.readString("UTF-8"));
			for (String key : json.keySet()) prices.put(key, json.getInt(key));
		} catch (Exception e) {
			Gdx.app.error("PriceLoader", "Failed to load prices.json, falling back to 0 for all swords", e);
		}
		return prices;
	}

	/**
	 * Returns the price for the given sword type name, or 0 if not found.
	 *
	 * @param swordTypeName the {@link org.lasarimanstudios.escapedungeon.weapons.SwordType} enum name
	 * @return price in currency units, or 0 if not defined
	 */
	public static int getPrice(String swordTypeName) {
		return load().getOrDefault(swordTypeName, 0);
	}
}
