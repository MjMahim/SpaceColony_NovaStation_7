package com.spacecolony.model;

/**
 * Scientist — the analyst of the crew.
 *
 * Stats: skill 8, resilience 1, max energy 17.
 * Specialization bonus: +3 damage on RESEARCH missions.
 *
 * Highest base skill in the roster but lowest resilience
 * (apart from Soldiers). Great at research, fragile in a fight.
 */
public class Scientist extends CrewMember {

    public Scientist(String name) {
        super(name, "Scientist", 8, 1, 17);
    }

    /**
     * Attacks during a mission turn.
     * Gets a +3 bonus on RESEARCH missions.
     */
    @Override
    public int act() {
        int randomBonus = (int) (Math.random() * 3);
        int dmg = getEffectiveSkill() + randomBonus;

        MissionType current = ActiveMission.getCurrentType();
        if (current == MissionType.RESEARCH) {
            dmg = dmg + 3;
        }

        return dmg;
    }
}
