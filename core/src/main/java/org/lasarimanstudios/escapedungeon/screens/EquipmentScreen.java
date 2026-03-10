package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.lasarimanstudios.escapedungeon.DungeonGame;
import org.lasarimanstudios.escapedungeon.SaveManager;
import org.lasarimanstudios.escapedungeon.SaveManager.SaveKey;
import org.lasarimanstudios.escapedungeon.weapons.SwordType;

/**
 * Inventory screen that lists all swords.
 * The player can buy swords they don't own and equip swords they do own.
 */
public class EquipmentScreen extends ScreenAdapter {
	private static final int BUTTON_WIDTH = 580;
	private static final int BUTTON_HEIGHT = 96;

	private final DungeonGame game;
	private Stage stage;
	private Skin skin;
	private BitmapFont font;
	private BitmapFont titleFont;
	private Texture buttonBackground;

	public EquipmentScreen(DungeonGame game) {
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

	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		buttonBackground = new Texture(Gdx.files.internal("ui/buttons/button_background.png"));
		buttonBackground.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		titleFont = createFontFromTtf(100);
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

		rebuildUI();
	}

	private void rebuildUI() {
		stage.clear();

		Table root = new Table();
		root.setFillParent(true);
		root.pad(20);
		stage.addActor(root);

		int money = SaveManager.getInt(SaveKey.MONEY, 0, Integer.MAX_VALUE);
		String equippedName = SaveManager.get(SaveKey.EQUIPPED_SWORD);

		Label title = new Label("EQUIPMENT", skin, "title");
		title.setAlignment(Align.center);
		root.add(title).colspan(5).padBottom(10).row();

		Label moneyLabel = new Label("Money: " + money, skin);
		moneyLabel.setAlignment(Align.center);
		root.add(moneyLabel).colspan(5).padBottom(20).row();

		// Header row
		root.add(new Label("Sword", skin)).padRight(20).align(Align.left);
		root.add(new Label("Damage", skin)).padRight(20).align(Align.center);
		root.add(new Label("Speed", skin)).padRight(20).align(Align.center);
		root.add(new Label("Range", skin)).padRight(20).align(Align.center);
		root.add(new Label("", skin));
		root.row().padTop(5);

		for (SwordType sword : SwordType.values()) {
			SaveKey ownershipKey = SaveManager.getOwnershipKey(sword.name());
			boolean owned = ownershipKey != null && SaveManager.getBoolean(ownershipKey);
			boolean equipped = sword.name().equals(equippedName);

			// Name
			Label nameLabel = new Label(sword.displayName, skin);
			if (equipped) nameLabel.setColor(Color.GREEN);
			else if (owned) nameLabel.setColor(Color.WHITE);
			else nameLabel.setColor(Color.GRAY);
			root.add(nameLabel).padRight(20).align(Align.left);

			// Stats
			String dmgText = sword.damage == Float.MAX_VALUE ? "INF" : String.valueOf((int) sword.damage);
			root.add(new Label(dmgText, skin)).padRight(20).align(Align.center);
			root.add(new Label(String.format("%.2f", sword.speed), skin)).padRight(20).align(Align.center);
			root.add(new Label(String.format("%.1f", sword.range), skin)).padRight(20).align(Align.center);

			// Action button
			if (equipped) {
				Label equippedLabel = new Label("[EQUIPPED]", skin);
				equippedLabel.setColor(Color.GREEN);
				root.add(equippedLabel).align(Align.center);
			} else if (owned) {
				TextButton equipBtn = new TextButton("Equip", skin);
				equipBtn.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						SaveManager.set(SaveKey.EQUIPPED_SWORD, sword.name());
						SaveManager.save();
						rebuildUI();
					}
				});
				root.add(equipBtn).align(Align.center);
			} else {
				int swordPrice = sword.getPrice();
				TextButton buyBtn = new TextButton("Buy (" + swordPrice + ")", skin);
				int currentMoney = SaveManager.getInt(SaveKey.MONEY, 0, Integer.MAX_VALUE);
				if (currentMoney < swordPrice) {
					buyBtn.setDisabled(true);
					buyBtn.setColor(Color.DARK_GRAY);
				}
				buyBtn.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						if (buyBtn.isDisabled()) return;
						int price = sword.getPrice();
						int m = SaveManager.getInt(SaveKey.MONEY, 0, Integer.MAX_VALUE);
						if (m < price) return;
						SaveKey key = SaveManager.getOwnershipKey(sword.name());
						if (key == null) return;
						SaveManager.set(SaveKey.MONEY, String.valueOf(m - price));
						SaveManager.set(key, "true");
						SaveManager.save();
						rebuildUI();
					}
				});
				root.add(buyBtn).align(Align.center);
			}

			root.row().padTop(8);
		}

		// Back button
		root.row().padTop(30);
		TextButton backBtn = new TextButton("BACK", skin);
		backBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.openMenu();
			}
		});
		root.add(backBtn).colspan(5).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).align(Align.center);
	}

	@Override
	public void render(float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			game.openMenu();
			return;
		}
		ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() == stage) {
			Gdx.input.setInputProcessor(null);
		}
	}

	@Override
	public void dispose() {
		if (stage != null) stage.dispose();
		if (skin != null) skin.dispose();
		if (font != null) font.dispose();
		if (titleFont != null) titleFont.dispose();
		if (buttonBackground != null) buttonBackground.dispose();
	}
}
