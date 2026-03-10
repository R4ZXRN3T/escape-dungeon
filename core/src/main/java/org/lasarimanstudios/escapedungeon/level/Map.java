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
	 * @param walls          static wall obstacles
	 * @param enemies        initial enemy list
	 * @param width          world width in world units
	 * @param height         world height in world units
	 * @param startPosX      player start X position in world units
	 * @param startPosY      player start Y position in world units
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
	 * Returns the background texture internal path.
	 *
	 * @return background texture path
	 */
	public String getBackgroundPath() {
		return backgroundPath;
	}

	/**
	 * Returns the walls in this map.
	 *
	 * @return wall array
	 */
	public Array<Wall> getWalls() {
		return walls;
	}

	/**
	 * Returns the world width in world units.
	 *
	 * @return world width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Sets the world width.
	 *
	 * @param width world width in world units
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * Returns the world height in world units.
	 *
	 * @return world height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Sets the world height.
	 *
	 * @param height world height in world units
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * Returns the player start X position in world units.
	 *
	 * @return player start X
	 */
	public float getStartPosX() {
		return startPosX;
	}

	/**
	 * Returns the player start Y position in world units.
	 *
	 * @return player start Y
	 */
	public float getStartPosY() {
		return startPosY;
	}

	/**
	 * Returns the enemies present in the map.
	 *
	 * @return enemy array
	 */
	public Array<Enemy> getEnemies() {
		return enemies;
	}

	/**
	 * Replaces the enemy list.
	 *
	 * @param enemies new enemy list
	 */
	public void setEnemies(Array<Enemy> enemies) {
		this.enemies = enemies;
	}
}
