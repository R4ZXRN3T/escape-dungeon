package org.lasarimanstudios.escapedungeon.entities.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.entities.enemies.Enemy;

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

	private static final float ARC_DEG = 180f;
	/**
	 * Base size of the sword sprite in world units (both width and height, since textures are square).
	 * The actual displayed size is {@code BASE_SIZE * range}.
	 */
	private static final float BASE_SIZE = 3f;

	private final Array<Enemy> enemies;
	private final Sprite arcSprite;
	private float startAngle;
	private float endAngle;
	private float elapsedTime;
	private boolean showArc;
	private static Sound swingSound;
	private static final float SOUND_DURATION = 0.342f;

	/**
	 * Creates a sword.
	 *
	 * @param enemies      enemies that can be hit (iterated every frame while attacking)
	 * @param texture      sword texture (must already be loaded)
	 * @param attackDamage damage dealt per hit
	 * @param attackSpeed  attack duration in seconds
	 * @param range        effective range – uniformly scales the sword sprite
	 * @param arcTexture   sword arc trail texture, or {@code null} to use a generated fallback
	 */
	public Sword(Array<Enemy> enemies, Texture texture, float attackDamage, float attackSpeed, float range, Texture arcTexture) {
		super(texture, attackDamage, attackSpeed, range);
		// Range uniformly scales both dimensions so the diagonal texture isn't distorted.
		float size = BASE_SIZE * range;
		setSize(size, size);
		setOrigin(0.5f, getHeight() / 2f);
		this.enemies = enemies;

		swingSound = Gdx.audio.newSound(Gdx.files.internal("sound/sword_swoosh.mp3"));

		// Set up arc trail sprite.
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

		for (Enemy enemy : enemies) {
			if (enemy.getBoundingRectangle().overlaps(getBoundingRectangle())) {
				enemy.takeDamage(getAttackDamage(), 0f, angle, getAttackInstanceId());
			}
		}
	}

	/**
	 * Draws the sword and, during the forward swing, a trailing arc effect behind the blade.
	 */
	@Override
	public void draw(Batch batch) {
		if (showArc) {
			// Match the arc's origin to the sword's so they rotate around the same pivot.
			arcSprite.setOrigin(getOriginX(), getOriginY());
			arcSprite.setPosition(
				getX() - (arcSprite.getWidth() - getWidth()) / 2f,
				getY() - (arcSprite.getHeight() - getHeight()) / 2f
			);
			// Offset rotation slightly towards the start angle for a trailing effect.
			float lag = (endAngle > startAngle) ? -20f : 20f;
			arcSprite.setRotation(getRotation() + lag);

			// Fade arc based on swing progress.
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
}
