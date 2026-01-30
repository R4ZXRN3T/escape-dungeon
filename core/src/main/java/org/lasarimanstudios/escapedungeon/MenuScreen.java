package org.lasarimanstudios.escapedungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen extends ScreenAdapter {
	private final DungeonGame game;

	public MenuScreen(DungeonGame game) {
		this.game = game;
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0f, 0f, 0f, 1f);
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			game.openLevel("map_01");
		}
	}
}
