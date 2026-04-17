package com.spacecolony.model;

/**
 * Abstract base for every person in the colony.
 *
 * All five specializations (Pilot, Engineer, Medic, Scientist, Soldier)
 * extend this class. It stores the shared stats and handles the common
 * logic like taking damage and restoring energy.
 *
 * The act() method is left abstract so each subclass can define
 * its own combat behaviour — that is the polymorphism part of this project.
 */
public abstract class CrewMember {

    // Counts how many crew members have been created across the whole session.
    // Static means all instances share the same counter.
    private static int memberSeq = 0;

    // --- fields ---

    private final int uid;         // unique ID, set once in the constructor
    private String crewName;
    private String role;           // "Pilot", "Engineer", etc.
    private final int baseSkill;   // fixed at recruitment, never changes
    private int armor;             // soaks part of each incoming hit
    private int xp;                // grows from training and mission victories
    private int hp;                // current energy — drops in combat
    private int maxHp;
    private String whereAmI;       // which room the crew member is currently in

    /**
     * Sets up a crew member with all the basic stats.
     * Called by each subclass constructor via super(...).
     */
    public CrewMember(String crewName, String role, int baseSkill, int armor, int maxHp) {
        this.uid       = ++memberSeq;
        this.crewName  = crewName;
        this.role      = role;
        this.baseSkill = baseSkill;
        this.armor     = armor;
        this.maxHp     = maxHp;
        this.hp        = maxHp;    // start fully rested
        this.xp        = 0;
        this.whereAmI  = "Quarters";
    }

    // --- abstract ---

    /**
     * What this crew member does on their mission turn.
     * Each subclass overrides this with their own damage formula
     * and specialization bonus.
     */
    public abstract int act();

    // --- concrete methods ---

    /**
     * Takes a hit and reduces hp accordingly.
     * Armor is subtracted from the incoming damage first.
     * hp cannot go below zero.
     */
    public void defend(int incomingDmg) {
        int afterArmor = incomingDmg - armor;

        // make sure we don't get negative damage (if armor > hit)
        if (afterArmor < 0) {
            afterArmor = 0;
        }

        int newHp = hp - afterArmor;
        if (newHp < 0) {
            newHp = 0;
        }
        hp = newHp;
    }

    /**
     * Returns a rank title based on total XP earned.
     *
     * Thresholds were chosen so a player needs a decent amount of
     * training and missions to reach the top rank — it doesn't come
     * for free.
     *
     *   0–4   XP  →  Cadet
     *   5–14  XP  →  Specialist
     *   15–29 XP  →  Veteran
     *   30+   XP  →  Elite
     */
    public String getRank() {
        if (xp >= 30) {
            return "Elite";
        }
        boolean isVet = (xp >= 15);
        if (isVet) {
            return "Veteran";
        }
        boolean isSpec = (xp >= 5);
        if (isSpec) {
            return "Specialist";
        }
        return "Cadet";
    }

    /**
     * Returns the combined skill used for damage calculations.
     * As crew gain XP their effective skill goes up, which is
     * why veteran crew hit harder than freshly recruited ones.
     */
    public int getEffectiveSkill() {
        int combined = baseSkill + xp;
        return combined;
    }

    /** True as long as the crew member still has energy remaining. */
    public boolean isAlive() {
        return hp > 0;
    }

    /** Fully recharges hp — used when resting in Quarters or leaving Medbay. */
    public void restoreEnergy() {
        hp = maxHp;
    }

    /** Called after a training session or a successful mission. */
    public void gainExperience(int amount) {
        xp = xp + amount;
    }

    /** How many crew have ever been created in this session (static counter). */
    public static int getNumberOfCreated() {
        return memberSeq;
    }

    // --- getters ---

    public int getId()                { return uid; }
    public String getName()           { return crewName; }
    public String getSpecialization() { return role; }
    public int getSkill()             { return baseSkill; }
    public int getResilience()        { return armor; }
    public int getExperience()        { return xp; }
    public int getEnergy()            { return hp; }
    public int getMaxEnergy()         { return maxHp; }
    public String getLocation()       { return whereAmI; }

    // --- setters ---

    public void setName(String n)           { this.crewName = n; }
    public void setSpecialization(String r) { this.role = r; }
    public void setResilience(int a)        { this.armor = a; }
    public void setExperience(int e)        { this.xp = e; }
    public void setLocation(String loc)     { this.whereAmI = loc; }
    public void setMaxEnergy(int m)         { this.maxHp = m; }

    /**
     * Sets hp but clamps it so it can never go below 0 or above maxHp.
     * Used by the Medic heal ability.
     */
    public void setEnergy(int val) {
        if (val < 0)    val = 0;
        if (val > maxHp) val = maxHp;
        hp = val;
    }

    @Override
    public String toString() {
        return role + "(" + crewName + ")"
                + " skill:" + baseSkill
                + " armor:" + armor
                + " xp:" + xp
                + " hp:" + hp + "/" + maxHp;
    }
}
