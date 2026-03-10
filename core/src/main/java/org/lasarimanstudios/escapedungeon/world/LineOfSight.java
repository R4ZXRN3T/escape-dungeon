package org.lasarimanstudios.escapedungeon.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import org.lasarimanstudios.escapedungeon.world.tiles.Wall;

/**
 * Utility for checking whether a straight line between two points is blocked by any wall.
 *
 * <p>Uses a slab-method segment-vs-AABB intersection test for each wall rectangle.</p>
 */
public final class LineOfSight {

	/**
	 * Returns {@code true} if the straight line segment from ({@code originX}, {@code originY}) to
	 * ({@code targetX}, {@code targetY}) does not intersect any wall's bounding rectangle.
	 *
	 * @param walls   the wall array to test against
	 * @param originX start x
	 * @param originY start y
	 * @param targetX end x
	 * @param targetY end y
	 * @return {@code true} if no wall blocks the line
	 */
	public static boolean hasLineOfSight(Array<Wall> walls, float originX, float originY, float targetX, float targetY) {
		float dirX = targetX - originX;
		float dirY = targetY - originY;

		for (Wall wall : walls) {
			Rectangle rect = wall.getBoundingRectangle();

			float tMin = 0f;
			float tMax = 1f;

			// X slab
			if (dirX != 0f) {
				float tx1 = (rect.x - originX) / dirX;
				float tx2 = (rect.x + rect.width - originX) / dirX;
				float txMin = Math.min(tx1, tx2);
				float txMax = Math.max(tx1, tx2);
				tMin = Math.max(tMin, txMin);
				tMax = Math.min(tMax, txMax);
			} else {
				// Ray is parallel to the Y axis – check if origin X is inside the slab
				if (originX < rect.x || originX > rect.x + rect.width) {
					continue; // no intersection possible
				}
			}

			// Y slab
			if (dirY != 0f) {
				float ty1 = (rect.y - originY) / dirY;
				float ty2 = (rect.y + rect.height - originY) / dirY;
				float tyMin = Math.min(ty1, ty2);
				float tyMax = Math.max(ty1, ty2);
				tMin = Math.max(tMin, tyMin);
				tMax = Math.min(tMax, tyMax);
			} else {
				if (originY < rect.y || originY > rect.y + rect.height) {
					continue;
				}
			}

			if (tMin <= tMax) {
				return false; // a wall blocks line of sight
			}
		}
		return true;
	}
}
