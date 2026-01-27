package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Level extends Game {

	Texture backgroundTexture;
	SpriteBatch spriteBatch;
	Array<Wall> wallArray;
	FitViewport viewport;
	Character characterSprite;
	Wall wallSprite;
	Camera camera;
	float width;
	float height;
	float startPosX;
	float startPosY;

	public Level(Map map) {
		this.backgroundTexture = map.getBackground();
		this.wallArray = map.getWalls();
		this.width = map.getWidth();
		this.height = map.getHeight();
		this.startPosX = map.getStartPosX();
		this.startPosY = map.getStartPosY();
	}

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();

		viewport = new FitViewport(this.width, this.height);
		camera = viewport.getCamera();
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		characterSprite = new Character("character.png", 5, 5);

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
		for(Wall wall : wallArray) {
			wall.draw(spriteBatch);
		}
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
