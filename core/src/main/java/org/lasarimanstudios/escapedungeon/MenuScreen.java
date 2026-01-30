package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Simple main menu screen.
 *
 * <p>Currently clears the screen and starts a hard-coded level when ENTER is pressed.</p>
 */
public class MenuScreen extends ScreenAdapter {
	private final DungeonGame game;

	/**
	 * Creates the menu screen.
	 *
	 * @param game game instance used to open levels/screens
	 */
	public MenuScreen(DungeonGame game) {
		this.game = game;
	}

	/**
	 * Renders the menu frame and handles input.
	 *
	 * @param delta time since last frame (seconds), provided by LibGDX
	 */
	@Override
	public void render(float delta) {
		ScreenUtils.clear(0f, 0f, 0f, 1f);
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			game.openLevel("map_01");
		}
	}
}
