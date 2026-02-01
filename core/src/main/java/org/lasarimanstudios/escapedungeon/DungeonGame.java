package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

/**
 * Main LibGDX {@link com.badlogic.gdx.Game} entry that manages screen transitions (menu, level, inventory).
 */
public class DungeonGame extends Game {
	/**
	 * Initializes the game and shows the main menu screen.
	 */
	@Override
	public void create() {
		Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
		Gdx.graphics.setUndecorated(true);
		Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
		Gdx.graphics.setForegroundFPS(0);
		Gdx.graphics.setVSync(false);
		setScreen(new IntroScreen(this));
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
