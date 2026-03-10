package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Main menu screen implemented with LibGDX Scene2D.
 *
 * <p>Creates a {@link Stage} with a table layout and several buttons to navigate to other screens.
 * GPU/UI resources are created in {@link #show()} and disposed in {@link #dispose()}.</p>
 */
public class MenuScreen extends ScreenAdapter {
	private static final int BUTTON_WIDTH = 580;
	private static final int BUTTON_HEIGHT = 96;
	private final DungeonGame game;
	private final Screen previousScreen;

	private Stage stage;
	private Skin skin;
	private BitmapFont font;
	private Texture buttonBackground;
	private Texture logoTexture;

	/**
	 * Creates the menu screen.
	 *
	 * @param game           game instance used to open other screens
	 * @param previousScreen previous screen to return to (nullable; when non-null a RESUME button is shown)
	 */
	public MenuScreen(DungeonGame game, Screen previousScreen) {
		this.game = game;
		this.previousScreen = previousScreen;
	}

	/**
	 * Generates a {@link BitmapFont} from the bundled TTF file at a fixed size.
	 *
	 * @return the generated bitmap font
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

	/**
	 * Initializes the Scene2D stage, skin, fonts, logo, and builds the button layout.
	 *
	 * <p>When {@code previousScreen} is non-null, a RESUME and RETURN TO MENU button are shown.
	 * Otherwise EQUIPMENT, SETTINGS, and EXIT buttons are displayed.</p>
	 */
	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		buttonBackground = new Texture(Gdx.files.internal("ui/buttons/button_background.png"));
		buttonBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		font = createFontFromTtf();

		skin = new Skin();
		skin.add("default-font", font);

		TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
		buttonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		buttonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		buttonStyle.over = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		buttonStyle.font = font;
		skin.add("default", buttonStyle);

		Table root = new Table();
		root.setFillParent(true);
		root.center();
		root.defaults().pad(10f);
		stage.addActor(root);

		logoTexture = new Texture(Gdx.files.internal("ui/logo.png"));
		logoTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		Image logoImage = new Image(logoTexture);
		root.add(logoImage).width(BUTTON_WIDTH).height(150).padBottom(20).row();

		if (previousScreen != null) {
			root.add(makeTextOnlyButton("RESUME", () -> game.setScreen(previousScreen)))
				.width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		}
		root.add(makeTextOnlyButton("NEW GAME", () -> game.openLevel("map_02"))).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		if (previousScreen != null) {
			root.add(makeTextOnlyButton("RETURN TO MENU", () -> game.setScreen(new MenuScreen(game, null))))
				.width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		}
		if (previousScreen == null) {
			root.add(makeTextOnlyButton("EQUIPMENT", game::openInventory)).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
			root.add(makeTextOnlyButton("SETTINGS", game::openSettings)).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
			root.add(makeTextOnlyButton("EXIT", () -> Gdx.app.exit())).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		}
	}

	/**
	 * Creates a {@link TextButton} with the given label that executes a callback on click.
	 *
	 * @param text    button label
	 * @param onClick action to run when the button is clicked
	 * @return the configured text button
	 */
	private TextButton makeTextOnlyButton(String text, Runnable onClick) {
		TextButton button = new TextButton(text, skin);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onClick.run();
			}
		});
		return button;
	}

	/**
	 * Clears the screen and draws the menu UI.
	 *
	 * @param delta time in seconds since the last frame
	 */
	@Override
	public void render(float delta) {
		ScreenUtils.clear(99f / 255f, 99f / 255f, 99f / 255f, 1f);

		stage.act(delta);
		stage.draw();
	}

	/**
	 * Updates the stage viewport when the window is resized.
	 *
	 * @param width  new width in pixels
	 * @param height new height in pixels
	 */
	@Override
	public void resize(int width, int height) {
		if (stage != null) {
			stage.getViewport().update(width, height, true);
		}
	}

	/**
	 * Clears the input processor when the screen is hidden to prevent input leaking.
	 */
	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() == stage) {
			Gdx.input.setInputProcessor(null);
		}
	}

	/**
	 * Disposes of GPU and UI resources owned by this screen.
	 */
	@Override
	public void dispose() {
		if (stage != null) stage.dispose();
		if (skin != null) skin.dispose();
		if (font != null) font.dispose();
		if (buttonBackground != null) buttonBackground.dispose();
		if (logoTexture != null) logoTexture.dispose();
	}
}
