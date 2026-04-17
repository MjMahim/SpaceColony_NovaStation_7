package com.spacecolony.model;

import java.util.HashMap;

/**
 * Singleton that holds a Statistics object for every crew member.
 *
 * Uses a HashMap (crew member ID → Statistics) so lookups are O(1).
 * If a crew member has never been tracked yet, getOrCreate() builds
 * a fresh Statistics for them automatically.
 */
public class StatisticsManager {

    private static StatisticsManager instance;

    private StatisticsManager() {
        scoreboard = new HashMap<>();
    }

    public static StatisticsManager getInstance() {
        if (instance == null) {
            instance = new StatisticsManager();
        }
        return instance;
    }

    // id → Statistics for that crew member
    private final HashMap<Integer, Statistics> scoreboard;

    /**
     * Returns the Statistics for this crew member, creating a new one
     * if they haven't been seen before.
     */
    private Statistics getOrCreate(CrewMember cm) {
        int key = cm.getId();
        boolean seenBefore = scoreboard.containsKey(key);
        if (!seenBefore) {
            scoreboard.put(key, new Statistics());
        }
        return scoreboard.get(key);
    }

    // --- record calls forwarded to the individual Statistics object ---

    public void recordTraining(CrewMember cm) { getOrCreate(cm).recordTraining(); }
    public void recordMission(CrewMember cm)  { getOrCreate(cm).recordMission(); }
    public void recordVictory(CrewMember cm)  { getOrCreate(cm).recordVictory(); }

    /** Returns the Statistics for this crew member (never null). */
    public Statistics getStats(CrewMember cm) {
        return getOrCreate(cm);
    }

    /**
     * Builds a colony-wide overview string by adding up all individual stats.
     * Shown at the top of the Statistics screen.
     */
    public String getColonySummary() {
        int totalMissions  = 0;
        int totalWins      = 0;
        int trackedCrew    = scoreboard.size();

        for (Statistics s : scoreboard.values()) {
            totalMissions = totalMissions + s.getMissionsPlayed();
            totalWins     = totalWins     + s.getVictories();
        }

        String summary = "=== Colony Report ===\n"
                + "Crew on record : " + trackedCrew + "\n"
                + "Total missions : " + totalMissions + "\n"
                + "Total victories: " + totalWins;

        return summary;
    }
}
