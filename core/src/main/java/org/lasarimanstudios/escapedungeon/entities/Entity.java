package org.lasarimanstudios.escapedungeon.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class Entity extends Sprite {

	/**
	 * Creates an entity with the given texture.
	 *
	 * @param texture the texture to use for this entity
	 */
	public Entity(Texture texture) {
		super(texture);
	}
}
