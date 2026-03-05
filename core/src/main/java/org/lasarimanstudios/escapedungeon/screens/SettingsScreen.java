package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Simple settings screen placeholder.
 *
 * <p>Currently only clears the screen and returns to the menu when the user presses ESC.</p>
 */
public class SettingsScreen extends ScreenAdapter {
	private final DungeonGame game;

	/**
	 * Creates the settings screen.
	 *
	 * @param game game instance used to navigate back to the menu
	 */
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

