package org.lasarimanstudios.escapedungeon.world.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Static wall/obstacle sprite.
 *
 * <p>This class currently loads its own texture from {@code textures/objects/} in the constructor.
 * Because of that, callers are responsible for disposing the returned {@link Texture} when the wall
 * is no longer needed (or switching this to use {@link org.lasarimanstudios.escapedungeon.GameAssets}
 * later).</p>
 */
public class Wall extends Sprite {
	/**
	 * Creates a wall sprite.
	 *
	 * @param textureFileName texture file name (relative to {@code textures/objects/})
	 * @param width           wall width in world units
	 * @param height          wall height in world units
	 * @param posX            wall X position in world units
	 * @param posY            wall Y position in world units
	 */
	public Wall(String textureFileName, float width, float height, float posX, float posY) {
		super(new Texture(Gdx.files.internal("textures/objects/" + textureFileName)));
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}
}
