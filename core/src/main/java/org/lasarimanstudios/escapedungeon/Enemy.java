package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class Enemy extends Sprite {

	public Enemy( String texture, float posX, float posY, float width, float height) {
		super(new Texture(Gdx.files.internal("textures/enemy/" + texture)));
		setOriginCenter();
	}
}
