package com.spacecolony.model;

/**
 * Pilot — the navigator of the crew.
 *
 * Stats: skill 5, resilience 4, max energy 20.
 * Specialization bonus: +3 damage on NAVIGATION missions.
 *
 * Pilots have good resilience, meaning they absorb hits well,
 * but their base skill is lower than most other roles.
 */
public class Pilot extends CrewMember {

    public Pilot(String name) {
        super(name, "Pilot", 5, 4, 20);
    }

    /**
     * Attacks during a mission turn.
     *
     * The random part (0–2) simulates the unpredictability of combat.
     * On a NAVIGATION mission the Pilot gets an extra +3 because
     * asteroid fields and warp jumps are their home turf.
     */
    @Override
    public int act() {
        int randomBonus = (int) (Math.random() * 3);  // 0, 1 or 2
        int dmg = getEffectiveSkill() + randomBonus;

        MissionType current = ActiveMission.getCurrentType();
        boolean isNavMission = (current == MissionType.NAVIGATION);

        if (isNavMission) {
            dmg = dmg + 3;
        }

        return dmg;
    }
}
