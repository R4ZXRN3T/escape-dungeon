package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Minimal placeholder settings screen.
 *
 * <p>Press ESC to go back to the menu.</p>
 */
public class SettingsScreen extends ScreenAdapter {
	private final DungeonGame game;

	public SettingsScreen(DungeonGame game) {
		this.game = game;
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1f);

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			game.openMenu();
		}
	}
}

