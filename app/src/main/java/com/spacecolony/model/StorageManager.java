package com.spacecolony.model;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles saving and loading crew data to/from a JSON file
 * in the app's private internal storage.
 *
 * Only the fields that can change are stored: name, role, xp, location.
 * Base stats (skill, resilience, maxEnergy) are always the same for each
 * role, so on load we just reconstruct the right subclass and apply
 * the saved xp and location on top.
 *
 * File: crew_data.json in getFilesDir()
 */
public final class StorageManager {

    private static final String TAG      = "StorageManager";
    private static final String SAVEFILE = "crew_data.json";

    // JSON key names — defined as constants so a typo doesn't cause a silent bug
    private static final String K_ID   = "id";
    private static final String K_NAME = "name";
    private static final String K_ROLE = "role";
    private static final String K_XP   = "xp";
    private static final String K_LOC  = "location";

    // Static utility class — never instantiated
    private StorageManager() {}

    // =========================================================================
    // SAVE
    // =========================================================================

    /**
     * Serializes all crew in Storage to crew_data.json.
     * Called from onPause / onStop in MainActivity so data
     * is saved whenever the app is backgrounded.
     */
    public static void saveToFile(Context ctx) {
        if (ctx == null) {
            Log.w(TAG, "saveToFile: null context, skipping");
            return;
        }

        List<CrewMember> everyone = Storage.getInstance().listAllCrew();
        JSONArray jsonArr = new JSONArray();

        for (CrewMember cm : everyone) {
            try {
                JSONObject entry = new JSONObject();
                entry.put(K_ID,   cm.getId());
                entry.put(K_NAME, cm.getName());
                entry.put(K_ROLE, cm.getSpecialization());
                entry.put(K_XP,   cm.getExperience());
                entry.put(K_LOC,  cm.getLocation());
                jsonArr.put(entry);
            } catch (JSONException e) {
                Log.e(TAG, "Could not serialize " + cm.getName(), e);
            }
        }

        // Write to file
        File outFile = new File(ctx.getFilesDir(), SAVEFILE);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] bytes = jsonArr.toString(2).getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            fos.flush();
            Log.d(TAG, "Saved " + everyone.size() + " crew members");
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Write to file failed", e);
        }
    }

    // =========================================================================
    // LOAD
    // =========================================================================

    /**
     * Reads crew_data.json and reconstructs all crew members in Storage.
     *
     * If the file doesn't exist yet (first launch), this method just
     * returns silently — no crash, no error dialog.
     */
    public static void loadFromFile(Context ctx) {
        if (ctx == null) {
            Log.w(TAG, "loadFromFile: null context, skipping");
            return;
        }

        File inFile = new File(ctx.getFilesDir(), SAVEFILE);
        boolean fileExists = inFile.exists();

        if (!fileExists) {
            Log.d(TAG, "No save file yet — starting fresh");
            return;
        }

        // Read the raw file content into a StringBuilder
        StringBuilder rawJson = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(inFile);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rawJson.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not read save file", e);
            return;
        }

        // Parse the JSON and rebuild each crew member
        try {
            JSONArray arr   = new JSONArray(rawJson.toString());
            Storage storage = Storage.getInstance();

            // Clear whatever is in memory before reloading from disk
            List<CrewMember> existing = storage.listAllCrew();
            for (CrewMember old : existing) {
                storage.removeCrewMember(old);
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String role     = obj.optString(K_ROLE, "Pilot");
                String name     = obj.optString(K_NAME, "Unknown");
                int savedXp     = obj.optInt(K_XP, 0);
                String location = obj.optString(K_LOC, "Quarters");

                // Rebuild the right subclass from the saved role
                CrewMember rebuilt = makeFromRole(role, name);
                if (rebuilt == null) {
                    Log.w(TAG, "Unknown role '" + role + "', skipping entry");
                    continue;
                }

                // Apply the saved dynamic state
                rebuilt.gainExperience(savedXp);
                rebuilt.setLocation(location);

                storage.addCrewMember(rebuilt);
            }

            Log.d(TAG, "Loaded " + arr.length() + " crew members from file");

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse save file", e);
        }
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    /**
     * Creates the correct CrewMember subclass for a given role string.
     * Returns null for any unrecognized role (so the caller can skip it).
     */
    private static CrewMember makeFromRole(String role, String name) {
        switch (role) {
            case "Pilot":     return new Pilot(name);
            case "Engineer":  return new Engineer(name);
            case "Medic":     return new Medic(name);
            case "Scientist": return new Scientist(name);
            case "Soldier":   return new Soldier(name);
            default:          return null;
        }
    }
}
