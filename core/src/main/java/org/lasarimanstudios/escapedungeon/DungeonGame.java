package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class DungeonGame extends Game {
	@Override
	public void create() {
		setScreen(new MenuScreen(this));
	}

	public void openLevel(String mapName) {
		Gdx.graphics.setForegroundFPS(0);
		Gdx.graphics.setVSync(false);
		Map map = MapLoader.loadMap(mapName);
		setScreen(new LevelScreen(this, map));
	}

	public void openInventory() {
		setScreen(new InventoryScreen(this));
	}
}
