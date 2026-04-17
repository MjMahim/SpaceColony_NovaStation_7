package com.spacecolony.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the Medbay — the recovery room for crew who are knocked out in combat.
 *
 * When a crew member's energy hits 0 during a mission they are sent here
 * instead of being permanently removed (this satisfies the "No Death" bonus).
 * Once they are discharged their energy is fully restored and they go back
 * to Quarters, ready to be used again.
 *
 * This is a Singleton because there is only one Medbay on the station.
 */
public class MedbayManager {

    private static MedbayManager instance;

    private MedbayManager() {
        sickBay = new ArrayList<>();
    }

    public static MedbayManager getInstance() {
        if (instance == null) {
            instance = new MedbayManager();
        }
        return instance;
    }

    // The list of crew currently recovering
    private final List<CrewMember> sickBay;

    /**
     * Admits a crew member to the Medbay.
     *
     * Does three things:
     *   - Adds them to the sickBay list (if not already there)
     *   - Fully restores their energy
     *   - Changes their location to "Medbay"
     */
    public void sendToMedbay(CrewMember cm) {
        boolean alreadyHere = sickBay.contains(cm);
        if (!alreadyHere) {
            sickBay.add(cm);
        }
        // Energy is NOT restored here — they arrive exhausted.
        // restoreEnergy() is called only in discharge() so the
        // Medbay bar visually shows them as depleted while recovering.
        cm.setLocation("Medbay");
    }

    /**
     * Discharges a recovered crew member back to Quarters.
     *
     * Removes them from sickBay, restores energy, and moves them.
     */
    public void discharge(CrewMember cm) {
        sickBay.remove(cm);
        cm.restoreEnergy();
        cm.setLocation("Quarters");
    }

    /** Returns true if the crew member is currently in the Medbay. */
    public boolean isRecovering(CrewMember cm) {
        return sickBay.contains(cm);
    }

    /** Returns a copy of the current patient list (safe to iterate). */
    public List<CrewMember> getRecoveryQueue() {
        return new ArrayList<>(sickBay);
    }
}
