package com.spacecolony.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all the data about one mission.
 *
 * Created when a mission launches and filled in turn by turn as
 * the Fragment drives the combat loop. Once the mission ends
 * it has a complete record of what happened.
 *
 * Fields are mostly final because they should not change
 * after the mission is set up — only the log, finished flag,
 * and won flag are mutable.
 */
public class MissionResult {

    private final List<CrewMember> squadSnapshot;  // who went on this mission
    private final Threat enemy;
    private final MissionType kind;
    private final List<String> eventLog;           // narrative of what happened

    private boolean finished;
    private boolean won;

    /**
     * Creates a fresh MissionResult ready for the combat loop.
     * Makes a copy of the team list so changes during combat
     * don't affect the original snapshot.
     */
    public MissionResult(List<CrewMember> team, Threat enemy, MissionType kind) {
        this.squadSnapshot = new ArrayList<>(team);
        this.enemy         = enemy;
        this.kind          = kind;
        this.eventLog      = new ArrayList<>();
        this.finished      = false;
        this.won           = false;
    }

    /** Adds one line to the mission log (shown in the battle log RecyclerView). */
    public void addLogEntry(String line) {
        eventLog.add(line);
    }

    // --- getters ---

    public List<CrewMember> getTeam()    { return squadSnapshot; }
    public Threat getThreat()            { return enemy; }
    public MissionType getMissionType()  { return kind; }
    public List<String> getLog()         { return eventLog; }
    public boolean isMissionComplete()   { return finished; }
    public boolean isVictory()           { return won; }

    // --- setters ---

    public void setMissionComplete(boolean v) { this.finished = v; }
    public void setVictory(boolean v)         { this.won = v; }
}
