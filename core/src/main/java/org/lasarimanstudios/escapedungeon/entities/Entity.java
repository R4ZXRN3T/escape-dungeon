package org.lasarimanstudios.escapedungeon.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Base renderable entity type in the game.
 *
 * <p>Currently this is a thin wrapper around LibGDX's {@link com.badlogic.gdx.graphics.g2d.Sprite}
 * that standardizes construction from a {@link com.badlogic.gdx.graphics.Texture}. Game objects like
 * the player, enemies, and world props extend this class.</p>
 */
public abstract class Entity extends Sprite {

	/**
	 * Creates an entity sprite using the given texture.
	 *
	 * <p>The texture must already be loaded and is not owned/disposed by this entity.</p>
	 *
	 * @param texture texture to render for this entity
	 */
	public Entity(Texture texture) {
		super(texture);
	}
}
