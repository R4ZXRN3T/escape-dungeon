package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Game;

public class DungeonGame extends Game {
	@Override
	public void create() {
		setScreen(new MenuScreen(this));
	}

	public void openLevel(String mapName) {
		Map map = MapLoader.loadMap(mapName);
		setScreen(new LevelScreen(this, map));
	}

	public void openInventory() {
		setScreen(new InventoryScreen(this));
	}
}
