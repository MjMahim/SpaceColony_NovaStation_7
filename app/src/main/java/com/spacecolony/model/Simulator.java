package com.spacecolony.model;

/**
 * Represents the training Simulator.
 *
 * Crew members who train here earn +1 XP per session.
 * The session is also logged in StatisticsManager so the
 * statistics screen can show how many drills each person has done.
 */
public class Simulator {

    /**
     * Runs one training session for the given crew member.
     *
     * Steps:
     *   1. Add +1 experience
     *   2. Make sure their location is set to "Simulator"
     *   3. Record the drill in StatisticsManager
     */
    public static void train(CrewMember cm) {
        cm.gainExperience(1);
        cm.setLocation("Simulator");
        StatisticsManager.getInstance().recordTraining(cm);
    }
}
