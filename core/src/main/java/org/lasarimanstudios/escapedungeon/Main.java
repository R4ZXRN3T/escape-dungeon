package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
	Texture backgroundTexture;
	SpriteBatch spriteBatch;
	FitViewport viewport;

	@Override
	public void create() {
		backgroundTexture = new Texture("test.png");
		spriteBatch = new SpriteBatch();
		viewport = new FitViewport(8, 5);
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
	}

	private void logic() {
	}

	private void draw() {
		ScreenUtils.clear(Color.BLACK);
		viewport.apply();
		spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.begin();

		float worldWidth = viewport.getWorldWidth();
		float worldHeight = viewport.getWorldHeight();

		spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

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
