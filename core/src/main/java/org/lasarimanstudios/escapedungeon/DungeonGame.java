package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

import org.lasarimanstudios.escapedungeon.ConfigManager.ConfigKey;
import org.lasarimanstudios.escapedungeon.assets.GameAssets;
import org.lasarimanstudios.escapedungeon.level.Map;
import org.lasarimanstudios.escapedungeon.level.MapLoader;
import org.lasarimanstudios.escapedungeon.screens.*;

/**
 * Main LibGDX {@link com.badlogic.gdx.Game} implementation.
 *
 * <p>Responsible for:</p>
 * <ul>
 *   <li>Initializing configuration ({@link ConfigManager})</li>
 *   <li>Applying window mode / FPS / VSync settings</li>
 *   <li>Managing transitions between screens (intro, menu, level, inventory, settings)</li>
 * </ul>
 */
public class DungeonGame extends Game {

	/**
	 * Initializes configuration and opens the intro screen.
	 */
	@Override
	public void create() {
		ConfigManager.init();
		SaveManager.init();
		applySettings();
		setScreen(new IntroScreen(this));
	}

	/**
	 * Applies window / FPS / VSync settings from {@link ConfigManager}.
	 *
	 * <p>This is used on startup and after the user presses "APPLY" in the settings menu.</p>
	 */
	public void applySettings() {
		int windowMode = ConfigManager.getInt(ConfigKey.WINDOW_MODE, 0, 2);
		switch (windowMode) {
			case 1 -> setBorderless();
			case 2 -> setFullscreen();
			default -> setWindowed();
		}
		Gdx.graphics.setForegroundFPS(ConfigManager.getInt(ConfigKey.MAX_FPS, 0, Integer.MAX_VALUE));
		Gdx.graphics.setVSync(ConfigManager.getBoolean(ConfigKey.VSYNC));
	}

	/**
	 * Sets the game to borderless windowed mode at the current display resolution.
	 */
	private void setBorderless() {
		Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
		Gdx.graphics.setUndecorated(true);
		Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
	}

	/**
	 * Sets the game to decorated windowed mode at 1280x720.
	 */
	private void setWindowed() {
		Gdx.graphics.setUndecorated(false);
		Gdx.graphics.setWindowedMode(1280, 720);
	}

	/**
	 * Sets the game to exclusive fullscreen mode using the current display mode.
	 */
	private void setFullscreen() {
		Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
		Gdx.graphics.setFullscreenMode(displayMode);
	}

	/**
	 * Switches to the main menu.
	 */
	public void openMenu() {
		setScreen(new MenuScreen(this, null));
	}

	/**
	 * Loads a map and switches to the gameplay screen.
	 *
	 * @param mapName map identifier without file extension (e.g. {@code "map_01"})
	 */
	public void openLevel(String mapName) {
		GameAssets assets = new GameAssets();
		assets.load();
		Map map = MapLoader.loadMap(mapName, assets);
		setScreen(new LevelScreen(this, map, assets));
	}

	/**
	 * Switches to the inventory screen.
	 */
	public void openInventory() {
		setScreen(new EquipmentScreen(this));
	}

	/**
	 * Switches to the settings screen.
	 */
	public void openSettings() {
		setScreen(new SettingsScreen(this));
	}
}
