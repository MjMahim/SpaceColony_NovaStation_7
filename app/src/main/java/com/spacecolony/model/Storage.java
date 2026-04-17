package com.spacecolony.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Central in-memory store for all crew members.
 *
 * This is a Singleton — only one instance ever exists.
 * Every part of the app that needs crew data asks this class for it.
 * The actual data structure is a HashMap so lookups by ID are fast.
 */
public class Storage {

    // The one shared instance — created the first time getInstance() is called
    private static Storage instance;

    /** Private so nothing outside this class can call new Storage(). */
    private Storage() {
        roster = new HashMap<>();
    }

    /** Returns the single instance, creating it on first call. */
    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    // Maps crew member ID -> the CrewMember object
    private final HashMap<Integer, CrewMember> roster;

    // The colony has a name that shows on the home screen
    private String stationName = "NOVA STATION-7";

    // --- add / remove ---

    /** Adds a newly created crew member to the colony registry. */
    public void addCrewMember(CrewMember cm) {
        roster.put(cm.getId(), cm);
    }

    /** Removes by numeric ID. */
    public void removeCrewMember(int id) {
        roster.remove(id);
    }

    /** Removes by object reference — looks up the ID internally. */
    public void removeCrewMember(CrewMember cm) {
        if (cm != null) {
            roster.remove(cm.getId());
        }
    }

    /** Returns a crew member by their unique ID, or null if not found. */
    public CrewMember getCrewMember(int id) {
        return roster.get(id);
    }

    /**
     * Returns a snapshot of all crew currently in the colony.
     * It's a copy so callers can't accidentally mess with the internal map.
     */
    public List<CrewMember> listAllCrew() {
        List<CrewMember> everyone = new ArrayList<>(roster.values());
        return everyone;
    }

    // --- filtering ---

    /**
     * Returns everyone currently at a specific location.
     * Valid values: "Quarters", "Simulator", "MissionControl", "Medbay"
     */
    public List<CrewMember> getCrewByLocation(String loc) {
        List<CrewMember> found = new ArrayList<>();
        for (CrewMember person : roster.values()) {
            boolean isHere = loc.equals(person.getLocation());
            if (isHere) {
                found.add(person);
            }
        }
        return found;
    }

    /** Quick count of how many crew are at a given location. */
    public int countByLocation(String loc) {
        return getCrewByLocation(loc).size();
    }

    /** Wipes the roster — used when reloading from a save file. */
    public void clearAll() {
        roster.clear();
    }

    // --- colony name ---

    public String getColonyName()             { return stationName; }
    public void setColonyName(String newName) { this.stationName = newName; }
}
