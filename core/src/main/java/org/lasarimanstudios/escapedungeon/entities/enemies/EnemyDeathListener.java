package org.lasarimanstudios.escapedungeon.entities.enemies;

/**
 * Listener that is notified when an {@link Enemy} dies.
 */
@FunctionalInterface
public interface EnemyDeathListener {
	/**
	 * Called when an enemy died.
	 *
	 * @param enemy the enemy instance that died
	 */
	void onEnemyDied(Enemy enemy);
}
