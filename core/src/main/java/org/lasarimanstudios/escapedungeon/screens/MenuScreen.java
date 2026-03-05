package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Main menu screen using LibGDX Scene2D UI.
 */
public class MenuScreen extends ScreenAdapter {
	private static final int BUTTON_WIDTH = 580;
	private static final int BUTTON_HEIGHT = 96;
	private final DungeonGame game;
	private Stage stage;
	private Skin skin;
	private BitmapFont font;
	private Texture buttonBackground;

	/**
	 * Creates the menu screen.
	 *
	 * @param game game instance used to open levels/screens
	 */
	public MenuScreen(DungeonGame game) {
		this.game = game;
	}

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
		root.defaults().pad(10f);
		stage.addActor(root);

		root.add(makeTextOnlyButton("PLAY", () -> game.openLevel("map_01"))).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		root.add(makeTextOnlyButton("INVENTORY", game::openInventory)).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		root.add(makeTextOnlyButton("SETTINGS", game::openSettings)).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
		root.add(makeTextOnlyButton("EXIT", () -> Gdx.app.exit())).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
	}

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

	@Override
	public void render(float delta) {
		ScreenUtils.clear(99f / 255f, 99f / 255f, 99f / 255f, 1f);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		if (stage != null) {
			stage.getViewport().update(width, height, true);
		}
	}

	@Override
	public void hide() {
		// Avoid leaking the input processor when switching screens.
		if (Gdx.input.getInputProcessor() == stage) {
			Gdx.input.setInputProcessor(null);
		}
	}

	@Override
	public void dispose() {
		if (stage != null) stage.dispose();
		if (skin != null) skin.dispose();
		if (font != null) font.dispose();
		if (buttonBackground != null) buttonBackground.dispose();
	}
}
