package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

import org.lasarimanstudios.escapedungeon.DungeonGame;

public class WinScreen extends ScreenAdapter {

	private final DungeonGame game;

	public WinScreen(DungeonGame game) {
		this.game = game;
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.DARK_GRAY);

		if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
			game.setScreen(new MenuScreen(game, null));
		}
	}
}
