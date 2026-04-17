package com.spacecolony.model;

/**
 * Soldier — the fighter of the crew.
 *
 * Stats: skill 9, resilience 0, max energy 16.
 * Specialization bonus: +3 damage on COMBAT missions.
 *
 * Soldiers have the highest attack skill but start with 0 base resilience.
 * However, they override defend() to add +1 effective resilience from their
 * military training, so they actually take a little less damage than the
 * raw number suggests.
 */
public class Soldier extends CrewMember {

    public Soldier(String name) {
        super(name, "Soldier", 9, 0, 16);
    }

    /**
     * Attacks during a mission turn.
     * Gets a +3 bonus on COMBAT missions.
     */
    @Override
    public int act() {
        int randomBonus = (int) (Math.random() * 3);
        int dmg = getEffectiveSkill() + randomBonus;

        MissionType current = ActiveMission.getCurrentType();
        if (current == MissionType.COMBAT) {
            dmg = dmg + 3;
        }

        return dmg;
    }

    /**
     * Overrides the default defend() to give +1 effective armor.
     *
     * Even though the base resilience is 0, soldiers are trained
     * to take hits — so we treat their armor as 1 when calculating
     * how much damage actually gets through.
     */
    @Override
    public void defend(int incomingDmg) {
        // military training adds 1 to their effective armor
        int effectiveArmor = getResilience() + 1;

        int absorbed = incomingDmg - effectiveArmor;
        if (absorbed < 0) {
            absorbed = 0;
        }

        int newHp = getEnergy() - absorbed;
        if (newHp < 0) {
            newHp = 0;
        }
        setEnergy(newHp);
    }
}
