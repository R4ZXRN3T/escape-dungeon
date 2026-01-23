package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class Map {
	private Texture background;
	private Array<Wall> walls;

	public Map(String backgroundTexture, Array<Wall> walls) {
		this.background = new Texture(backgroundTexture);
		this.walls = walls;
	}

	public Texture getBackground() {
		return background;
	}

	public void setBackground(Texture background) {
		this.background = background;
	}

	public Array<Wall> getWalls() {
		return walls;
	}

	public void setWalls(Array<Wall> walls) {
		this.walls = walls;
	}
}
