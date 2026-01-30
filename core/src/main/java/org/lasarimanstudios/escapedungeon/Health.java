package org.lasarimanstudios.escapedungeon;

public class Health {
	private String toHealthBar(int health, int maxHealth, int barSize) {
		StringBuilder bar = new StringBuilder("|");
		double percentage = ((double) health) / maxHealth;
		int numHearts = (int) (percentage * barSize);
		for (int ii = 0; ii < numHearts; ++ii) {
			bar.append("#");
		}
		for (int ii = numHearts; ii < barSize; ++ii) {
			bar.append("-");
		}
		return bar.append("|").toString();
	}
}
