package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.PriceLoader;
import org.lasarimanstudios.escapedungeon.SaveManager;
import org.lasarimanstudios.escapedungeon.assets.GameAssets;
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
	BASIC("Basic Sword", GameAssets.TEX_WEAPON_SWORD_BASIC, 8f, 0.55f, 1.5f),
	IRON("Iron Sword", GameAssets.TEX_WEAPON_SWORD_IRON, 12f, 0.50f, 1.5f),
	BLUE("Blue Sword", GameAssets.TEX_WEAPON_SWORD_BLUE, 15f, 0.45f, 1.5f),
	YELLOW("Yellow Sword", GameAssets.TEX_WEAPON_SWORD_YELLOW, 14f, 0.35f, 1.5f),
	PINK("Pink Sword", GameAssets.TEX_WEAPON_SWORD_PINK, 18f, 0.50f, 1.5f),
	RAINBOW("Rainbow Sword", GameAssets.TEX_WEAPON_SWORD_RAINBOW, 20f, 0.45f, 2.0f),
	GOLD("Gold Sword", GameAssets.TEX_WEAPON_SWORD_GOLD, 25f, 0.40f, 1.5f),
	FAT("Fat Sword", GameAssets.TEX_WEAPON_SWORD_FAT, 30f, 0.60f, 2.5f),

	RGB_SABER("RGB Saber", GameAssets.TEX_WEAPON_RGB_SABER, 30f, 0.30f, 1.5f) {
		@Override
		public Sword create(Array<Enemy> enemies, Array<Wall> walls, GameAssets assets) {
			return new RgbSaber(enemies, walls, assets.getTexture(texturePath), damage, speed, range, assets.getArcTexture());
		}
	},

	DEV("Dev Sword", GameAssets.TEX_WEAPON_SWORD_DEV, Float.MAX_VALUE, 0.35f, 20f) {
		@Override
		public Sword create(Array<Enemy> enemies, Array<Wall> walls, GameAssets assets) {
			return new DevSword(enemies, walls, assets.getTexture(texturePath), damage, speed, range, assets.getArcTexture());
		}
	};

	public final String displayName;
	public final String texturePath;
	public final float damage;
	public final float speed;
	public final float range;

	SwordType(String displayName, String texturePath, float damage, float speed, float range) {
		this.displayName = displayName;
		this.texturePath = texturePath;
		this.damage = damage;
		this.speed = speed;
		this.range = range;
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
	 * Returns the currently equipped sword type from save data.
	 * Falls back to {@link #BASIC} if the saved value is invalid.
	 */
	public static SwordType getEquipped() {
		String name = SaveManager.get(SaveManager.SaveKey.EQUIPPED_SWORD);
		try {
			return SwordType.valueOf(name);
		} catch (IllegalArgumentException e) {
			return BASIC;
		}
	}

	public Sword create(Array<Enemy> enemies, Array<Wall> walls, GameAssets assets) {
		return new Sword(enemies, walls, assets.getTexture(texturePath), damage, speed, range,
			assets.getArcTexture());
	}
}
