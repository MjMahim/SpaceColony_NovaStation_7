package com.spacecolony.model;

/**
 * Holds the mission type that is currently running.
 *
 * Because act() takes no parameters, each CrewMember subclass needs
 * some way to know what kind of mission is happening so it can apply
 * the right specialization bonus. This class acts as that shared context.
 *
 * The Fragment sets the type before calling act(), and clears it
 * when the mission ends.
 */
public class ActiveMission {

    // null means no mission is currently in progress
    private static MissionType ongoingType = null;

    // This class is never instantiated — purely static utility
    private ActiveMission() {}

    /** Call this before the first act() of a new mission. */
    public static void setCurrentType(MissionType t) {
        ongoingType = t;
    }

    /**
     * Returns what mission type is running right now.
     * Returns null if no mission has started yet.
     */
    public static MissionType getCurrentType() {
        return ongoingType;
    }

    /** Clears the mission context when a mission ends. */
    public static void clear() {
        ongoingType = null;
    }
}
