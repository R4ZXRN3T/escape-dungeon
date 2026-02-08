package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import org.lasarimanstudios.escapedungeon.ConfigManager.ConfigKey;

/**
 * Main LibGDX {@link com.badlogic.gdx.Game} entry that manages screen transitions (menu, level, inventory).
 */
public class DungeonGame extends Game {
	/**
	 * Initializes the game and shows the main menu screen.
	 */
	@Override
	public void create() {
		ConfigManager.init();
		int windowMode = ConfigManager.getInt(ConfigKey.WINDOW_MODE, 0, 2);
		switch (windowMode) {
			case 1 -> setBorderless();
			case 2 -> setFullscreen();
			default -> setWindowed();
		}
		Gdx.graphics.setForegroundFPS(ConfigManager.getInt(ConfigKey.MAX_FPS, 0, Integer.MAX_VALUE));
		Gdx.graphics.setVSync(ConfigManager.getBoolean(ConfigKey.VSYNC));
		setScreen(new IntroScreen(this));
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
	 * Sets the game to windowed mode with a fixed resolution of 1280x720.
	 */
	private void setWindowed() {
		Gdx.graphics.setUndecorated(false);
		Gdx.graphics.setWindowedMode(1280, 720);
	}

	/**
	 * Sets the game to exclusive fullscreen mode at the current display resolution.
	 */
	private void setFullscreen() {
		Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
		Gdx.graphics.setFullscreenMode(displayMode);
	}

	/**
	 * Loads a level map and switches to the gameplay screen.
	 *
	 * <p>Also disables VSync and removes the foreground FPS cap for gameplay.</p>
	 *
	 * @param mapName level/map identifier without file extension (resolved from {@code levels/<mapName>.json})
	 * @throws RuntimeException if the map cannot be loaded or parsed
	 */
	public void openLevel(String mapName) {
		Map map = MapLoader.loadMap(mapName);
		setScreen(new LevelScreen(this, map));
	}

	/**
	 * Switches to the inventory screen.
	 */
	public void openInventory() {
		setScreen(new InventoryScreen(this));
	}
}
