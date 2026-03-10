package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.PriceLoader;
import org.lasarimanstudios.escapedungeon.SaveManager;
import org.lasarimanstudios.escapedungeon.assets.AssetManager;
import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Data-driven catalogue of sword variants.
 *
 * <p>Each constant defines the texture path, base stats, and (optionally) a factory override.
 * Standard swords simply create a {@link Sword}; special swords (e.g. {@link #RGB_SABER})
 * override the factory to return a specialised subclass.</p>
 *
 * <p>Prices are loaded at runtime from {@code prices.json} via {@link PriceLoader}.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * Sword sword = SwordType.GOLD.create(enemies, walls, assets);
 * int price = SwordType.GOLD.getPrice();
 * }</pre>
 */
public enum SwordType {
	BASIC("Basic Sword", AssetManager.TEX_WEAPON_SWORD_BASIC, 8f, 0.55f, 1.5f, 0.75f),
	IRON("Iron Sword", AssetManager.TEX_WEAPON_SWORD_IRON, 12f, 0.50f, 1.5f, 1f),
	BLUE("Blue Sword", AssetManager.TEX_WEAPON_SWORD_BLUE, 15f, 0.45f, 1.5f, 1.3f),
	YELLOW("Yellow Sword", AssetManager.TEX_WEAPON_SWORD_YELLOW, 14f, 0.35f, 1.5f, 1.1f),
	PINK("Pink Sword", AssetManager.TEX_WEAPON_SWORD_PINK, 18f, 0.50f, 1.5f, 1.5f),
	RAINBOW("Rainbow Sword", AssetManager.TEX_WEAPON_SWORD_RAINBOW, 20f, 0.45f, 2.0f, 1.7f),
	GOLD("Gold Sword", AssetManager.TEX_WEAPON_SWORD_GOLD, 25f, 0.40f, 1.5f, 1.6f),
	FAT("Fat Sword", AssetManager.TEX_WEAPON_SWORD_FAT, 30f, 0.60f, 2.5f, 3f),

	RGB_SABER("RGB Saber", AssetManager.TEX_WEAPON_RGB_SABER, 30f, 0.30f, 1.5f, 2f) {
		@Override
		public Sword create(Array<Enemy> enemies, Array<Wall> walls, AssetManager assets) {
			return new RgbSaber(enemies, walls, assets.getTexture(texturePath), damage, speed, range, knockback, assets.getArcTexture());
		}
	},

	DEV("Dev Sword", AssetManager.TEX_WEAPON_SWORD_DEV, Float.MAX_VALUE, 0.35f, 20f, 10f) {
		@Override
		public Sword create(Array<Enemy> enemies, Array<Wall> walls, AssetManager assets) {
			return new DevSword(enemies, walls, assets.getTexture(texturePath), damage, speed, range, knockback, assets.getArcTexture());
		}
	};

	public final String displayName;
	public final String texturePath;
	public final float damage;
	public final float speed;
	public final float range;
	public final float knockback;

	/**
	 * Creates a sword type constant.
	 *
	 * @param displayName display name shown in the equipment UI
	 * @param texturePath internal texture asset path
	 * @param damage      base damage per hit
	 * @param speed       attack duration in seconds
	 * @param range       range multiplier
	 * @param knockback   knockback strength
	 */
	SwordType(String displayName, String texturePath, float damage, float speed, float range, float knockback) {
		this.displayName = displayName;
		this.texturePath = texturePath;
		this.damage = damage;
		this.speed = speed;
		this.range = range;
		this.knockback = knockback;
	}

	/**
	 * Returns the currently equipped sword type from save data.
	 * Falls back to {@link #BASIC} if the saved value is invalid.
	 *
	 * @return the equipped sword type
	 */
	public static SwordType getEquipped() {
		String name = SaveManager.get(SaveManager.SaveKey.EQUIPPED_SWORD);
		try {
			return SwordType.valueOf(name);
		} catch (IllegalArgumentException e) {
			return BASIC;
		}
	}

	/**
	 * Returns the price for this sword type, loaded from {@code prices.json}.
	 *
	 * @return price in currency units, or 0 if not defined
	 */
	public int getPrice() {
		return PriceLoader.getPrice(this.name());
	}

	/**
	 * Creates a new {@link Sword} instance from this type's stats.
	 *
	 * <p>Special sword types (e.g. {@link #RGB_SABER}, {@link #DEV}) override this method
	 * to return a specialised subclass.</p>
	 *
	 * @param enemies enemies that can be hit
	 * @param walls   walls used for line-of-sight checks
	 * @param assets  game asset registry providing textures
	 * @return a new sword instance
	 */
	public Sword create(Array<Enemy> enemies, Array<Wall> walls, AssetManager assets) {
		return new Sword(enemies, walls, assets.getTexture(texturePath), damage, speed, range, knockback, assets.getArcTexture());
	}
}
