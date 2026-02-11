package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class Enemy extends Sprite {

	public Enemy(String texture, float width, float height, float posX, float posY) {
		super(new Texture(Gdx.files.internal("textures/enemy/" + texture)));
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}
}
