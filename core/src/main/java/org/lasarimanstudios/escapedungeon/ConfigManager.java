package org.lasarimanstudios.escapedungeon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.swing.*;

public class ConfigManager {

	private static HashMap<String, String> configList;
	public static final String CONFIG_PATH = String.valueOf(getConfigFilePath());

	public static void init() {
		configList = new HashMap<>();
		readConfig();
	}

	public static void saveConfig() {
		writeConfig();
	}

	private static void readConfig() {
		try {
			File configFile = new File(new File(ConfigManager.CONFIG_PATH).getAbsolutePath());
			System.out.println("Reading config from " + configFile.getAbsolutePath());
			if (!configFile.exists() || configFile.length() == 0) {
				setDefaultConfig();
				return;
			}
			String fileContent = new String(java.nio.file.Files.readAllBytes(Path.of(configFile.getAbsolutePath())));
			JSONObject jsonObject = new JSONObject(fileContent);
			for (String currentKey : jsonObject.keySet()) {
				String content = jsonObject.getString(currentKey);
				configList.put(currentKey, content);
				System.out.println("Loaded config: " + currentKey + " = " + content);
			}
		} catch (JSONException e) {
			System.err.println("Config file is corrupted or invalid JSON: " + e.getMessage());
			setDefaultConfig();
		} catch (IOException e) {
			System.err.println("Failed to read config file: " + e.getMessage());
		}
	}

	private static void writeConfig() {
		File configFile = new File(new File(ConfigManager.CONFIG_PATH).getAbsolutePath());
		try (FileWriter writer = new FileWriter(configFile)) {
			if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs()) {
				System.err.println("Failed to create config directory");
				return;
			}
			JSONObject jsonToSave = new JSONObject(configList);
			for (String key : configList.keySet()) System.out.println("Saving config: " + key + " = " + configList.get(key));
			writer.write(jsonToSave.toString(4));
			writer.flush();
		} catch (IOException e) {
			System.err.println("Error writing config: " + e.getMessage());
		}
	}

	private static void setDefaultConfig() {
	}

	private static void setDefault(ConfigKey key) {
	}

	public static String getConfig(ConfigKey key) {
		return configList.getOrDefault(key.toString(), null);
	}

	public static Path getConfigFilePath() {
		String os = System.getProperty("os.name").toLowerCase();
		String fileName = "escape-dungeon/config.json";
		if (os.contains("win")) {
			String appData = System.getenv("APPDATA");
			return Paths.get(appData, fileName);
		} else if (os.contains("mac")) {
			String userHome = System.getProperty("user.home");
			return Paths.get(userHome, "Library", "Application Support", fileName);
		} else { // Linux and others
			String userHome = System.getProperty("user.home");
			return Paths.get(userHome, ".config", fileName);
		}
	}

	public static void setConfig(ConfigKey key, String value) {
		configList.put(key.toString(), value);
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

		private final String value;

		ConfigKey(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
}
