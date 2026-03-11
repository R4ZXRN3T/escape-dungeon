package org.lasarimanstudios.escapedungeon.roguelike;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks per-run stat modifiers accumulated through perk selections.
 *
 * <p>An instance is created at the start of a run and shared between levels.
 * It is discarded when the player dies or returns to the main menu.</p>
 */
public class PlayerStats {

    private float bonusDamagePercent = 0f;
    private float bonusSpeedPercent = 0f;
    private float maxHealthBonus = 0f;
    private float defensePercent = 0f;
    private float attackSpeedReduction = 0f;
    private float rangeMultiplierBonus = 0f;
    private float lifestealPercent = 0f;
    private float thornsPercent = 0f;

	/**
     * Bonus added to every enemy's level when loading a new map.
     * Incremented each time the player advances to the next level.
     */
    private int enemyLevelBonus = 0;

    private final Set<Perk> acquiredPerks = new HashSet<>();

    // ── stat queries ──────────────────────────────────────────────

    /**
     * Returns effective damage after applying bonus damage modifiers.
     *
     * @param baseDamage raw weapon damage
     * @return modified damage
     */
    public float applyDamage(float baseDamage) {
        return baseDamage * (1f + bonusDamagePercent);
    }

    /**
     * Returns effective speed after applying bonus speed modifiers.
     *
     * @param baseSpeed raw movement speed
     * @return modified speed
     */
    public float applySpeed(float baseSpeed) {
        return baseSpeed * (1f + bonusSpeedPercent);
    }

    /**
     * Returns damage remaining after applying defense reduction.
     *
     * @param incomingDamage raw incoming damage
     * @return reduced damage (never less than 1)
     */
    public float applyDefense(float incomingDamage) {
        return Math.max(1f, incomingDamage * (1f - defensePercent));
    }

    /**
     * Returns effective attack duration after applying attack speed reduction.
     *
     * @param baseDuration raw attack duration in seconds
     * @return reduced duration
     */
    public float applyAttackSpeed(float baseDuration) {
        return baseDuration * (1f - attackSpeedReduction);
    }

    /**
     * Returns the range multiplier bonus (additive on top of the weapon's base range).
     *
     * @param baseRange raw weapon range multiplier
     * @return modified range multiplier
     */
    public float applyRange(float baseRange) {
        return baseRange * (1f + rangeMultiplierBonus);
    }

    public float getMaxHealthBonus() {
        return maxHealthBonus;
    }

    public float getLifestealPercent() {
        return lifestealPercent;
    }

    public boolean hasLifesteal() {
        return lifestealPercent > 0f;
    }

    public float getThornsPercent() {
        return thornsPercent;
    }

    public boolean hasThorns() {
        return thornsPercent > 0f;
    }

	// ── modifiers (called by Perk.apply) ─────────────────────────

    public void addBonusDamagePercent(float amount) {
        this.bonusDamagePercent += amount;
    }

    public void addBonusSpeedPercent(float amount) {
        this.bonusSpeedPercent += amount;
    }

    public void addMaxHealthBonus(float amount) {
        this.maxHealthBonus += amount;
    }

    public void addDefensePercent(float amount) {
        this.defensePercent += amount;
    }

    public void addAttackSpeedReduction(float amount) {
        this.attackSpeedReduction += amount;
    }

    public void addRangeMultiplierBonus(float amount) {
        this.rangeMultiplierBonus += amount;
    }

    public void setLifestealPercent(float amount) {
        this.lifestealPercent = amount;
    }

    public void setThornsPercent(float amount) {
        this.thornsPercent = amount;
    }

	/**
     * Returns the bonus added to every enemy's base level.
     */
    public int getEnemyLevelBonus() {
        return enemyLevelBonus;
    }

    /**
     * Increases the enemy level bonus (called when advancing to the next map).
     *
     * @param amount levels to add
     */
    public void addEnemyLevelBonus(int amount) {
        this.enemyLevelBonus += amount;
    }

    // ── perk tracking ────────────────────────────────────────────

    /**
     * Records a perk as acquired.
     *
     * @param perk the perk to add
     */
    public void acquirePerk(Perk perk) {
        acquiredPerks.add(perk);
    }

    /**
     * Returns whether the given perk has already been acquired.
     */
    public boolean hasPerk(Perk perk) {
        return acquiredPerks.contains(perk);
    }

}

