package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

/**
 * Immutable-ish container for level data: background texture, wall sprites, world dimensions, and start position.
 *
 * <p>This class loads its background texture from disk in the constructor. Callers are responsible for disposing
 * textures (see {@link LevelScreen#dispose()}).</p>
 */
public class Map {
	private Texture background;
	private Array<Wall> walls;
	private Array<Enemy> enemies;
	private float width;
	private float height;
	private float startPosX;
	private float startPosY;

	/**
	 * Creates a map and loads the background texture from {@code textures/maps/}.
	 *
	 * @param backgroundTexture background texture file name (relative to {@code textures/maps/})
	 * @param walls             wall sprites/colliders in the level
	 * @param width             map/world width (world units)
	 * @param height            map/world height (world units)
	 * @param startPosX         character spawn X (world units)
	 * @param startPosY         character spawn Y (world units)
	 */
	public Map(String backgroundTexture, Array<Wall> walls, Array<Enemy> enemies, float width, float height, float startPosX, float startPosY) {
		this.background = new Texture(Gdx.files.internal("textures/maps/" + backgroundTexture));
		this.walls = walls;
		this.enemies = enemies;
		this.width = width;
		this.height = height;
		this.startPosX = startPosX;
		this.startPosY = startPosY;
	}



	/**
	 * @return the background texture for the map (maybe {@code null} if cleared externally)
	 */
	public Texture getBackground() {
		return background;
	}

	/**
	 * Sets the background texture reference.
	 *
	 * @param background new background texture
	 */
	public void setBackground(Texture background) {
		this.background = background;
	}

	/**
	 * @return walls in this map
	 */
	public Array<Wall> getWalls() {
		return walls;
	}

	/**
	 * Replaces the wall array reference.
	 *
	 * @param walls new walls array
	 */
	public void setWalls(Array<Wall> walls) {
		this.walls = walls;
	}

	/**
	 * @return world width in world units
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Sets the world width.
	 *
	 * @param width new width in world units
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * @return world height in world units
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Sets the world height.
	 *
	 * @param height new height in world units
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * @return character start X in world units
	 */
	public float getStartPosX() {
		return startPosX;
	}

	/**
	 * Sets the character start X coordinate.
	 *
	 * @param startPosX new start X in world units
	 */
	public void setStartPosX(float startPosX) {
		this.startPosX = startPosX;
	}

	/**
	 * @return character start Y in world units
	 */
	public float getStartPosY() {
		return startPosY;
	}

	/**
	 * Sets the character start Y coordinate.
	 *
	 * @param startPosY new start Y in world units
	 */
	public void setStartPosY(float startPosY) {
		this.startPosY = startPosY;
	}


	/**
	 * Replaces the Enemy array reference.
	 *
	 * @param enemies new walls array
	 */
	public void setEnemies(Array<Enemy> enemies) {
		this.enemies = enemies;
	}

	/**
	 * @return Enemies in this map
	 */
	public Array<Enemy> getEnemies() {
		return enemies;
	}









}
