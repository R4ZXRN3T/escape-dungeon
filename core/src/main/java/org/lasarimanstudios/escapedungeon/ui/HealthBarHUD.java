package org.lasarimanstudios.escapedungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import org.lasarimanstudios.escapedungeon.entities.Character;

/**
 * Draws a health bar HUD centered at the bottom of the screen.
 *
 * <p>Layout (left to right): heart icon → filled bar overlaying the empty bar background.
 * The whole group is horizontally centered on the screen.</p>
 */
public class HealthBarHUD implements Disposable {

	private static final float BAR_WIDTH = 384f;
	private static final float BAR_HEIGHT = 32f;
	private static final float HEART_SIZE = 32f;
	private static final float PADDING = 4f;
	private static final float BOTTOM_MARGIN = 20f;
	private static final float BAR_FILL_INSET_X = 4f;
	private static final float BAR_FILL_INSET_Y = 4f;

	private final Texture emptyBarTexture;
	private final Texture heartTexture;
	private final OrthographicCamera hudCamera;
	private final ShapeRenderer shapeRenderer;

	/**
	 * Creates the health bar HUD and loads its textures.
	 */
	public HealthBarHUD() {
		emptyBarTexture = new Texture(Gdx.files.internal("ui/health_bar/empty_bar.png"));
		heartTexture = new Texture(Gdx.files.internal("ui/health_bar/heart.png"));
		hudCamera = new OrthographicCamera();
		shapeRenderer = new ShapeRenderer();
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/**
	 * Updates the HUD camera projection to match the new screen dimensions.
	 * Call this from the screen's {@code resize()} method.
	 *
	 * @param screenWidth  new screen width in pixels
	 * @param screenHeight new screen height in pixels
	 */
	public void resize(int screenWidth, int screenHeight) {
		hudCamera.setToOrtho(false, screenWidth, screenHeight);
		hudCamera.update();
	}

	/**
	 * Renders the health bar. Must be called <b>outside</b> any active {@link SpriteBatch#begin()}.
	 *
	 * @param batch     the sprite batch to draw with (will be begun and ended internally)
	 * @param character the player character whose health to display
	 */
	public void render(SpriteBatch batch, Character character) {
		float screenWidth = hudCamera.viewportWidth;

		float groupWidth = HEART_SIZE + PADDING + BAR_WIDTH;
		float heartX = (screenWidth - groupWidth) / 2f;
		float barY = BOTTOM_MARGIN;

		float heartY = barY + (BAR_HEIGHT - HEART_SIZE) / 2f;
		float barX = heartX + HEART_SIZE + PADDING;

		float ratio = Math.max(0f, Math.min(1f, character.getRemainingHealth() / character.getMaxHealth()));

		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		batch.draw(emptyBarTexture, barX, barY, BAR_WIDTH, BAR_HEIGHT);
		batch.end();

		Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.setProjectionMatrix(hudCamera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Color barColor = new Color(1f - ratio, ratio, 0f, 1f);
		shapeRenderer.setColor(barColor);
		float fillX = barX + BAR_FILL_INSET_X;
		float fillY = barY + BAR_FILL_INSET_Y;
		float maxFillWidth = BAR_WIDTH - BAR_FILL_INSET_X * 2f;
		float fillHeight = BAR_HEIGHT - BAR_FILL_INSET_Y * 2f;
		shapeRenderer.rect(fillX, fillY, maxFillWidth * ratio, fillHeight);
		shapeRenderer.end();

		batch.begin();
		batch.draw(heartTexture, heartX, heartY, HEART_SIZE, HEART_SIZE);
		batch.end();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		emptyBarTexture.dispose();
		heartTexture.dispose();
		shapeRenderer.dispose();
	}
}
