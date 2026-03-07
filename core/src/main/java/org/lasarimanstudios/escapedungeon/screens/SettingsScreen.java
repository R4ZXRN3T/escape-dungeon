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
	// Make checkboxes bigger without needing bigger source textures.
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

	/**
	 * Draft key bindings (source of truth; UI text is presentation only).
	 */
	private int forwardKeyCode;
	private int backwardKeyCode;
	private int leftKeyCode;
	private int rightKeyCode;

	/**
	 * ATTACK binding encoding:
	 * - >= 0: keyboard keycode
	 * - < 0 : mouse button encoded as -(button + 1)
	 */
	private int attackBindCode;

	private KeyBindingTarget captureTarget;

	public SettingsScreen(DungeonGame game) {
		this.game = game;
	}

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

	private static BitmapFont createTitleFontFromTtf() {
		return createFontFromTtf(100);
	}

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

	private static int parseIntSafe(String raw, int fallback) {
		try {
			return Integer.parseInt(raw);
		} catch (Exception ignored) {
			return fallback;
		}
	}

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

	private static boolean isMouseEncoded(int code) {
		return code < 0;
	}

	private static int encodeMouseButton(int button) {
		return -(button + 1);
	}

	private static int decodeMouseButton(int code) {
		return (-code) - 1;
	}

	private static String attackBindName(int code) {
		if (isMouseEncoded(code)) return mouseButtonName(decodeMouseButton(code));
		return Input.Keys.toString(code);
	}

	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());

		// Match the MenuScreen visuals.
		buttonBackground = new Texture(Gdx.files.internal("ui/buttons/button_background.png"));
		buttonBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		Texture checboxEmptyTexture = new Texture(Gdx.files.internal("ui/buttons/checkbox_empty.png"));
		checboxEmptyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		Texture checkboxCheckedTexture = new Texture(Gdx.files.internal("ui/buttons/checkbox_filled.png"));
		checkboxCheckedTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		// Keep these in the same ballpark as MenuScreen so scaling feels consistent.
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

		Label.LabelStyle labelStyle = new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE);
		skin.add("default", labelStyle);
		Label.LabelStyle titleLabelStyle = new Label.LabelStyle(titleFont, com.badlogic.gdx.graphics.Color.WHITE);
		skin.add("title", titleLabelStyle);

		TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
		textFieldStyle.font = font;
		textFieldStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
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
		// Use the dedicated checkbox textures.
		TextureRegionDrawable off = new TextureRegionDrawable(new TextureRegion(checboxEmptyTexture));
		TextureRegionDrawable on = new TextureRegionDrawable(new TextureRegion(checkboxCheckedTexture));
		// Prevent Scene2D from scaling these unexpectedly (they sit inside a 62px row).
		// Also scale them up a bit so they're easier to read.
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
		// IMPORTANT: don't set checkBoxStyle.checked here. 'checked' is an extra overlay drawable,
		// so assigning it to the same drawable as checkboxOn draws the texture twice when selected.
		skin.add("default", checkBoxStyle);

		SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
		selectBoxStyle.font = font;
		selectBoxStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
		selectBoxStyle.background = new TextureRegionDrawable(new TextureRegion(buttonBackground));
		selectBoxStyle.scrollStyle = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle();
		selectBoxStyle.listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(font, com.badlogic.gdx.graphics.Color.WHITE,
			com.badlogic.gdx.graphics.Color.WHITE,
			new TextureRegionDrawable(new TextureRegion(buttonBackground)));
		skin.add("default", selectBoxStyle);

		buildUi();
		loadDraftFromConfig();

		// Input routing: stage first; if not handled, our adapter handles ESC and key capture.
		multiplexer = new com.badlogic.gdx.InputMultiplexer(stage, new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				Actor focus = stage.getKeyboardFocus();
				boolean textFieldFocused = focus instanceof TextField;

				// If user is typing into a TextField, never intercept keys here.
				if (textFieldFocused) {
					return false;
				}

				if (captureTarget != null) {
					// Backspace / Escape cancels capture.
					if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.ESCAPE) {
						captureTarget = null;
						renderKeyBindingLabels();
						return true;
					}

					// Allow ATTACK to be a keyboard key, too.
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
				// Some platforms/backends may deliver backspace as a typed character ('\b').
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

		// Actions
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

		// Build video widgets
		windowModeSelect = new SelectBox<>(skin);
		windowModeSelect.setItems("WINDOWED", "BORDERLESS", "FULLSCREEN");

		maxFpsField = new TextField("", skin);
		maxFpsField.setMessageText("e.g. 60");
		maxFpsField.setTextFieldFilter((textField, c) ->
			Character.isDigit(c) || c == '\b'
		);
		maxFpsField.setAlignment(Align.center);
		maxFpsField.setTextFieldListener((textField, c) -> { /* keep for potential future validation */ });

		vSyncCheck = new CheckBox("", skin);
		showFpsCheck = new CheckBox("", skin);

		addLabeledRow(video, "WINDOW MODE", windowModeSelect);
		addLabeledRow(video, "MAX FPS", maxFpsField);
		addLabeledRow(video, "V-SYNC", vSyncCheck);
		addLabeledRow(video, "SHOW FPS", showFpsCheck);

		// Build controls widgets
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

		// Un-capture if user interacts with other UI
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

	private Table makeSectionTable(String titleText) {
		Table section = new Table();
		section.defaults().pad(8f);

		Label title = new Label(titleText, skin);
		title.setAlignment(Align.center);
		section.add(title).colspan(2).padBottom(10f).row();

		return section;
	}

	private void addLabeledRow(Table table, String label, Actor widget) {
		Label l = new Label(label, skin);
		l.setAlignment(Align.left);
		table.add(l).width(340).height(ROW_HEIGHT).left();
		// Right-align the widget inside the fixed-width cell.
		table.add(widget).width(FIELD_WIDTH).height(ROW_HEIGHT).right();
		table.row();
	}

	private TextButton makeKeyCaptureButton(String label, KeyBindingTarget target) {
		TextButton button = new TextButton(label, skin);
		button.getLabel().setAlignment(Align.center);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				captureTarget = target;
				// Ensure we're not typing into a TextField while trying to bind keys.
				if (stage != null) stage.setKeyboardFocus(null);
				renderKeyBindingLabels();
			}
		});
		return button;
	}

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

	private String formatKeyButtonText(String base, KeyBindingTarget target) {
		return target == captureTarget ? (base + " (PRESS…)") : base;
	}

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

		// Back-compat: old config stored a mouse button (e.g. 0 for LEFT). New encoding uses negative for mouse.
		int rawAttack = parseIntSafe(ConfigManager.getConfig(ConfigKey.ATTACK_KEY), Input.Buttons.LEFT);
		attackBindCode = (rawAttack >= 0 && rawAttack <= 20) ? encodeMouseButton(rawAttack) : rawAttack;

		renderKeyBindingLabels();
	}

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

	private void setCapturedAttackButton(int button) {
		attackBindCode = encodeMouseButton(button);
		captureTarget = null;
		renderKeyBindingLabels();
	}

	private void setCapturedAttackKey(int keycode) {
		attackBindCode = keycode;
		captureTarget = null;
		renderKeyBindingLabels();
	}

	private void applyAndSave() {
		// Video
		ConfigManager.setConfig(ConfigKey.WINDOW_MODE, String.valueOf(windowModeSelect.getSelectedIndex()));
		ConfigManager.setConfig(ConfigKey.MAX_FPS, sanitizeMaxFps(maxFpsField.getText()));
		ConfigManager.setConfig(ConfigKey.VSYNC, String.valueOf(vSyncCheck.isChecked()));
		ConfigManager.setConfig(ConfigKey.SHOW_FPS, String.valueOf(showFpsCheck.isChecked()));

		// Controls (write from draft state; never parse UI labels)
		ConfigManager.setConfig(ConfigKey.FORWARD_KEY, String.valueOf(forwardKeyCode));
		ConfigManager.setConfig(ConfigKey.BACKWARD_KEY, String.valueOf(backwardKeyCode));
		ConfigManager.setConfig(ConfigKey.LEFT_KEY, String.valueOf(leftKeyCode));
		ConfigManager.setConfig(ConfigKey.RIGHT_KEY, String.valueOf(rightKeyCode));
		ConfigManager.setConfig(ConfigKey.ATTACK_KEY, String.valueOf(attackBindCode));

		ConfigManager.saveConfig();
		game.applySettings();
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1f);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		if (stage != null) stage.getViewport().update(width, height, true);
	}

	@Override
	public void hide() {
		// Avoid leaking the input processor when switching screens.
		if (Gdx.input.getInputProcessor() == multiplexer) {
			Gdx.input.setInputProcessor(null);
		}
		multiplexer = null;
	}

	@Override
	public void dispose() {
		if (stage != null) stage.dispose();
		if (skin != null) skin.dispose();
		if (font != null) font.dispose();
		if (titleFont != null) titleFont.dispose();
		if (buttonBackground != null) buttonBackground.dispose();
		super.dispose();
	}

	private enum KeyBindingTarget {FORWARD, BACKWARD, LEFT, RIGHT, ATTACK}
}
