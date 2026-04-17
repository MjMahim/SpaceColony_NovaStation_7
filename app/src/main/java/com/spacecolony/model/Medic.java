package com.spacecolony.model;

/**
 * Medic — the healer of the crew.
 *
 * Stats: skill 7, resilience 2, max energy 18.
 * Specialization bonus: +3 damage on MEDICAL missions.
 *
 * Medics have an extra ability (heal) that no other class has.
 * During a mission turn they can use their special action to
 * restore up to 5 hp to a teammate instead of attacking.
 */
public class Medic extends CrewMember {

    public Medic(String name) {
        super(name, "Medic", 7, 2, 18);
    }

    /**
     * Attacks during a mission turn.
     * Gets a +3 bonus on MEDICAL missions.
     */
    @Override
    public int act() {
        int randomBonus = (int) (Math.random() * 3);
        int dmg = getEffectiveSkill() + randomBonus;

        MissionType current = ActiveMission.getCurrentType();
        if (current == MissionType.MEDICAL) {
            dmg = dmg + 3;
        }

        return dmg;
    }

    /**
     * Heals a target crew member by up to 5 hp.
     *
     * The result is capped at the target's maximum energy so
     * nobody can be healed above their max.
     * Can also be used as self-heal if target == this.
     */
    public void heal(CrewMember target) {
        int currentHp  = target.getEnergy();
        int afterHeal  = currentHp + 5;
        int cappedHp   = Math.min(afterHeal, target.getMaxEnergy());
        target.setEnergy(cappedHp);
    }
}
