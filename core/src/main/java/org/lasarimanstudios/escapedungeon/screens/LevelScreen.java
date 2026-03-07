package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.lasarimanstudios.escapedungeon.DungeonGame;
import org.lasarimanstudios.escapedungeon.GameAssets;
import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.level.Map;
import org.lasarimanstudios.escapedungeon.world.World;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Gameplay screen.
 *
 * <p>Owns the render loop for a {@link Map}: updates the player character, updates/enables the
 * {@link org.lasarimanstudios.escapedungeon.world.World} runtime state, updates enemies, and draws
 * the background and sprites using a {@link SpriteBatch}.</p>
 */
public class LevelScreen extends ScreenAdapter {
	/**
	 * Base world size used for gameplay rendering.
	 *
	 * <p>With an {@link ExtendViewport}, this is the minimum visible world size; on wider/taller windows
	 * the viewport will extend to fill the screen without stretching (it shows more world instead of
	 * adding black bars).</p>
	 */
	private static final float MIN_WORLD_WIDTH = 80f;
	private static final float MIN_WORLD_HEIGHT = 50f;
	private final DungeonGame game;
	private final Map map;
	private final SpriteBatch spriteBatch;
	private final Viewport viewport;
	private final OrthographicCamera camera;
	private final Character characterSprite;
	private final GameAssets assets;
	private final World world;
	private boolean deathHandled = false;

	public LevelScreen(DungeonGame game, Map map, GameAssets assets) {
		this.game = game;
		this.map = map;
		this.assets = assets;

		world = new World(map, assets);

		spriteBatch = new SpriteBatch();

		camera = new OrthographicCamera();
		viewport = new ExtendViewport(MIN_WORLD_WIDTH, MIN_WORLD_HEIGHT, camera);
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		Texture weaponTex = assets.getTexture(GameAssets.TEX_WEAPON_SWORD_1);
		characterSprite = new Character(map.getWalls(), map.getEnemies(), assets, weaponTex, 5, 5, 100);
		characterSprite.setPosition(map.getStartPosX(), map.getStartPosY());

		// Allow the World to wire dynamically spawned enemies to the player.
		world.setPlayerCharacter(characterSprite);

		// Ensure all map-loaded enemies are wired the same way as dynamically spawned ones.
		for (Enemy enemy : map.getEnemies()) {
			enemy.setCharacter(this.characterSprite);
			enemy.setDeathListener(world::onEnemyDied);
		}

		characterSprite.setDeathListener(this::onPlayerDied);

		camera.update();
	}

	private void onPlayerDied(Character character) {
		if (deathHandled) return;
		deathHandled = true;

		world.onPlayerDied(character);
		game.setScreen(new DeathScreen(game));
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		camera.update();
	}

	/**
	 * Per-frame update/render loop: applies viewport, updates camera matrices, updates character input/rotation,
	 * moves camera, applies gameplay logic, then draws the frame.
	 *
	 * @param delta time since last frame (seconds), provided by LibGDX
	 */
	@Override
	public void render(float delta) {
		viewport.apply();
		camera.update();
		spriteBatch.setProjectionMatrix(camera.combined);

		if (!deathHandled) {
			characterSprite.update(delta, camera);

			world.update(delta);
			for (Enemy enemy : map.getEnemies()) {
				enemy.update(delta);
			}

			logic();
			moveCamera();
		}

		draw();
	}

	/**
	 * Positions the camera to follow the character and clamps it to the map bounds.
	 */
	private void moveCamera() {
		Vector2 target = new Vector2(characterSprite.getX(), characterSprite.getY());

		camera.position.set(target.x, target.y, 0);
	}

	/**
	 * Applies gameplay/world constraints.
	 *
	 * <p>Clamps the character position to the map bounds (not the viewport bounds).
	 * With {@link ExtendViewport}, the visible world size can change with window aspect ratio.</p>
	 */
	private void logic() {
		float mapWidth = map.getWidth();
		float mapHeight = map.getHeight();
		float characterWidth = characterSprite.getWidth();
		float characterHeight = characterSprite.getHeight();

		characterSprite.setX(MathUtils.clamp(characterSprite.getX(), 0, mapWidth - characterWidth));
		characterSprite.setY(MathUtils.clamp(characterSprite.getY(), 0, mapHeight - characterHeight));
	}

	/**
	 * Clears the screen and renders the map background, character sprite, and walls.
	 */
	private void draw() {
		ScreenUtils.clear(Color.BLACK);
		spriteBatch.begin();

		Texture background = assets.getTexture(map.getBackgroundPath());
		// Draw in map/world coordinates so the background isn't stretched by viewport size.
		spriteBatch.draw(background, 0, 0, map.getWidth(), map.getHeight());

		world.draw(spriteBatch);

		if (!characterSprite.isDead()) {
			characterSprite.getWeapon().draw(spriteBatch);
			characterSprite.draw(spriteBatch);
		}

		for (Wall wall : map.getWalls()) {
			wall.draw(spriteBatch);
		}

		spriteBatch.end();
	}

	/**
	 * Disposes GPU resources owned by this screen.
	 *
	 * <p>Important: the {@link Map} currently loads its own background texture and {@link Wall} loads its
	 * own texture. Those textures are disposed here as well.</p>
	 */
	@Override
	public void dispose() {
		spriteBatch.dispose();

		assets.dispose();
	}

	public Map getMap() {
		return map;
	}
}
