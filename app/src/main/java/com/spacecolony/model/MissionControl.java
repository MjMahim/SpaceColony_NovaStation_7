package com.spacecolony.model;

import java.util.List;

/**
 * Handles mission preparation and tracks how many missions
 * the colony has completed in total.
 *
 * The actual turn-by-turn combat loop is handled in
 * MissionControlFragment so the UI can update between each action.
 * This class just sets things up and provides the threat generator.
 */
public class MissionControl {

    // How many missions have been launched since the app started
    private static int launchCount = 0;

    public static int getMissionCount()        { return launchCount; }
    public static void incrementMissionCount() { launchCount++; }

    // --- instance variant (keeps MedbayManager injected for testability) ---

    private final MedbayManager bay;

    public MissionControl(MedbayManager bay) {
        this.bay = bay;
    }

    /**
     * Sets up a full mission:
     *   - generates the threat for this mission type
     *   - sets the active mission context so act() bonuses work
     *   - moves all team members to "MissionControl"
     *   - records the mission start in StatisticsManager
     *   - returns a MissionResult the Fragment can drive
     */
    public MissionResult launchMission(List<CrewMember> team, MissionType kind) {
        Threat danger = buildThreat(kind);
        ActiveMission.setCurrentType(kind);

        for (CrewMember person : team) {
            person.setLocation("MissionControl");
            StatisticsManager.getInstance().recordMission(person);
        }

        MissionResult result = new MissionResult(team, danger, kind);
        result.addLogEntry("Mission started: " + kind.getDisplayName());
        result.addLogEntry("Threat: " + danger.getName() + "  " + danger.getStatsString());
        return result;
    }

    /** Creates a Threat scaled to the current mission count. */
    public Threat buildThreat(MissionType kind) {
        return new Threat(launchCount, kind);
    }

    /**
     * Static convenience method so the Fragment can generate a threat
     * without needing a MissionControl instance.
     */
    public static Threat generateThreat(MissionType kind, boolean staticCall) {
        return new Threat(launchCount, kind);
    }
}
