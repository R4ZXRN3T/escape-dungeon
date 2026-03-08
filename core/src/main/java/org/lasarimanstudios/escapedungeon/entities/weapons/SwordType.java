package org.lasarimanstudios.escapedungeon.entities.weapons;

import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.assets.GameAssets;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;

/**
 * Data-driven catalogue of sword variants.
 *
 * <p>Each constant defines the texture path, base stats, and (optionally) a factory override.
 * Standard swords simply create a {@link Sword}; special swords (e.g. {@link #RGB_SABER})
 * override the factory to return a specialised subclass.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * Sword sword = SwordType.GOLD.create(enemies, assets);
 * }</pre>
 */
public enum SwordType {
	BASIC(GameAssets.TEX_WEAPON_SWORD_BASIC, 8f, 0.55f, 1.5f),
	IRON(GameAssets.TEX_WEAPON_SWORD_IRON, 12f, 0.50f, 1.5f),
	BLUE(GameAssets.TEX_WEAPON_SWORD_BLUE, 15f, 0.45f, 1.5f),
	GOLD(GameAssets.TEX_WEAPON_SWORD_GOLD, 25f, 0.40f, 1.5f),
	PINK(GameAssets.TEX_WEAPON_SWORD_PINK, 18f, 0.50f, 1.5f),
	YELLOW(GameAssets.TEX_WEAPON_SWORD_YELLOW, 14f, 0.35f, 1.5f),
	FAT(GameAssets.TEX_WEAPON_SWORD_FAT, 40f, 0.60f, 2.5f),
	RAINBOW(GameAssets.TEX_WEAPON_SWORD_RAINBOW, 20f, 0.45f, 2.0f),

	/**
	 * Special sword that cycles through rainbow hues while swinging.
	 */
	RGB_SABER(GameAssets.TEX_WEAPON_RGB_SABER, 30f, 0.30f, 1.5f) {
		@Override
		public Sword create(Array<Enemy> enemies, GameAssets assets) {
			return new RgbSaber(enemies, assets.getTexture(texturePath), damage, speed, range,
				assets.getArcTexture());
		}
	};

	/**
	 * Internal asset path for the sword texture.
	 */
	public final String texturePath;
	/**
	 * Base damage per hit.
	 */
	public final float damage;
	/**
	 * Attack duration in seconds.
	 */
	public final float speed;
	/**
	 * Effective range in world units.
	 */
	public final float range;

	SwordType(String texturePath, float damage, float speed, float range) {
		this.texturePath = texturePath;
		this.damage = damage;
		this.speed = speed;
		this.range = range;
	}

	/**
	 * Creates a {@link Sword} instance for this type.
	 *
	 * <p>Override this method in enum constants that need a specialised subclass.</p>
	 *
	 * @param enemies reference to the enemy list (for hit detection)
	 * @param assets  game assets (provides textures)
	 * @return a new sword instance with the stats defined by this type
	 */
	public Sword create(Array<Enemy> enemies, GameAssets assets) {
		return new Sword(enemies, assets.getTexture(texturePath), damage, speed, range,
			assets.getArcTexture());
	}
}
