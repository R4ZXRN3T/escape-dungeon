package org.lasarimanstudios.escapedungeon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import org.lasarimanstudios.escapedungeon.SaveManager;
import org.lasarimanstudios.escapedungeon.assets.AssetManager;
import org.lasarimanstudios.escapedungeon.entities.Character;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.level.Map;
import org.lasarimanstudios.escapedungeon.ui.HealthBarHUD;
import org.lasarimanstudios.escapedungeon.ui.MoneyHUD;
import org.lasarimanstudios.escapedungeon.weapons.SwordType;
import org.lasarimanstudios.escapedungeon.world.World;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Gameplay screen.
 *
 * <p>Owns the render loop for a {@link Map}: updates the player character, updates/enables the
 * {@link World} runtime state, updates enemies, and draws the background and sprites using a
 * {@link SpriteBatch}.</p>
 */
public class LevelScreen extends ScreenAdapter {
	/**
	 * Base world size used for gameplay rendering.
	 *
	 * <p>With an {@link ExtendViewport}, this is the minimum visible world size; on wider/taller
	 * windows the viewport will extend to fill the screen without stretching.</p>
	 */
	private static final float MIN_WORLD_WIDTH = 80f;
	private static final float MIN_WORLD_HEIGHT = 50f;
	private final DungeonGame game;
	private final Map map;
	private final SpriteBatch spriteBatch;
	private final Viewport viewport;
	private final OrthographicCamera camera;
	private final Character characterSprite;
	private final AssetManager assets;
	private final World world;
	private final HealthBarHUD healthBarHUD;
	private final MoneyHUD moneyHUD;
	private boolean deathHandled = false;

	private int currentMoney;

	/**
	 * Creates a new level screen for the given map.
	 *
	 * <p>Sets up the camera, viewport, player character (with the currently equipped sword),
	 * and wires all map-loaded enemies to the player and world death listener.</p>
	 *
	 * @param game   the game instance used to change screens
	 * @param map    the map to play
	 * @param assets shared asset registry
	 */
	public LevelScreen(DungeonGame game, Map map, AssetManager assets) {
		this.game = game;
		this.map = map;
		this.assets = assets;

        world = new org.lasarimanstudios.escapedungeon.world.World(map, assets, this);

		spriteBatch = new SpriteBatch();

		camera = new OrthographicCamera();
		viewport = new ExtendViewport(MIN_WORLD_WIDTH, MIN_WORLD_HEIGHT, camera);
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		SwordType equippedSword = SwordType.getEquipped();
		characterSprite = new Character(map.getWalls(), map.getEnemies(), assets, equippedSword, 100);
		characterSprite.setPosition(map.getStartPosX(), map.getStartPosY());

		characterSprite.setPlayerSprite("character_01");

		world.setPlayerCharacter(characterSprite);

		for (Enemy enemy : map.getEnemies()) {
			enemy.setCharacter(this.characterSprite);
			enemy.setDeathListener(world::onEnemyDied);
		}

		characterSprite.setDeathListener(this::onPlayerDied);

		currentMoney = 0;

		healthBarHUD = new HealthBarHUD();
		moneyHUD = new MoneyHUD();
		camera.update();
	}

	/**
	 * Add a certain amount of money to memory.
	 *
	 * <p>This does not persist. If the player dies and doesn't finish the level, this money amount will be reset</p>
	 *
	 * @param amount amount of money to add (can be negative)
	 */
	public void addMoney(int amount) {
		this.currentMoney += amount;
	}

	/**
	 * Handles the player's death by notifying the world and transitioning to the
	 * {@link DeathScreen}. Called at most once per level.
	 *
	 * @param character the player character that died
	 */
	private void onPlayerDied(Character character) {
		if (deathHandled) return;
		deathHandled = true;

		world.onPlayerDied(character);
		game.setScreen(new DeathScreen(game));
	}

	/**
	 * Updates the viewport and HUD camera when the window is resized.
	 *
	 * @param width  new width in pixels
	 * @param height new height in pixels
	 */
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		camera.update();
		healthBarHUD.resize(width, height);
		moneyHUD.resize(width, height);
	}

	/**
	 * Per-frame update/render loop: applies viewport, updates character input/rotation,
	 * moves camera, applies gameplay logic, then draws the frame.
	 *
	 * @param delta time since last frame in seconds
	 */
	@Override
	public void render(float delta) {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			game.setScreen(new MenuScreen(game, this));
			return;
		}
		viewport.apply();

		if (!deathHandled) {
			characterSprite.update(delta, camera);

			world.update(delta);
			for (Enemy enemy : map.getEnemies()) {
				enemy.update(delta);
			}
			if (map.getEnemies().isEmpty()) {
				SaveManager.addMoney(currentMoney);
				game.setScreen(new WinScreen(game, currentMoney));
				return;
			}

			logic();
			moveCamera();

			camera.update();
			spriteBatch.setProjectionMatrix(camera.combined);
		}

		draw();

		if (!deathHandled) {
			healthBarHUD.render(spriteBatch, characterSprite);
			moneyHUD.render(spriteBatch, currentMoney);
		}
	}

	/**
	 * Positions the camera to follow the player character.
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
	 * Clears the screen and renders the map background, world objects, character, weapon,
	 * and walls.
	 */
	private void draw() {
		ScreenUtils.clear(Color.BLACK);
		spriteBatch.begin();

		Texture background = assets.getTexture(map.getBackgroundPath());
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
	 * Disposes GPU resources owned by this screen, including the shared {@link AssetManager}.
	 */
	@Override
	public void dispose() {
		spriteBatch.dispose();
		healthBarHUD.dispose();
		moneyHUD.dispose();

		assets.dispose();
	}
}
