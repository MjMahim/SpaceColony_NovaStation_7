package com.spacecolony.model;

/**
 * Tracks performance data for a single crew member.
 *
 * Three counters are kept:
 *   - how many missions they have participated in
 *   - how many of those were victories
 *   - how many training sessions they have completed
 *
 * One Statistics object is created per crew member and stored
 * in StatisticsManager using the crew member's ID as the key.
 */
public class Statistics {

    private int missionCount;
    private int winCount;
    private int drillCount;

    public Statistics() {
        missionCount = 0;
        winCount     = 0;
        drillCount   = 0;
    }

    // --- record methods called by StatisticsManager ---

    /** Increments the missions counter. Call when a mission starts or ends. */
    public void recordMission() {
        missionCount = missionCount + 1;
    }

    /** Increments the win counter. Call only on a successful mission. */
    public void recordVictory() {
        winCount = winCount + 1;
    }

    /** Increments the training drill counter. */
    public void recordTraining() {
        drillCount = drillCount + 1;
    }

    // --- getters ---

    public int getMissionsPlayed()   { return missionCount; }
    public int getVictories()        { return winCount; }
    public int getTrainingSessions() { return drillCount; }

    /**
     * Returns a formatted one-liner for this crew member's stats.
     * Used in the stats list.
     */
    public String getSummary(String name) {
        return name
                + "  |  Missions: " + missionCount
                + "  Wins: " + winCount
                + "  Drills: " + drillCount;
    }
}
