package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

import org.lasarimanstudios.escapedungeon.DungeonGame;

/**
 * Placeholder screen displayed when the player wins (all enemies defeated).
 *
 * <p>Shows a dark gray background and transitions to the {@link MenuScreen} when the
 * player presses {@code ENTER}.</p>
 */
public class WinScreen extends ScreenAdapter {

	private final DungeonGame game;

	/**
	 * Creates a new win screen.
	 *
	 * @param game the game instance used to change screens
	 */
	public WinScreen(DungeonGame game) {
		this.game = game;
	}

	/**
	 * Clears the screen and waits for the player to press {@code ENTER} to return to the menu.
	 *
	 * @param delta time in seconds since the last frame
	 */
	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.DARK_GRAY);

		if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
			game.setScreen(new MenuScreen(game, null));
		}
	}
}
