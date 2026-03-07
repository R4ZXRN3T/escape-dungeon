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

	public Wall(Texture texture, float width, float height, float posX, float posY) {
		super(texture);
		this.texture = texture;
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}

	public Texture getTexture() {
		return texture;
	}
}
