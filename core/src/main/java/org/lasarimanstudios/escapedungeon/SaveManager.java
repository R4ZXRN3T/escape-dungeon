package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.math.MathUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON-backed save-data store.
 *
 * <p>Values are persisted to a platform-specific save file path (see {@link #getSaveFilePath()}).
 * The save data is loaded lazily on first use (or eagerly via {@link #init()}).</p>
 *
 * <h2>Thread safety</h2>
 * All public methods are thread-safe. Access is guarded by an internal lock.
 *
 * <h2>Types</h2>
 * Values are stored as strings. Convenience getters ({@link #getInt(SaveKey, int, int)} and
 * {@link #getBoolean(SaveKey)}) parse and normalize values and write normalized values back into
 * the in-memory map.
 */
public final class SaveManager {

	private static final Object LOCK = new Object();
	private static final Path SAVE_PATH = getSaveFilePath();
	private static final Map<SaveKey, String> saveData = new EnumMap<>(SaveKey.class);
	private static final Map<String, SaveKey> SWORD_OWNERSHIP_LOOKUP = new HashMap<>();
	private static boolean initialized = false;

	static {
		SWORD_OWNERSHIP_LOOKUP.put("BASIC", SaveKey.HAS_BASIC_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("IRON", SaveKey.HAS_IRON_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("BLUE", SaveKey.HAS_BLUE_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("GOLD", SaveKey.HAS_GOLD_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("PINK", SaveKey.HAS_PINK_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("YELLOW", SaveKey.HAS_YELLOW_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("FAT", SaveKey.HAS_FAT_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("RAINBOW", SaveKey.HAS_RAINBOW_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("DEV", SaveKey.HAS_DEV_SWORD);
		SWORD_OWNERSHIP_LOOKUP.put("RGB_SABER", SaveKey.HAS_RGB_SABER);
	}

	/**
	 * Initializes the save system.
	 *
	 * <p>If the save file does not exist (or is empty/invalid), defaults are written.</p>
	 */
	public static void init() {
		synchronized (LOCK) {
			if (initialized) return;
			readSaveLocked();
			initialized = true;
		}
	}

	/**
	 * Writes the current save data to disk.
	 *
	 * <p>Call {@link #set(SaveKey, String)} to change values and then call this method to
	 * persist them.</p>
	 */
	public static void save() {
		ensureInitialized();
		synchronized (LOCK) {
			writeSaveAtomicallyLocked();
		}
	}

	/**
	 * Returns the raw stored value for the given key.
	 *
	 * @param key save key
	 * @return stored value or the default for {@code key} if missing
	 */
	public static String get(SaveKey key) {
		ensureInitialized();
		synchronized (LOCK) {
			return saveData.getOrDefault(key, getDefault(key));
		}
	}

	/**
	 * Returns a save value parsed as an integer.
	 *
	 * <p>If the value is missing or cannot be parsed, the default value is used. The value is then
	 * clamped into the provided range. If parsing/clamping changes the value, the normalized value is
	 * stored back into the in-memory save data.</p>
	 *
	 * @param key save key
	 * @param min minimum allowed value (inclusive)
	 * @param max maximum allowed value (inclusive)
	 * @return normalized integer value
	 */
	public static int getInt(SaveKey key, int min, int max) {
		ensureInitialized();
		synchronized (LOCK) {
			String raw = saveData.get(key);
			int value;
			try {
				value = Integer.parseInt(raw);
			} catch (Exception e) {
				value = Integer.parseInt(getDefault(key));
			}

			value = MathUtils.clamp(value, min, max);
			String normalized = String.valueOf(value);
			if (!normalized.equals(raw)) {
				saveData.put(key, normalized);
			}
			return value;
		}
	}

	/**
	 * Returns a save value parsed as a boolean.
	 *
	 * <p>If the key is missing, the default value is used. Normalization is written back into the
	 * in-memory save data map.</p>
	 *
	 * @param key save key
	 * @return boolean value
	 */
	public static boolean getBoolean(SaveKey key) {
		ensureInitialized();
		synchronized (LOCK) {
			String raw = saveData.get(key);
			if (raw == null) raw = getDefault(key);

			boolean value = Boolean.parseBoolean(raw);
			String normalized = String.valueOf(value);
			if (!normalized.equalsIgnoreCase(raw)) {
				saveData.put(key, normalized);
			}
			return value;
		}
	}

	/**
	 * Sets a raw save value in memory.
	 *
	 * <p>This does not persist automatically. Call {@link #save()} to write to disk.</p>
	 *
	 * @param key   save key
	 * @param value raw value to store
	 */
	public static void set(SaveKey key, String value) {
		ensureInitialized();
		synchronized (LOCK) {
			saveData.put(key, value);
		}
	}

	/**
	 * Add a certain amount of money to memory.
	 *
	 * <p>This does not persist automatically. Call {@link #save()} to write to disk.</p>
	 *
	 * @param amount amount of money to add (can be negative)
	 */
	public static void addMoney(int amount) {
		ensureInitialized();
		synchronized (LOCK) {
			saveData.put(SaveKey.MONEY, getInt(SaveKey.MONEY, Integer.MIN_VALUE, Integer.MAX_VALUE) + amount + "");
		}
	}

	/**
	 * Returns the save file location for the current operating system.
	 *
	 * @return path to {@code escape-dungeon/save.json}
	 */
	public static Path getSaveFilePath() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String fileName = "save.json";

		if (os.contains("win")) {
			String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) return Paths.get(appData, "escape-dungeon", fileName);
			return Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "escape-dungeon", fileName);
		} else if (os.contains("mac")) {
			return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "escape-dungeon", fileName);
		} else {
			return Paths.get(System.getProperty("user.home"), ".local", "share", "escape-dungeon", fileName);
		}
	}

	/**
	 * Returns the {@link SaveKey} that tracks ownership of the given sword type name.
	 *
	 * @param swordTypeName enum name of the sword type (e.g. "IRON", "GOLD")
	 * @return matching save key, or {@code null} if there is no ownership key for that sword
	 */
	public static SaveKey getOwnershipKey(String swordTypeName) {
		return SWORD_OWNERSHIP_LOOKUP.get(swordTypeName);
	}

	private static void ensureInitialized() {
		if (!initialized) {
			init();
		}
	}

	/**
	 * Reads the save file from disk into the in-memory map. If the file is missing,
	 * empty, or contains invalid JSON, defaults are applied (and persisted).
	 */
	private static void readSaveLocked() {
		try {
			if (!Files.exists(SAVE_PATH) || Files.size(SAVE_PATH) == 0) {
				setDefaultsLocked();
				writeSaveAtomicallyLocked();
				return;
			}

			String fileContent = Files.readString(SAVE_PATH, StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(fileContent);

			setDefaultsLocked();
			for (String jsonKey : jsonObject.keySet()) {
				SaveKey key = SaveKey.fromJsonKey(jsonKey);
				if (key == null) continue;

				Object v = jsonObject.opt(jsonKey);
				if (v == null || v == JSONObject.NULL) continue;

				saveData.put(key, String.valueOf(v));
			}
		} catch (JSONException e) {
			setDefaultsLocked();
			writeSaveAtomicallyLocked();
		} catch (IOException e) {
			setDefaultsLocked();
		}
	}

	/**
	 * Writes the in-memory save data to disk atomically. A temporary file is written
	 * first and then moved into place to avoid corruption on crash.
	 */
	private static void writeSaveAtomicallyLocked() {
		try {
			Path parent = SAVE_PATH.getParent();
			if (parent != null) Files.createDirectories(parent);

			JSONObject jsonToSave = new JSONObject();
			for (SaveKey key : SaveKey.values()) {
				jsonToSave.put(key.jsonKey, saveData.getOrDefault(key, getDefault(key)));
			}

			Path tmp = SAVE_PATH.resolveSibling(SAVE_PATH.getFileName() + ".tmp");
			Files.writeString(tmp, jsonToSave.toString(4), StandardCharsets.UTF_8);

			try {
				Files.move(tmp, SAVE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(tmp, SAVE_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ignored) {
		}
	}

	/**
	 * Populates the in-memory map with default values for any keys not already present.
	 */
	private static void setDefaultsLocked() {
		for (SaveKey key : SaveKey.values()) {
			saveData.putIfAbsent(key, getDefault(key));
		}
	}

	/**
	 * Returns the default value for the given save key.
	 *
	 * @param key save key
	 * @return the default value as a string
	 */
	private static String getDefault(SaveKey key) {
		return switch (key) {
			case HAS_BASIC_SWORD -> "true";
			case HAS_IRON_SWORD,
				 HAS_BLUE_SWORD,
				 HAS_GOLD_SWORD,
				 HAS_PINK_SWORD,
				 HAS_YELLOW_SWORD,
				 HAS_FAT_SWORD,
				 HAS_RAINBOW_SWORD,
				 HAS_DEV_SWORD,
				 HAS_RGB_SABER -> "false";
			case MONEY -> "0";
			case EQUIPPED_SWORD -> "BASIC";
		};
	}

	/**
	 * Supported save-data keys.
	 *
	 * <p>Each key maps to a stable JSON property name.</p>
	 */
	public enum SaveKey {
		HAS_BASIC_SWORD("hasBasicSword"),
		HAS_IRON_SWORD("hasIronSword"),
		HAS_BLUE_SWORD("hasBlueSword"),
		HAS_GOLD_SWORD("hasGoldSword"),
		HAS_PINK_SWORD("hasPinkSword"),
		HAS_YELLOW_SWORD("hasYellowSword"),
		HAS_FAT_SWORD("hasFatSword"),
		HAS_RAINBOW_SWORD("hasRainbowSword"),
		HAS_DEV_SWORD("hasDevSword"),
		HAS_RGB_SABER("hasRgbSaber"),
		MONEY("money"),
		EQUIPPED_SWORD("equippedSword");

		private static final Map<String, SaveKey> LOOKUP = new HashMap<>();

		static {
			for (SaveKey k : values()) LOOKUP.put(k.jsonKey, k);
		}

		private final String jsonKey;

		SaveKey(String jsonKey) {
			this.jsonKey = jsonKey;
		}

		/**
		 * Looks up a {@link SaveKey} by its JSON property name.
		 *
		 * @param jsonKey JSON property name
		 * @return matching key, or {@code null} if unknown
		 */
		static SaveKey fromJsonKey(String jsonKey) {
			return LOOKUP.get(jsonKey);
		}

		/**
		 * Returns the JSON property name for this key.
		 *
		 * @return JSON property name
		 */
		@Override
		public String toString() {
			return jsonKey;
		}
	}
}
