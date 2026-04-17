package com.spacecolony.model;

/**
 * Represents the crew Quarters — the starting point for all crew members.
 *
 * Two operations happen here:
 * 1. Onboarding a new recruit (createCrewMember)
 * 2. Sending someone back to rest so their energy refills (restoreEnergy)
 *
 * All methods are static because there is no per-instance state needed.
 */
public class Quarters {

    /**
     * Registers a new crew member with the colony.
     * Sets their location to "Quarters" and adds them to Storage.
     */
    public static void createCrewMember(CrewMember cm) {
        cm.setLocation("Quarters");
        Storage.getInstance().addCrewMember(cm);
    }

    /**
     * Sends an existing crew member back to rest.
     * Fully restores their energy and moves them to "Quarters".
     * Used when returning from the Simulator.
     */
    public static void restoreEnergy(CrewMember cm) {
        cm.restoreEnergy();   // energy back to max
        cm.setLocation("Quarters");
    }
}
