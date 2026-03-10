package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.lasarimanstudios.escapedungeon.ConfigManager;
import org.lasarimanstudios.escapedungeon.ConfigManager.ConfigKey;
import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Settings screen implemented with LibGDX Scene2D.
 *
 * <p>Uses {@link ConfigManager} as persistence backend. The UI edits a draft state; changes are
 * written to disk only when the user presses "APPLY".</p>
 */
public class SettingsScreen extends ScreenAdapter {
	private static final int BUTTON_WIDTH = 580;
	private static final int BUTTON_HEIGHT = 96;
	private static final int FIELD_WIDTH = 360;
	private static final int ROW_HEIGHT = 62;
	private static final float CHECKBOX_SCALE = 1.6f;

	private final DungeonGame game;

	private Stage stage;
	private Skin skin;
	private BitmapFont font;
	private BitmapFont titleFont;
	private Texture buttonBackground;

	private com.badlogic.gdx.InputMultiplexer multiplexer;

	// Video
	private SelectBox<String> windowModeSelect;
	private TextField maxFpsField;
	private CheckBox vSyncCheck;
	private CheckBox showFpsCheck;

	// Controls
	private TextButton forwardKeyBtn;
	private TextButton backwardKeyBtn;
	private TextButton leftKeyBtn;
	private TextButton rightKeyBtn;
	private TextButton attackKeyBtn;

	private int forwardKeyCode;
	private int backwardKeyCode;
	private int leftKeyCode;
	private int rightKeyCode;

	/**
	 * Draft attack binding code.
	 *
	 * <p>Encoding: {@code >= 0} means a keyboard keycode; {@code < 0} means a mouse button
	 * encoded as {@code -(button + 1)}.</p>
	 */
	private int attackBindCode;

	/**
	 * The key binding target currently awaiting a key press, or {@code null} if none.
	 */
	private KeyBindingTarget captureTarget;

	/**
	 * Creates a new settings screen.
	 *
	 * @param game the game instance used to navigate between screens and apply settings
	 */
	public SettingsScreen(DungeonGame game) {
		this.game = game;
	}

	/**
	 * Generates a {@link BitmapFont} from the bundled TTF file at the specified size.
	 *
	 * @param size desired font size in pixels
	 * @return the generated bitmap font
	 */
	private static BitmapFont createFontFromTtf(int size) {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/font.ttf"));
		try {
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameter.size = size;
			parameter.magFilter = Texture.TextureFilter.Nearest;
			parameter.minFilter = Texture.TextureFilter.Nearest;
			parameter.incremental = false;
			parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
			return generator.generateFont(parameter);
		} finally {
			generator.dispose();
		}
	}

	/**
	 * Generates a title-sized {@link BitmapFont} from the bundled TTF file.
	 *
	 * @return the generated title font
	 */
	private static BitmapFont createTitleFontFromTtf() {
		return createFontFromTtf(100);
	}

	/**
	 * Sanitizes raw text input for the max FPS field, clamping to {@code [0, 9999]}.
	 *
	 * @param txt raw text input (may be {@code null} or non-numeric)
	 * @return a valid FPS string (defaults to {@code "60"} on invalid input)
	 */
	private static String sanitizeMaxFps(String txt) {
		if (txt == null || txt.isBlank()) return "60";
		try {
			int v = Integer.parseInt(txt);
			if (v < 0) v = 0;
			if (v > 9999) v = 9999;
			return String.valueOf(v);
		} catch (Exception ignored) {
			return "60";
		}
	}

	/**
	 * Parses an integer from a string, returning a fallback value on failure.
	 *
	 * @param raw      the string to parse
	 * @param fallback value returned when parsing fails
	 * @return the parsed integer or the fallback
	 */
	private static int parseIntSafe(String raw, int fallback) {
		try {
			return Integer.parseInt(raw);
		} catch (Exception ignored) {
			return fallback;
		}
	}

	/**
	 * Returns a human-readable name for a mouse button constant.
	 *
	 * @param button one of the {@link Input.Buttons} constants
	 * @return display name (e.g. {@code "MOUSE LEFT"})
	 */
	private static String mouseButtonName(int button) {
		return switch (button) {
			case Input.Buttons.LEFT -> "MOUSE LEFT";
			case Input.Buttons.RIGHT -> "MOUSE RIGHT";
			case Input.Buttons.MIDDLE -> "MOUSE MIDDLE";
			case Input.Buttons.BACK -> "MOUSE BACK";
			case Input.Buttons.FORWARD -> "MOUSE FORWARD";
			default -> "MOUSE " + button;
		};
	}

	/**
	 * Returns {@code true} if the code represents an encoded mouse button (negative value).
	 *
	 * @param code the binding code
	 * @return {@code true} if mouse-encoded
	 */
	private static boolean isMouseEncoded(int code) {
		return code < 0;
	}

	/**
	 * Encodes a mouse button constant as a negative integer for storage.
	 *
	 * @param button one of the {@link Input.Buttons} constants
	 * @return the encoded value ({@code -(button + 1)})
	 */
	private static int encodeMouseButton(int button) {
		return -(button + 1);
	}

	/**
	 * Decodes a mouse button constant from an encoded negative integer.
	 *
	 * @param code the encoded value
	 * @return the original {@link Input.Buttons} constant
	 */
	private static int decodeMouseButton(int code) {
		return (-code) - 1;
	}

	/**
	 * Returns a human-readable name for an attack binding code (keyboard key or mouse button).
	 *
	 * @param code the binding code (keyboard keycode or encoded mouse button)
	 * @return display name
	 */
	private static String attackBindName(int code) {
		if (isMouseEncoded(code)) return mouseButtonName(decodeMouseButton(code));
		return Input.Keys.toString(code);
	}

	/**
	 * Initializes the Scene2D stage, skin, fonts, and builds the settings UI.
	 * Sets up an input multiplexer that routes events to the stage first, then handles
	 * key-binding capture and ESC.
	 */
	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());

		buttonBackground = new Texture(Gdx.files.internal("ui/buttons/button_background.png"));
		buttonBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		Texture checboxEmptyTexture = new Texture(Gdx.files.internal("ui/buttons/checkbox_empty.png"));
		checboxEmptyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		Texture checkboxCheckedTexture = new Texture(Gdx.files.internal("ui/buttons/checkbox_filled.png"));
		checkboxCheckedTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		titleFont = createTitleFontFromTtf();
		font = createFontFromTtf(48);

		skin = new Skin();
		skin.add("default-font", font);
		skin.add("title-font", titleFont);

		TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
		buttonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		buttonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		buttonStyle.over = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		buttonStyle.font = font;
		skin.add("default", buttonStyle);

		Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
		skin.add("default", labelStyle);
		Label.LabelStyle titleLabelStyle = new Label.LabelStyle(titleFont, Color.WHITE);
		skin.add("title", titleLabelStyle);

		TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
		textFieldStyle.font = font;
		textFieldStyle.fontColor = Color.WHITE;
		Pixmap pixmap = new Pixmap(2, (int) font.getLineHeight(), Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		Texture cursorTex = new Texture(pixmap);
		pixmap.dispose();

		textFieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(cursorTex));
		textFieldStyle.selection = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		textFieldStyle.background = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		skin.add("default", textFieldStyle);

		CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
		checkBoxStyle.font = font;
		TextureRegionDrawable off = new TextureRegionDrawable(new TextureRegion(checboxEmptyTexture));
		TextureRegionDrawable on = new TextureRegionDrawable(new TextureRegion(checkboxCheckedTexture));
		float offW = checboxEmptyTexture.getWidth() * CHECKBOX_SCALE;
		float offH = checboxEmptyTexture.getHeight() * CHECKBOX_SCALE;
		float onW = checkboxCheckedTexture.getWidth() * CHECKBOX_SCALE;
		float onH = checkboxCheckedTexture.getHeight() * CHECKBOX_SCALE;
		off.setMinWidth(offW);
		off.setMinHeight(offH);
		on.setMinWidth(onW);
		on.setMinHeight(onH);
		checkBoxStyle.checkboxOff = off;
		checkBoxStyle.checkboxOn = on;
		skin.add("default", checkBoxStyle);

		SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
		selectBoxStyle.font = font;
		selectBoxStyle.fontColor = Color.WHITE;
		selectBoxStyle.background = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		selectBoxStyle.scrollStyle = new ScrollPane.ScrollPaneStyle();
		selectBoxStyle.listStyle = new List.ListStyle(font, Color.WHITE,
			Color.WHITE,
			new TextureRegionDrawable(new TextureRegion(buttonBackground)));
		skin.add("default", selectBoxStyle);

		buildUi();
		loadDraftFromConfig();

		multiplexer = new com.badlogic.gdx.InputMultiplexer(stage, new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				Actor focus = stage.getKeyboardFocus();
				boolean textFieldFocused = focus instanceof TextField;

				if (textFieldFocused) {
					return false;
				}

				if (captureTarget != null) {
					if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.ESCAPE) {
						captureTarget = null;
						renderKeyBindingLabels();
						return true;
					}

					if (captureTarget == KeyBindingTarget.ATTACK) {
						setCapturedAttackKey(keycode);
					} else {
						setCapturedKey(keycode);
					}
					return true;
				}

				if (keycode == Input.Keys.ESCAPE) {
					game.openMenu();
					return true;
				}
				return false;
			}

			@Override
			public boolean keyTyped(char character) {
				Actor focus = stage.getKeyboardFocus();
				boolean textFieldFocused = focus instanceof TextField;
				if (textFieldFocused) return false;

				if (captureTarget != null && (character == '\b' || character == 127)) {
					captureTarget = null;
					renderKeyBindingLabels();
					return true;
				}
				return false;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (captureTarget == KeyBindingTarget.ATTACK) {
					setCapturedAttackButton(button);
					return true;
				}
				return false;
			}
		});
		Gdx.input.setInputProcessor(multiplexer);
	}

	/**
	 * Builds the settings UI layout with video and controls sections,
	 * plus APPLY and BACK action buttons.
	 */
	private void buildUi() {
		Table root = new Table();
		root.setFillParent(true);
		root.defaults().pad(10f);
		stage.addActor(root);

		Label title = new Label("SETTINGS", skin, "title");
		title.setAlignment(Align.center);
		root.add(title).padBottom(20f).row();

		Table content = new Table();
		content.defaults().pad(10f);
		root.add(content).row();

		Table video = makeSectionTable("VIDEO");
		Table controls = makeSectionTable("CONTROLS");
		content.add(video).padRight(20f).top();
		content.add(controls).top();
		content.row();

		Table actions = new Table();
		actions.defaults().pad(10f);
		root.add(actions).padTop(20f).row();

		TextButton apply = new TextButton("APPLY", skin);
		apply.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				applyAndSave();
			}
		});

		TextButton back = new TextButton("BACK", skin);
		back.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.openMenu();
			}
		});

		actions.add(apply).width(BUTTON_WIDTH / 2f).height(BUTTON_HEIGHT);
		actions.add(back).width(BUTTON_WIDTH / 2f).height(BUTTON_HEIGHT);

		windowModeSelect = new SelectBox<>(skin);
		windowModeSelect.setItems("WINDOWED", "BORDERLESS", "FULLSCREEN");

		maxFpsField = new TextField("", skin);
		maxFpsField.setMessageText("e.g. 60");
		maxFpsField.setTextFieldFilter((textField, c) ->
			Character.isDigit(c) || c == '\b'
		);
		maxFpsField.setAlignment(Align.center);

		vSyncCheck = new CheckBox("", skin);
		showFpsCheck = new CheckBox("", skin);

		addLabeledRow(video, "WINDOW MODE", windowModeSelect);
		addLabeledRow(video, "MAX FPS", maxFpsField);
		addLabeledRow(video, "V-SYNC", vSyncCheck);
		addLabeledRow(video, "SHOW FPS", showFpsCheck);

		forwardKeyBtn = makeKeyCaptureButton("FORWARD", KeyBindingTarget.FORWARD);
		backwardKeyBtn = makeKeyCaptureButton("BACKWARD", KeyBindingTarget.BACKWARD);
		leftKeyBtn = makeKeyCaptureButton("LEFT", KeyBindingTarget.LEFT);
		rightKeyBtn = makeKeyCaptureButton("RIGHT", KeyBindingTarget.RIGHT);
		attackKeyBtn = makeKeyCaptureButton("ATTACK", KeyBindingTarget.ATTACK);

		addLabeledRow(controls, "FORWARD", forwardKeyBtn);
		addLabeledRow(controls, "BACKWARD", backwardKeyBtn);
		addLabeledRow(controls, "LEFT", leftKeyBtn);
		addLabeledRow(controls, "RIGHT", rightKeyBtn);
		addLabeledRow(controls, "ATTACK", attackKeyBtn);

		stage.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (actor != forwardKeyBtn && actor != backwardKeyBtn && actor != leftKeyBtn && actor != rightKeyBtn && actor != attackKeyBtn) {
					captureTarget = null;
					renderKeyBindingLabels();
				}
			}
		});
	}

	/**
	 * Creates a labeled section table with a centered title.
	 *
	 * @param titleText the section title
	 * @return the configured table
	 */
	private Table makeSectionTable(String titleText) {
		Table section = new Table();
		section.defaults().pad(8f);

		Label title = new Label(titleText, skin);
		title.setAlignment(Align.center);
		section.add(title).colspan(2).padBottom(10f).row();

		return section;
	}

	/**
	 * Adds a label-widget row to a section table.
	 *
	 * @param table  the section table
	 * @param label  the row label text
	 * @param widget the widget to place in the right column
	 */
	private void addLabeledRow(Table table, String label, Actor widget) {
		Label l = new Label(label, skin);
		l.setAlignment(Align.left);
		table.add(l).width(340).height(ROW_HEIGHT).left();
		table.add(widget).width(FIELD_WIDTH).height(ROW_HEIGHT).right();
		table.row();
	}

	/**
	 * Creates a button that, when clicked, enters key-capture mode for the given binding target.
	 *
	 * @param label  initial button label
	 * @param target the key binding target to capture
	 * @return the configured text button
	 */
	private TextButton makeKeyCaptureButton(String label, KeyBindingTarget target) {
		TextButton button = new TextButton(label, skin);
		button.getLabel().setAlignment(Align.center);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				captureTarget = target;
				if (stage != null) stage.setKeyboardFocus(null);
				renderKeyBindingLabels();
			}
		});
		return button;
	}

	/**
	 * Updates all key-binding button labels to reflect the current draft key codes
	 * and whether a capture is in progress.
	 */
	private void renderKeyBindingLabels() {
		if (forwardKeyBtn != null)
			forwardKeyBtn.setText(formatKeyButtonText(Input.Keys.toString(forwardKeyCode), KeyBindingTarget.FORWARD));
		if (backwardKeyBtn != null)
			backwardKeyBtn.setText(formatKeyButtonText(Input.Keys.toString(backwardKeyCode), KeyBindingTarget.BACKWARD));
		if (leftKeyBtn != null)
			leftKeyBtn.setText(formatKeyButtonText(Input.Keys.toString(leftKeyCode), KeyBindingTarget.LEFT));
		if (rightKeyBtn != null)
			rightKeyBtn.setText(formatKeyButtonText(Input.Keys.toString(rightKeyCode), KeyBindingTarget.RIGHT));
		if (attackKeyBtn != null)
			attackKeyBtn.setText(formatKeyButtonText(attackBindName(attackBindCode), KeyBindingTarget.ATTACK));
	}

	/**
	 * Formats a key-binding button's display text, appending a capture indicator when active.
	 *
	 * @param base   the key/button name to display
	 * @param target the binding target this label represents
	 * @return formatted label text
	 */
	private String formatKeyButtonText(String base, KeyBindingTarget target) {
		return target == captureTarget ? (base + " (PRESS…)") : base;
	}

	/**
	 * Loads the current configuration from {@link ConfigManager} into the draft UI state.
	 */
	private void loadDraftFromConfig() {
		int windowMode = ConfigManager.getInt(ConfigKey.WINDOW_MODE, 0, 2);
		windowModeSelect.setSelectedIndex(windowMode);

		maxFpsField.setText(String.valueOf(ConfigManager.getInt(ConfigKey.MAX_FPS, 0, 9999)));
		vSyncCheck.setChecked(ConfigManager.getBoolean(ConfigKey.VSYNC));
		showFpsCheck.setChecked(ConfigManager.getBoolean(ConfigKey.SHOW_FPS));

		forwardKeyCode = parseIntSafe(ConfigManager.getConfig(ConfigKey.FORWARD_KEY), Input.Keys.W);
		backwardKeyCode = parseIntSafe(ConfigManager.getConfig(ConfigKey.BACKWARD_KEY), Input.Keys.S);
		leftKeyCode = parseIntSafe(ConfigManager.getConfig(ConfigKey.LEFT_KEY), Input.Keys.A);
		rightKeyCode = parseIntSafe(ConfigManager.getConfig(ConfigKey.RIGHT_KEY), Input.Keys.D);

		int rawAttack = parseIntSafe(ConfigManager.getConfig(ConfigKey.ATTACK_KEY), Input.Buttons.LEFT);
		attackBindCode = (rawAttack >= 0 && rawAttack <= 20) ? encodeMouseButton(rawAttack) : rawAttack;

		renderKeyBindingLabels();
	}

	/**
	 * Assigns the captured keyboard keycode to the current binding target (movement keys).
	 *
	 * @param keycode the pressed keyboard keycode
	 */
	private void setCapturedKey(int keycode) {
		switch (captureTarget) {
			case FORWARD -> forwardKeyCode = keycode;
			case BACKWARD -> backwardKeyCode = keycode;
			case LEFT -> leftKeyCode = keycode;
			case RIGHT -> rightKeyCode = keycode;
			case ATTACK -> { /* mouse-only; ignore */ }
		}
		captureTarget = null;
		renderKeyBindingLabels();
	}

	/**
	 * Assigns the captured mouse button to the attack binding.
	 *
	 * @param button the pressed mouse button constant
	 */
	private void setCapturedAttackButton(int button) {
		attackBindCode = encodeMouseButton(button);
		captureTarget = null;
		renderKeyBindingLabels();
	}

	/**
	 * Assigns the captured keyboard keycode to the attack binding.
	 *
	 * @param keycode the pressed keyboard keycode
	 */
	private void setCapturedAttackKey(int keycode) {
		attackBindCode = keycode;
		captureTarget = null;
		renderKeyBindingLabels();
	}

	/**
	 * Writes the current draft state to {@link ConfigManager} and applies the settings.
	 */
	private void applyAndSave() {
		ConfigManager.setConfig(ConfigKey.WINDOW_MODE, String.valueOf(windowModeSelect.getSelectedIndex()));
		ConfigManager.setConfig(ConfigKey.MAX_FPS, sanitizeMaxFps(maxFpsField.getText()));
		ConfigManager.setConfig(ConfigKey.VSYNC, String.valueOf(vSyncCheck.isChecked()));
		ConfigManager.setConfig(ConfigKey.SHOW_FPS, String.valueOf(showFpsCheck.isChecked()));

		ConfigManager.setConfig(ConfigKey.FORWARD_KEY, String.valueOf(forwardKeyCode));
		ConfigManager.setConfig(ConfigKey.BACKWARD_KEY, String.valueOf(backwardKeyCode));
		ConfigManager.setConfig(ConfigKey.LEFT_KEY, String.valueOf(leftKeyCode));
		ConfigManager.setConfig(ConfigKey.RIGHT_KEY, String.valueOf(rightKeyCode));
		ConfigManager.setConfig(ConfigKey.ATTACK_KEY, String.valueOf(attackBindCode));

		ConfigManager.saveConfig();
		game.applySettings();
	}

	/**
	 * Clears the screen and draws the settings UI.
	 *
	 * @param delta time in seconds since the last frame
	 */
	@Override
	public void render(float delta) {
		ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1f);
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
		if (stage != null) stage.getViewport().update(width, height, true);
	}

	/**
	 * Clears the input processor when the screen is hidden to prevent input leaking.
	 */
	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() == multiplexer) {
			Gdx.input.setInputProcessor(null);
		}
		multiplexer = null;
	}

	/**
	 * Disposes of GPU and UI resources owned by this screen.
	 */
	@Override
	public void dispose() {
		if (stage != null) stage.dispose();
		if (skin != null) skin.dispose();
		if (font != null) font.dispose();
		if (titleFont != null) titleFont.dispose();
		if (buttonBackground != null) buttonBackground.dispose();
		super.dispose();
	}

	/**
	 * Identifiers for the key/button binding targets that can be captured.
	 */
	private enum KeyBindingTarget {
		FORWARD, BACKWARD, LEFT, RIGHT, ATTACK
	}
}
