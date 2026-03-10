package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;

import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Win screen displayed when the player wins.
 *
 * <p>Draws a win image, the earned money and a "Press ENTER to continue" prompt using
 * the same TTF/font generation settings as {@link MenuScreen}.</p>
 */
public class WinScreen extends ScreenAdapter {

	private final DungeonGame game;
	private final int earnedMoney;

	private SpriteBatch batch;
	private Texture winTexture;
	private BitmapFont font;
	private GlyphLayout layout;

	/**
	 * Create a WinScreen.
	 *
	 * @param game the game instance used to change screens
	 * @param earnedMoney amount earned by the player to display
	 */
	public WinScreen(DungeonGame game, int earnedMoney) {
		this.game = game;
		this.earnedMoney = earnedMoney;
	}

	/**
	 * Generates a BitmapFont using the bundled TTF with the same parameters used by MenuScreen.
	 */
	private static BitmapFont createFontFromTtf() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
		try {
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameter.size = 100;
			parameter.magFilter = Texture.TextureFilter.Nearest;
			parameter.minFilter = Texture.TextureFilter.Nearest;
			parameter.incremental = true;
			return generator.generateFont(parameter);
		} finally {
			generator.dispose();
		}
	}

	@Override
	public void show() {
		batch = new SpriteBatch();

		winTexture = new Texture(Gdx.files.internal("ui/win_screen.png"));
		winTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		font = createFontFromTtf();
		layout = new GlyphLayout();
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.DARK_GRAY);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		batch.begin();

		// Draw the win image centered near the top half of the screen and scaled to fit width
		float imgW = winTexture.getWidth();
		float imgH = winTexture.getHeight();
		float targetImgW = imgW * 10f;
		float scale = targetImgW / imgW;
		float targetImgH = imgH * scale;
		float imgX = (w - targetImgW) / 2f;
		float imgY = h * 0.55f;
		batch.draw(winTexture, imgX, imgY, targetImgW, targetImgH);

		// Draw the earned money text centered below the image
		font.setColor(Color.WHITE);
		font.getData().setScale(0.6f);
		String moneyText = "You earned: " + earnedMoney;
		layout.setText(font, moneyText);
		float moneyX = (w - layout.width) / 2f;
		float moneyY = imgY - 30f; // some spacing below the image
		font.draw(batch, layout, moneyX, moneyY);

		// Draw the prompt text smaller and centered under the money text
		font.getData().setScale(0.45f);
		String prompt = "Press ENTER to continue";
		layout.setText(font, prompt);
		float promptX = (w - layout.width) / 2f;
		float promptY = moneyY - 40f;
		font.setColor(Color.LIGHT_GRAY);
		font.draw(batch, layout, promptX, promptY);

		batch.end();

		// Handle input to return to the menu
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			game.setScreen(new MenuScreen(game, null));
		}
	}

	@Override
	public void resize(int width, int height) {
		// nothing to do for now; render uses current Gdx.graphics size
	}

	@Override
	public void hide() {
		// nothing special
	}

	@Override
	public void dispose() {
		if (batch != null) batch.dispose();
		if (winTexture != null) winTexture.dispose();
		if (font != null) font.dispose();
		// layout does not require disposal
	}
}
