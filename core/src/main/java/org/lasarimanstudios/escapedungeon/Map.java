package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class Map {
	private Texture background;
	private Array<Wall> walls;
	private float width;
	private float height;
	private float startPosX;
	private float startPosY;

	public Map(String backgroundTexture, Array<Wall> walls, float width, float height, float startPosX, float startPosY) {
		this.background = new Texture(Gdx.files.internal("textures/maps/" + backgroundTexture));
		this.walls = walls;
		this.width = width;
		this.height = height;
		this.startPosX = startPosX;
		this.startPosY = startPosY;
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

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getStartPosX() {
		return startPosX;
	}

	public void setStartPosX(float startPosX) {
		this.startPosX = startPosX;
	}

	public float getStartPosY() {
		return startPosY;
	}

	public void setStartPosY(float startPosY) {
		this.startPosY = startPosY;
	}
}
