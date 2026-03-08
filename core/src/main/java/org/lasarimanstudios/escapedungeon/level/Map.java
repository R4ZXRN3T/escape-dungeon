package org.lasarimanstudios.escapedungeon.level;

import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Level data container.
 *
 * <p>A {@code Map} bundles:</p>
 * <ul>
 *   <li>Background texture (by asset path)</li>
 *   <li>Static walls</li>
 *   <li>Initial enemies</li>
 *   <li>World dimensions</li>
 *   <li>Player starting position</li>
 * </ul>
 */
public class Map {
	private final String backgroundPath;
	private final Array<Wall> walls;
	private final float startPosX;
	private final float startPosY;
	private Array<Enemy> enemies;
	private float width;
	private float height;

	/**
	 * Creates a map.
	 *
	 * @param backgroundPath background texture internal path (e.g. {@code textures/maps/test.png})
	 */
	public Map(String backgroundPath, Array<Wall> walls, Array<Enemy> enemies, float width, float height, float startPosX, float startPosY) {
		this.backgroundPath = backgroundPath;
		this.walls = walls;
		this.enemies = enemies;
		this.width = width;
		this.height = height;
		this.startPosX = startPosX;
		this.startPosY = startPosY;
	}

	/**
	 * @return background texture
	 */
	public String getBackgroundPath() {
		return backgroundPath;
	}

	/**
	 * @return walls in this map
	 */
	public Array<Wall> getWalls() {
		return walls;
	}

	/**
	 * @return world width in world units
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @param width world width in world units
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
	 * @param height world height in world units
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * @return player start X in world units
	 */
	public float getStartPosX() {
		return startPosX;
	}

	/**
	 * @return player start Y in world units
	 */
	public float getStartPosY() {
		return startPosY;
	}

	/**
	 * @return enemies present in the map
	 */
	public Array<Enemy> getEnemies() {
		return enemies;
	}

	/**
	 * @param enemies new enemy list
	 */
	public void setEnemies(Array<Enemy> enemies) {
		this.enemies = enemies;
	}
}
