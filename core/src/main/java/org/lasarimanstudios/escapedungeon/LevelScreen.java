package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Gameplay screen that renders a {@link Map}, updates the {@link Character}, and follows the character
 * with an orthographic camera.
 */
public class LevelScreen extends ScreenAdapter {
	private final DungeonGame game;
	private final Map map;
	private final SpriteBatch spriteBatch;
	private final FitViewport viewport;
	private final OrthographicCamera camera;
	private final Character characterSprite;

	/**
	 * Creates a new level screen for the given map, sets up rendering, viewport, camera, and spawns the character
	 * at the map's start position.
	 *
	 * @param game game instance used for navigation between screens
	 * @param map  loaded map defining world size, background, walls, and start position
	 */
	public LevelScreen(DungeonGame game, Map map) {
		this.game = game;
		this.map = map;

		spriteBatch = new SpriteBatch();
		viewport = new FitViewport(map.getWidth(), map.getHeight());
		camera = new OrthographicCamera(80, 50);
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		characterSprite = new Character(map.getWalls(), "character.png", 5, 5);
		characterSprite.setPosition(map.getStartPosX(), map.getStartPosY());

		camera.update();
	}

	/**
	 * Updates viewport and camera after a window resize.
	 *
	 * @param width  new backbuffer width in pixels
	 * @param height new backbuffer height in pixels
	 */
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		camera.update();
	}

	/**
	 * Per-frame update/render loop: applies viewport, updates camera matrices, updates character input/rotation,
	 * moves camera, applies gameplay logic, then draws the frame.
	 *
	 * @param delta time since last frame (seconds), provided by LibGDX
	 */
	@Override
	public void render(float delta) {
		viewport.apply();
		camera.update();
		spriteBatch.setProjectionMatrix(camera.combined);

		characterSprite.run(camera);

		moveCamera();
		logic();
		draw();
	}

	/**
	 * Positions the camera to follow the character.
	 *
	 * <p>Note: this does not clamp the camera to world bounds.</p>
	 */
	private void moveCamera() {
		camera.position.set(characterSprite.getX(), characterSprite.getY(), 0);
	}

	/**
	 * Applies gameplay/world constraints.
	 *
	 * <p>Currently clamps the character position to the world rectangle defined by the viewport's world size.</p>
	 */
	private void logic() {
		float worldWidth = viewport.getWorldWidth();
		float worldHeight = viewport.getWorldHeight();
		float characterWidth = characterSprite.getWidth();
		float characterHeight = characterSprite.getHeight();

		characterSprite.setX(MathUtils.clamp(characterSprite.getX(), 0, worldWidth - characterWidth));
		characterSprite.setY(MathUtils.clamp(characterSprite.getY(), 0, worldHeight - characterHeight));
	}

	/**
	 * Clears the screen and renders the map background, character sprite, and walls.
	 */
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

	/**
	 * Disposes GPU resources owned/used by this screen (batch, character texture, map textures).
	 *
	 * <p>Assumes the {@link Map} owns its background texture and each {@link Wall} owns its texture.</p>
	 */
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
