package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class LevelScreen extends ScreenAdapter {
	private final DungeonGame game;
	private final Map map;
	private SpriteBatch spriteBatch;
	private FitViewport viewport;
	private Camera camera;
	private Character characterSprite;

	public LevelScreen(DungeonGame game, Map map) {
		this.game = game;
		this.map = map;

		spriteBatch = new SpriteBatch();
		viewport = new FitViewport(map.getWidth(), map.getHeight());
		camera = viewport.getCamera();
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		characterSprite = new Character("character.png", 5, 5);
		characterSprite.setPosition(map.getStartPosX(), map.getStartPosY());

		camera.update();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		camera.update();
	}

	@Override
	public void render(float delta) {
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
		spriteBatch.draw(map.getBackground(), 0, 0, worldWidth, worldHeight);
		characterSprite.draw(spriteBatch);
		for (Wall wall : map.getWalls()) {
			wall.draw(spriteBatch);
		}
		spriteBatch.end();
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		characterSprite.getTexture().dispose();
		if (map.getBackground() != null) map.getBackground().dispose();
		for (Wall w : map.getWalls()) {
			if (w.getTexture() != null) w.getTexture().dispose();
		}
	}
}
