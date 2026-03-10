package org.lasarimanstudios.escapedungeon.world.tiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import org.lasarimanstudios.escapedungeon.assets.GameAssets;

/**
 * Static wall/obstacle sprite.
 *
 * <p>Textures are provided externally (typically via {@link GameAssets})
 * and are therefore not owned/disposed by this class.</p>
 */
public class Wall extends Sprite {
	private final Texture texture;

	/**
	 * Creates a wall sprite at the specified position and size.
	 *
	 * @param texture wall texture (must already be loaded; not owned by this instance)
	 * @param width   wall width in world units
	 * @param height  wall height in world units
	 * @param posX    X position in world units
	 * @param posY    Y position in world units
	 */
	public Wall(Texture texture, float width, float height, float posX, float posY) {
		super(texture);
		this.texture = texture;
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}

	/**
	 * Returns the texture used by this wall.
	 *
	 * @return the wall texture
	 */
	public Texture getTexture() {
		return texture;
	}
}
