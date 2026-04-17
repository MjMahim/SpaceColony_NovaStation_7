package com.spacecolony.model;

/**
 * The enemy the squad faces during a mission.
 *
 * Threats scale with mission progress — the more missions
 * the colony has already run, the tougher the threat becomes.
 * This creates a natural difficulty curve without needing a
 * separate difficulty setting.
 *
 * Each mission type produces a different flavoured enemy name
 * (e.g. COMBAT → "Alien Raider", MEDICAL → "Viral Outbreak").
 */
public class Threat {

    private final String threatName;
    private final int attackPower;   // how hard it hits each turn
    private final int toughness;     // damage reduction (like armor)
    private int hp;
    private final int maxHp;
    private final MissionType origin;

    /**
     * Builds a threat whose stats are scaled to how many missions
     * have already been completed this session.
     */
    public Threat(int missionsCompleted, MissionType type) {
        this.origin      = type;

        // Scale the stats — more missions completed = harder threat
        this.attackPower = 4 + missionsCompleted;
        this.toughness   = 1 + (missionsCompleted / 3);
        this.maxHp       = 20 + (missionsCompleted * 3);
        this.hp          = this.maxHp;

        this.threatName  = pickName(type);
    }

    /** Returns a theme-appropriate name for this threat based on mission type. */
    private String pickName(MissionType t) {
        switch (t) {
            case NAVIGATION: return "Asteroid Storm";
            case REPAIR:     return "System Failure";
            case MEDICAL:    return "Viral Outbreak";
            case RESEARCH:   return "Rogue AI";
            case COMBAT:     return "Alien Raider";
            default:         return t.getDisplayName() + " Threat";
        }
    }

    // --- combat actions ---

    /**
     * Calculates the threat's attack damage this turn.
     * The small random part (0–2) keeps combat from being totally predictable.
     */
    public int attack() {
        int roll = (int) (Math.random() * 3);
        int totalDmg = attackPower + roll;
        return totalDmg;
    }

    /**
     * The crew deals damage to the threat.
     * Toughness reduces the incoming damage before it hits hp.
     */
    public void takeDamage(int incoming) {
        int afterToughness = incoming - toughness;

        // prevent negative damage (shouldn't happen but just in case)
        if (afterToughness < 0) {
            afterToughness = 0;
        }

        hp = hp - afterToughness;
        if (hp < 0) {
            hp = 0;
        }
    }

    /** Returns true when hp reaches 0. */
    public boolean isDefeated() {
        return hp <= 0;
    }

    // --- getters ---

    public String getName()             { return threatName; }
    public int getSkill()               { return attackPower; }
    public int getResilience()          { return toughness; }
    public int getEnergy()              { return hp; }
    public int getMaxEnergy()           { return maxHp; }
    public MissionType getMissionType() { return origin; }

    /** Short status line shown in the mission UI. */
    public String getStatsString() {
        return "power:" + attackPower
                + "  tough:" + toughness
                + "  hp:" + hp + "/" + maxHp;
    }

    @Override
    public String toString() {
        return threatName + " [" + origin.getDisplayName() + "] " + getStatsString();
    }
}
