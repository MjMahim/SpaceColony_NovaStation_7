package com.spacecolony.model;

/**
 * All five mission types the colony can run.
 *
 * Each type has one crew specialization that gets a +3 bonus
 * when they act() during that mission.
 *
 * Using an enum here keeps the type-checking clean — no magic strings
 * like "COMBAT" scattered around the code.
 */
public enum MissionType {

    NAVIGATION("Asteroid Field Navigation"),
    REPAIR    ("Station Repair"),
    MEDICAL   ("Medical Emergency"),
    RESEARCH  ("Research Expedition"),
    COMBAT    ("Alien Attack");

    // Human-readable name shown in the UI
    private final String label;

    MissionType(String label) {
        this.label = label;
    }

    public String getDisplayName() {
        return label;
    }

    /**
     * Picks a random mission type.
     * Used at mission launch so the player doesn't always get the same type.
     */
    public static MissionType random() {
        MissionType[] all = values();

        // cast is needed because Math.random() returns a double
        int pick = (int) (Math.random() * all.length);

        return all[pick];
    }
}
