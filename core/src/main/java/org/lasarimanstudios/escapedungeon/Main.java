package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
	Texture backgroundTexture;
	SpriteBatch spriteBatch;
	Array<Wall> spriteArray;
	FitViewport viewport;
	Character characterSprite;
	Wall wallSprite;
	Camera camera;

	@Override
	public void create() {
		backgroundTexture = new Texture(Gdx.files.internal("maps/test.png"));
		spriteBatch = new SpriteBatch();

		viewport = new FitViewport(80, 50);
		camera = viewport.getCamera();
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		characterSprite = new Character("characters/character.png", 5, 5);
		wallSprite = new Wall("objects/wall.png", 20, 20, 20, 20);

		float x = (viewport.getWorldWidth() - characterSprite.getWidth()) * 0.5f;
		float y = (viewport.getWorldHeight() - characterSprite.getHeight()) * 0.5f;
		characterSprite.setPosition(x, y);

		camera.update();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		camera.update();
	}

	@Override
	public void render() {
		viewport.apply();
		camera.update();
		spriteBatch.setProjectionMatrix(camera.combined);

		characterSprite.run(viewport);

		logic();
		draw();
	}

	private void logic() {
		float worldWidth = viewport.getWorldWidth();
		float worldHeight = viewport.getWorldHeight();
		float characterWidth = characterSprite.getWidth();
		float characterHeight = characterSprite.getHeight();

		characterSprite.setX(MathUtils.clamp(characterSprite.getX(), 0, worldWidth - characterWidth));
		characterSprite.setY(MathUtils.clamp(characterSprite.getY(), 0, worldHeight - characterHeight));
	}

	private void draw() {
		ScreenUtils.clear(Color.BLACK);

		spriteBatch.begin();
		float worldWidth = viewport.getWorldWidth();
		float worldHeight = viewport.getWorldHeight();
		spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
		characterSprite.draw(spriteBatch);
		wallSprite.draw(spriteBatch);
		spriteBatch.end();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		backgroundTexture.dispose();
		characterSprite.getTexture().dispose();
		wallSprite.getTexture().dispose();
	}
}
