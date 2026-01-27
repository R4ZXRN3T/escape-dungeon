package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Wall extends Sprite {
	public Wall(String texture, float width, float height, float posX, float posY) {
		super(new Texture(Gdx.files.internal("textures/objects/" + texture)));
		setBounds(posX, posY, width, height);
		setOriginCenter();
	}
}
