package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Input;
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
 * Simple JSON-backed configuration store.
 *
 * <p>Values are persisted to a platform-specific config file path (see {@link #getConfigFilePath()}).
 * The config is loaded lazily on first use (or eagerly via {@link #init()}).</p>
 *
 * <h2>Thread safety</h2>
 * All public methods are thread-safe. Access is guarded by an internal lock.
 *
 * <h2>Types</h2>
 * Values are stored as strings. Convenience getters ({@link #getInt(ConfigKey, int, int)} and
 * {@link #getBoolean(ConfigKey)}) parse and normalize values and write normalized values back into
 * the in-memory map.
 */
public final class ConfigManager {

	private static final Object LOCK = new Object();
	private static final Path CONFIG_PATH = getConfigFilePath();
	private static final Map<ConfigKey, String> config = new EnumMap<>(ConfigKey.class);
	private static boolean initialized = false;

	/**
	 * Initializes the configuration system.
	 *
	 * <p>If the config file does not exist (or is empty/invalid), defaults are written.</p>
	 */
	public static void init() {
		synchronized (LOCK) {
			if (initialized) return;
			readConfigLocked();
			initialized = true;
		}
	}

	/**
	 * Writes the current configuration to disk.
	 *
	 * <p>Call {@link #setConfig(ConfigKey, String)} to change values and then call this method to
	 * persist them.</p>
	 */
	public static void saveConfig() {
		ensureInitialized();
		synchronized (LOCK) {
			writeConfigAtomicallyLocked();
		}
	}

	/**
	 * Returns the raw stored value for the given key.
	 *
	 * @param key config key
	 * @return stored value or the default for {@code key} if missing
	 */
	public static String getConfig(ConfigKey key) {
		ensureInitialized();
		synchronized (LOCK) {
			return config.getOrDefault(key, getDefault(key));
		}
	}

	/**
	 * Returns a config value parsed as an integer.
	 *
	 * <p>If the value is missing or cannot be parsed, the default value is used. The value is then
	 * clamped into the provided range. If parsing/clamping changes the value, the normalized value is
	 * stored back into the in-memory config.</p>
	 *
	 * @param key config key
	 * @param min minimum allowed value (inclusive)
	 * @param max maximum allowed value (inclusive)
	 * @return normalized integer value
	 */
	public static int getInt(ConfigKey key, int min, int max) {
		ensureInitialized();
		synchronized (LOCK) {
			String raw = config.get(key);
			int value;
			try {
				value = Integer.parseInt(raw);
			} catch (Exception e) {
				value = Integer.parseInt(getDefault(key));
			}

			value = MathUtils.clamp(value, min, max);
			String normalized = String.valueOf(value);
			if (!normalized.equals(raw)) {
				config.put(key, normalized);
			}
			return value;
		}
	}

	/**
	 * Returns a config value parsed as a boolean.
	 *
	 * <p>If the key is missing, the default value is used. Normalization is written back into the
	 * in-memory config map.</p>
	 *
	 * @param key config key
	 * @return boolean value
	 */
	public static boolean getBoolean(ConfigKey key) {
		ensureInitialized();
		synchronized (LOCK) {
			String raw = config.get(key);
			if (raw == null) raw = getDefault(key);

			boolean value = Boolean.parseBoolean(raw);
			String normalized = String.valueOf(value);
			if (!normalized.equalsIgnoreCase(raw)) {
				config.put(key, normalized);
			}
			return value;
		}
	}

	/**
	 * Sets a raw config value in memory.
	 *
	 * <p>This does not persist automatically. Call {@link #saveConfig()} to write to disk.</p>
	 *
	 * @param key   config key
	 * @param value raw value to store
	 */
	public static void setConfig(ConfigKey key, String value) {
		ensureInitialized();
		synchronized (LOCK) {
			config.put(key, value);
		}
	}

	/**
	 * Returns the config file location for the current operating system.
	 *
	 * @return path to {@code escape-dungeon/config.json}
	 */
	public static Path getConfigFilePath() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String fileName = "config.json";

		if (os.contains("win")) {
			String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) return Paths.get(appData, fileName);
			return Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "escape-dungeon", fileName);
		} else if (os.contains("mac")) {
			return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "escape-dungeon", fileName);
		} else {
			return Paths.get(System.getProperty("user.home"), ".config", "escape-dungeon", fileName);
		}
	}

	private static void ensureInitialized() {
		if (!initialized) {
			init();
		}
	}

	private static void readConfigLocked() {
		try {
			if (!Files.exists(CONFIG_PATH) || Files.size(CONFIG_PATH) == 0) {
				setDefaultsLocked();
				writeConfigAtomicallyLocked();
				return;
			}

			String fileContent = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
			JSONObject jsonObject = new JSONObject(fileContent);

			setDefaultsLocked();
			for (String jsonKey : jsonObject.keySet()) {
				ConfigKey key = ConfigKey.fromJsonKey(jsonKey);
				if (key == null) continue;

				Object v = jsonObject.opt(jsonKey);
				if (v == null || v == JSONObject.NULL) continue;

				config.put(key, String.valueOf(v));
			}
		} catch (JSONException e) {
			setDefaultsLocked();
			writeConfigAtomicallyLocked();
		} catch (IOException e) {
			setDefaultsLocked();
		}
	}

	private static void writeConfigAtomicallyLocked() {
		try {
			Path parent = CONFIG_PATH.getParent();
			if (parent != null) Files.createDirectories(parent);

			JSONObject jsonToSave = new JSONObject();
			for (ConfigKey key : ConfigKey.values()) {
				jsonToSave.put(key.jsonKey, config.getOrDefault(key, getDefault(key)));
			}

			Path tmp = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
			Files.writeString(tmp, jsonToSave.toString(4), StandardCharsets.UTF_8);

			try {
				Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(tmp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ignored) {
			// Intentionally ignored (consider logging).
		}
	}

	private static void setDefaultsLocked() {
		for (ConfigKey key : ConfigKey.values()) {
			config.putIfAbsent(key, getDefault(key));
		}
	}

	private static String getDefault(ConfigKey key) {
		return switch (key) {
			case WINDOW_MODE -> "0";
			case MAX_FPS -> "60";
			case VSYNC -> "true";
			case SHOW_FPS -> "false";
			case FORWARD_KEY -> String.valueOf(Input.Keys.W);
			case BACKWARD_KEY -> String.valueOf(Input.Keys.S);
			case LEFT_KEY -> String.valueOf(Input.Keys.A);
			case RIGHT_KEY -> String.valueOf(Input.Keys.D);
			case ATTACK_KEY -> String.valueOf(Input.Buttons.LEFT);
		};
	}

	/**
	 * Supported configuration keys.
	 *
	 * <p>Each key maps to a stable JSON property name.</p>
	 */
	public enum ConfigKey {
		WINDOW_MODE("windowMode"),
		MAX_FPS("maxFps"),
		VSYNC("vSync"),
		SHOW_FPS("showFps"),
		FORWARD_KEY("forwardKey"),
		BACKWARD_KEY("backwardKey"),
		LEFT_KEY("leftKey"),
		RIGHT_KEY("rightKey"),
		ATTACK_KEY("attackKey");

		private static final Map<String, ConfigKey> LOOKUP = new HashMap<>();

		static {
			for (ConfigKey k : values()) LOOKUP.put(k.jsonKey, k);
		}

		private final String jsonKey;

		ConfigKey(String jsonKey) {
			this.jsonKey = jsonKey;
		}

		/**
		 * Looks up a {@link ConfigKey} by its JSON property name.
		 *
		 * @param jsonKey JSON property name
		 * @return matching key, or {@code null} if unknown
		 */
		static ConfigKey fromJsonKey(String jsonKey) {
			return LOOKUP.get(jsonKey);
		}

		/**
		 * @return JSON property name for this key
		 */
		@Override
		public String toString() {
			return jsonKey;
		}
	}
}
