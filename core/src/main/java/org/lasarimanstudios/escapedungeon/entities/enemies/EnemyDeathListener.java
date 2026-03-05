package org.lasarimanstudios.escapedungeon.entities.enemies;

/**
 * Callback for enemy death events.
 */
@FunctionalInterface
public interface EnemyDeathListener {
	void onEnemyDied(Enemy enemy);
}

