package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Static wall/obstacle sprite with an axis-aligned bounding rectangle used for collision.
 *
 * <p>The wall loads its texture from {@code textures/objects/}. Callers are responsible for disposing
 * the texture when no longer needed.</p>
 */
public class Wall extends Sprite {
	/**
	 * Creates a wall sprite with the given texture, bounds, and origin.
	 *
	 * @param texture wall texture file name (relative to {@code textures/objects/})
	 * @param width   wall width in world units
	 * @param height  wall height in world units
	 * @param posX    wall X position in world units
	 * @param posY    wall Y position in world units
	 */
	public Wall(String texture, float width, float height, float posX, float posY) {
		super(new Texture(Gdx.files.internal("textures/objects/" + texture)));
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}
}
