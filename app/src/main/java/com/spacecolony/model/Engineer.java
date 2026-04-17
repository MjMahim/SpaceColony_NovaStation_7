package com.spacecolony.model;

/**
 * Engineer — the technical expert of the crew.
 *
 * Stats: skill 6, resilience 3, max energy 19.
 * Specialization bonus: +3 damage on REPAIR missions.
 *
 * Engineers are well-rounded — decent skill and resilience,
 * and they absolutely dominate station repair emergencies.
 */
public class Engineer extends CrewMember {

    public Engineer(String name) {
        super(name, "Engineer", 6, 3, 19);
    }

    /**
     * Attacks during a mission turn.
     * Gets a +3 bonus when the mission type is REPAIR.
     */
    @Override
    public int act() {
        int randomBonus = (int) (Math.random() * 3);
        int dmg = getEffectiveSkill() + randomBonus;

        MissionType current = ActiveMission.getCurrentType();
        if (current == MissionType.REPAIR) {
            dmg = dmg + 3;
        }

        return dmg;
    }
}
