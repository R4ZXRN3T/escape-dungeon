package org.lasarimanstudios.escapedungeon.roguelike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catalogue of all available roguelike perks/upgrades.
 *
 * <p>Each constant carries display metadata and knows how to apply itself to a
 * {@link PlayerStats} instance. Perks are split into two categories:</p>
 * <ul>
 *   <li><b>STAT</b> – pure numeric boosts (stackable: can be picked multiple times)</li>
 *   <li><b>ABILITY</b> – unique abilities (offered only once)</li>
 * </ul>
 */
public enum Perk {

    // ── stat boosts (stackable) ──────────────────────────────────

    DAMAGE_UP("Sharper Blade", "+20% attack damage", Category.STAT) {
        @Override
        public void apply(PlayerStats stats) {
            stats.addBonusDamagePercent(0.20f);
        }
    },

    SPEED_UP("Swift Feet", "+15% movement speed", Category.STAT) {
        @Override
        public void apply(PlayerStats stats) {
            stats.addBonusSpeedPercent(0.15f);
        }
    },

    HEALTH_UP("Tough Body", "+25 max health", Category.STAT) {
        @Override
        public void apply(PlayerStats stats) {
            stats.addMaxHealthBonus(25f);
        }
    },

    DEFENSE_UP("Iron Skin", "+15% damage reduction", Category.STAT) {
        @Override
        public void apply(PlayerStats stats) {
            stats.addDefensePercent(0.15f);
        }
    },

    ATTACK_SPEED_UP("Quick Hands", "+15% attack speed", Category.STAT) {
        @Override
        public void apply(PlayerStats stats) {
            stats.addAttackSpeedReduction(0.15f);
        }
    },

    RANGE_UP("Long Reach", "+20% weapon range", Category.STAT) {
        @Override
        public void apply(PlayerStats stats) {
            stats.addRangeMultiplierBonus(0.20f);
        }
    },

    // ── ability perks (unique) ───────────────────────────────────

    LIFESTEAL("Vampiric Strike", "Heal 10% of damage dealt", Category.ABILITY) {
        @Override
        public void apply(PlayerStats stats) {
            stats.setLifestealPercent(0.10f);
        }
    },


    THORNS("Thorns", "Reflect 25% of received damage to attacker", Category.ABILITY) {
        @Override
        public void apply(PlayerStats stats) {
            stats.setThornsPercent(0.25f);
        }
    };

    public final String displayName;
    public final String description;
    public final Category category;

    Perk(String displayName, String description, Category category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    /**
     * Applies this perk's effect to the given stats.
     *
     * @param stats the player stats to modify
     */
    public abstract void apply(PlayerStats stats);

    /**
     * Returns a random selection of perks that the player can still pick.
     *
     * <p>Stat perks are always available (stackable). Ability perks are excluded once acquired.</p>
     *
     * @param stats current player stats (used to filter already-acquired abilities)
     * @param count number of perks to return (capped to available pool size)
     * @return a list of random, distinct perks
     */
    public static List<Perk> getRandomSelection(PlayerStats stats, int count) {
        List<Perk> pool = new ArrayList<>();
        for (Perk perk : values()) {
            if (perk.category == Category.ABILITY && stats.hasPerk(perk)) {
                continue; // ability already acquired
            }
            pool.add(perk);
        }
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    /**
     * Perk category.
     */
    public enum Category {
        /** Numeric stat boost – can be picked multiple times. */
        STAT,
        /** Unique ability – offered only once. */
        ABILITY
    }
}

