package org.lasarimanstudios.escapedungeon.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Level data container.
 *
 * <p>A {@code Map} bundles:</p>
 * <ul>
 *   <li>Background texture</li>
 *   <li>Static walls</li>
 *   <li>Initial enemies</li>
 *   <li>World dimensions</li>
 *   <li>Player starting position</li>
 * </ul>
 *
 * <h2>Resource ownership</h2>
 * The background texture is loaded in the constructor and must be disposed by the code that owns the
 * map (typically the gameplay screen).
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
	 * @param enemies           enemies present at level start
	 * @param width             map/world width in world units
	 * @param height            map/world height in world units
	 * @param startPosX         player start X in world units
	 * @param startPosY         player start Y in world units
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

	/** @return background texture */
	public Texture getBackground() {
		return background;
	}

	/** @param background background texture */
	public void setBackground(Texture background) {
		this.background = background;
	}

	/** @return walls in this map */
	public Array<Wall> getWalls() {
		return walls;
	}

	/** @param walls new wall list */
	public void setWalls(Array<Wall> walls) {
		this.walls = walls;
	}

	/** @return world width in world units */
	public float getWidth() {
		return width;
	}

	/** @param width world width in world units */
	public void setWidth(float width) {
		this.width = width;
	}

	/** @return world height in world units */
	public float getHeight() {
		return height;
	}

	/** @param height world height in world units */
	public void setHeight(float height) {
		this.height = height;
	}

	/** @return player start X in world units */
	public float getStartPosX() {
		return startPosX;
	}

	/** @param startPosX player start X in world units */
	public void setStartPosX(float startPosX) {
		this.startPosX = startPosX;
	}

	/** @return player start Y in world units */
	public float getStartPosY() {
		return startPosY;
	}

	/** @param startPosY player start Y in world units */
	public void setStartPosY(float startPosY) {
		this.startPosY = startPosY;
	}

	/** @return enemies present in the map */
	public Array<Enemy> getEnemies() {
		return enemies;
	}

	/** @param enemies new enemy list */
	public void setEnemies(Array<Enemy> enemies) {
		this.enemies = enemies;
	}

}
