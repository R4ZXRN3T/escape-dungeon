package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Displays an intro texture with a simple zoom animation and transitions to the MenuScreen
 * when the animation completes.
 */
public class IntroScreen extends ScreenAdapter {
	private final SpriteBatch batch;
	private final OrthographicCamera camera;
	private final Texture texture;
	private final Viewport viewport;
	private final DungeonGame game;

	private float elapsed = 0f;

	/**
	 * Creates a new IntroScreen.
	 *
	 * @param game the game instance used to change screens
	 */
	public IntroScreen(DungeonGame game) {
		this.game = game;
		batch = new SpriteBatch();
		camera = new OrthographicCamera();

		texture = new Texture(Gdx.files.internal("textures/ui/intro_screen.png"));

		viewport = new ScalingViewport(Scaling.fit, texture.getWidth(), texture.getHeight(), camera);
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		camera.position.set(texture.getWidth() / 2f, texture.getHeight() / 2f, 0);
	}

	/**
	 * Renders the intro with a zoom animation. After the animation duration elapses,
	 * the screen is switched to the MenuScreen.
	 *
	 * @param delta time in seconds since the last frame
	 */
	@Override
	public void render(float delta) {
		elapsed += delta;

		float duration = 3.0f;
		float progress = Math.min(elapsed / duration, 1f);

		float startZoom = 0.5f;
		float endZoom = 1.0f;
		camera.zoom = Interpolation.pow3Out.apply(startZoom, endZoom, progress);
		camera.update();

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(texture, 0, 0, texture.getWidth(), texture.getHeight());
		batch.end();

		if (progress >= 1f || Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
			game.setScreen(new MenuScreen(game));
		}
	}

	/**
	 * Updates the viewport when the screen size changes.
	 *
	 * @param width  new width in pixels
	 * @param height new height in pixels
	 */
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}

	/**
	 * Disposes of disposable resources used by this screen.
	 */
	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
	}
}
