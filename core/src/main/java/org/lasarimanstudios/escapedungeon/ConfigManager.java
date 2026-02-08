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

public final class ConfigManager {

	private static final Object LOCK = new Object();
	private static boolean initialized = false;

	private static final Path CONFIG_PATH = getConfigFilePath();
	private static final Map<ConfigKey, String> config = new EnumMap<>(ConfigKey.class);

	private ConfigManager() {
	}

	public static void init() {
		synchronized (LOCK) {
			if (initialized) return;
			readConfigLocked();
			initialized = true;
		}
	}

	public static void saveConfig() {
		ensureInitialized();
		synchronized (LOCK) {
			writeConfigAtomicallyLocked();
		}
	}

	public static String getConfig(ConfigKey key) {
		ensureInitialized();
		synchronized (LOCK) {
			return config.getOrDefault(key, getDefault(key));
		}
	}

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

	public static void setConfig(ConfigKey key, String value) {
		ensureInitialized();
		synchronized (LOCK) {
			config.put(key, value);
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
			// Consider logging.
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

	public static Path getConfigFilePath() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String fileName = "escape-dungeon/config.json";

		if (os.contains("win")) {
			String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) return Paths.get(appData, fileName);
			return Paths.get(System.getProperty("user.home"), "AppData", "Roaming", fileName);
		} else if (os.contains("mac")) {
			return Paths.get(System.getProperty("user.home"), "Library", "Application Support", fileName);
		} else {
			return Paths.get(System.getProperty("user.home"), ".config", fileName);
		}
	}

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

		static ConfigKey fromJsonKey(String jsonKey) {
			return LOOKUP.get(jsonKey);
		}

		@Override
		public String toString() {
			return jsonKey;
		}
	}
}
