package org.lasarimanstudios.escapedungeon.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;
import org.lasarimanstudios.escapedungeon.roguelike.PlayerStats;
import org.lasarimanstudios.escapedungeon.world.LineOfSight;
import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Sword weapon that swings in an arc.
 *
 * <p>During an attack, the sword interpolates its rotation from a start angle to an end angle over
 * {@link #getAttackSpeed()} seconds and damages enemies whose bounding rectangles overlap the sword's
 * bounding rectangle.</p>
 *
 * <p>The {@code range} stat uniformly scales the sword sprite (preserving the diagonal texture art).
 * A sword arc trail is rendered behind the blade during the forward swing.</p>
 */
public class Sword extends Weapon {

	protected static final float SOUND_DURATION = 0.342f;
	private static final float ARC_DEG = 180f;
	private static final float BASE_SIZE = 3f;
	protected static Sound swingSound;
	protected final Array<Enemy> enemies;
	protected final Array<Wall> walls;
	protected final Sprite arcSprite;
	protected float startAngle;
	protected float endAngle;
	protected float elapsedTime;
	protected boolean showArc;
	private final float baseRange;

	/**
	 * Creates a sword.
	 *
	 * @param enemies      enemies that can be hit (iterated every frame while attacking)
	 * @param walls        walls used for line-of-sight checks (enemies behind walls cannot be hit)
	 * @param texture      sword texture (must already be loaded)
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 * @param range        effective range – uniformly scales the sword sprite
	 * @param arcTexture   sword arc trail texture, or {@code null} to use a generated fallback
	 */
	public Sword(Array<Enemy> enemies, Array<Wall> walls, Texture texture, float attackDamage, float attackSpeed, float range, float knockback, Texture arcTexture) {
		super(texture, attackDamage, attackSpeed, knockback);
		this.baseRange = range;
		float size = BASE_SIZE * range;
		setSize(size, size);
		this.enemies = enemies;
		this.walls = walls;

		swingSound = Gdx.audio.newSound(Gdx.files.internal("sound/sword_swoosh.mp3"));

		Texture arcTex = arcTexture != null ? arcTexture : createFallbackArcTexture();
		this.arcSprite = new Sprite(arcTex);
		float arcSize = size * 1.3f;
		this.arcSprite.setSize(arcSize, arcSize);
	}

	/**
	 * Creates a simple semi-transparent white circle texture as a fallback arc effect.
	 */
	private static Texture createFallbackArcTexture() {
		int texSize = 32;
		Pixmap pixmap = new Pixmap(texSize, texSize, Pixmap.Format.RGBA8888);
		pixmap.setColor(1f, 1f, 1f, 0.4f);
		pixmap.fillCircle(texSize / 2, texSize / 2, texSize / 2 - 1);
		Texture tex = new Texture(pixmap);
		pixmap.dispose();
		return tex;
	}

	/**
	 * Updates the swing animation and applies damage to overlapping enemies.
	 *
	 * @param delta time since last frame in seconds
	 */
	@Override
	public void update(float delta) {
		if (!isAttacking()) {
			showArc = false;
			return;
		}

		elapsedTime += delta;

		float totalDuration = getAttackSpeed();
		float t = MathUtils.clamp(elapsedTime / totalDuration, 0f, 1f);

		float forwardPortion = 3f / 5f;
		float angle;

		float swingT = t / forwardPortion;
		angle = MathUtils.lerp(startAngle, endAngle,
			MathUtils.sin(swingT * MathUtils.PI / 2f));
		showArc = true;

		setRotation(angle);

		if (t >= 1f) {
			setAttacking(false);
			showArc = false;
		}

		for (Enemy enemy : enemies)
			if (enemy.getBoundingRectangle().overlaps(getBoundingRectangle()) && LineOfSight.hasLineOfSight(walls, getOriginX() + getX(), getOriginY() + getY(), enemy.getCenterX(), enemy.getCenterY()))
				enemy.takeDamage(getAttackDamage(), getKnockback(), angle, getAttackInstanceId());
	}

	/**
	 * Draws the sword and, during the forward swing, a trailing arc effect behind the blade.
	 *
	 * @param batch the sprite batch to draw with
	 */
	@Override
	public void draw(Batch batch) {
		if (showArc) {
			arcSprite.setOrigin(getOriginX(), getOriginY());
			arcSprite.setPosition(
				getX() - (arcSprite.getWidth() - getWidth()) / 2f,
				getY() - (arcSprite.getHeight() - getHeight()) / 2f
			);
			float lag = (endAngle > startAngle) ? -20f : 20f;
			arcSprite.setRotation(getRotation() + lag);

			float totalDuration = getAttackSpeed();
			float t = MathUtils.clamp(elapsedTime / totalDuration, 0f, 1f);
			float forwardPortion = 3f / 5f;
			float swingProgress = MathUtils.clamp(t / forwardPortion, 0f, 1f);
			arcSprite.setAlpha(0.5f * (1f - swingProgress));

			arcSprite.draw(batch);
		}
		super.draw(batch);
	}

	/**
	 * Starts a swing around the given facing angle.
	 *
	 * @param facingAngle player facing angle in degrees
	 */
	@Override
	public void startAttack(float facingAngle) {
		if (isAttacking()) return;

		beginAttackInstance();

		float halfArc = ARC_DEG * 0.5f;
		this.startAngle = facingAngle + halfArc + 45;
		this.endAngle = facingAngle - halfArc + 45;

		setAttacking(true);
		this.elapsedTime = 0f;
		this.showArc = false;

		setRotation(startAngle);

		float pitch = SOUND_DURATION / getAttackSpeed();
		swingSound.play(0.5f, pitch, 0f);
	}

	/**
	 * Updates the effective range by uniformly rescaling the sword and arc sprites.
	 *
	 * @param range new range multiplier
	 */
	public void setRange(float range) {
		float size = BASE_SIZE * range;
		setSize(size, size);
		float arcSize = size * 1.3f;
		this.arcSprite.setSize(arcSize, arcSize);
	}

	/**
	 * Applies player stats and adjusts weapon range based on perk bonuses.
	 */
	@Override
	public void setPlayerStats(PlayerStats playerStats) {
		super.setPlayerStats(playerStats);
		if (playerStats != null) {
			setRange(playerStats.applyRange(baseRange));
		}
	}

	/**
	 * Returns the X origin offset used to attach the sword to the player.
	 *
	 * @return attachment origin X in world units
	 */
	@Override
	public float getAttachmentOriginX() {
		return -1f * BASE_SIZE;
	}

	/**
	 * Returns the Y origin offset used to attach the sword to the player.
	 *
	 * @return attachment origin Y in world units
	 */
	@Override
	public float getAttachmentOriginY() {
		return -1f * BASE_SIZE;
	}
}
