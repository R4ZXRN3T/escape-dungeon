package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
	Texture backgroundTexture;
	Texture characterTexture;
	Texture wallTexture;
	SpriteBatch spriteBatch;
	FitViewport viewport;
	Sprite characterSprite;
	Sprite wallSprite;

	@Override
	public void create() {
		backgroundTexture = new Texture("test.png");
		characterTexture = new Texture("character.png");
		wallTexture = new Texture("wall.png");
		spriteBatch = new SpriteBatch();
		wallSprite = new Sprite(wallTexture);
		characterSprite = new Sprite(characterTexture);
		characterSprite.setSize(5, 5);
		viewport = new FitViewport(80, 50);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}

	@Override
	public void render() {
		input();
		logic();
		draw();
	}

	private void input() {
		float speed = 22f;
		float delta = Gdx.graphics.getDeltaTime();

		float multiplier = 2f / 3f;

		if ((Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.W)) || (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.D))) {
			return;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
			characterSprite.translateX(speed * delta * multiplier);
			characterSprite.translateY(-speed * delta * multiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
			characterSprite.translateX(-speed * delta * multiplier);
			characterSprite.translateY(speed * delta * multiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) {
			characterSprite.translateX(speed * delta * multiplier);
			characterSprite.translateY(speed * delta * multiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) {
			characterSprite.translateX(-speed * delta * multiplier);
			characterSprite.translateY(-speed * delta * multiplier);
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			characterSprite.translateX(speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			characterSprite.translateX(-speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			characterSprite.translateY(speed * delta);
		} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			characterSprite.translateY(-speed * delta);
		}
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
		viewport.apply();
		spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.begin();

		float worldWidth = viewport.getWorldWidth();
		float worldHeight = viewport.getWorldHeight();

		spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
		characterSprite.draw(spriteBatch);

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

	}
}
