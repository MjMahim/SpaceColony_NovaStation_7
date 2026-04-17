package com.spacecolony.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.adapter.MissionLogAdapter;
import com.spacecolony.databinding.FragmentMissionControlBinding;
import com.spacecolony.model.ActiveMission;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.MedbayManager;
import com.spacecolony.model.Medic;
import com.spacecolony.model.MissionControl;
import com.spacecolony.model.MissionResult;
import com.spacecolony.model.MissionType;
import com.spacecolony.model.StatisticsManager;
import com.spacecolony.model.Storage;
import com.spacecolony.model.Threat;
import java.util.ArrayList;
import java.util.List;

/**
 * Mission Control — the most involved screen in the app.
 *
 * The screen has two visual states:
 *   • Lobby  — the pre-mission panel where you pick 2-3 crew members to deploy.
 *   • Battle — the turn-by-turn combat panel that appears once a mission starts.
 *
 * During battle the user chooses Attack, Defend, or Special each turn.
 * The enemy always hits back after the crew acts (damage is halved when defending).
 * Any crew member who drops to 0 energy is automatically sent to the Medbay.
 * The mission ends when either the enemy is defeated (victory) or the whole
 * squad is incapacitated (defeat).
 *
 * State that matters during a live mission:
 *   squad      — the crew members currently fighting
 *   turnIndex  — index into squad for who is acting this turn
 *   enemy      — the threat we are fighting
 *   missionData — running log and outcome tracker
 *   over       — flag that stops the action buttons once the mission ends
 */
public class MissionControlFragment extends Fragment {

    private FragmentMissionControlBinding binding;

    private CrewMemberAdapter squadAdapter;
    private MissionLogAdapter logAdapter;

    // --- live mission state ---
    private List<CrewMember> squad    = new ArrayList<>();
    private int turnIndex             = 0;
    private Threat enemy              = null;
    private MissionResult missionData = null;
    private boolean over              = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMissionControlBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSquadList();
        setupLog();
        showLobby();

        binding.btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMission();
            }
        });

        binding.btnAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAttack();
            }
        });

        binding.btnDefend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDefend();
            }
        });

        binding.btnSpecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSpecial();
            }
        });

        binding.btnReturnCrew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endAndReturn();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only refresh the squad picker if we are not already in a battle
        boolean idleInLobby = !over && enemy == null;
        if (idleInLobby) {
            reloadSquadList();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // =========================================================================
    // SETUP
    // =========================================================================

    /** Attaches the multi-select adapter for the pre-mission squad picker. */
    private void setupSquadList() {
        squadAdapter = new CrewMemberAdapter(true);
        binding.rvSquadSelect.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSquadSelect.setAdapter(squadAdapter);
        reloadSquadList();
    }

    /** Attaches the colour-coded log adapter to the battle log RecyclerView. */
    private void setupLog() {
        logAdapter = new MissionLogAdapter();
        binding.rvMissionLog.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMissionLog.setAdapter(logAdapter);
    }

    /**
     * Refreshes the available crew list — only crew at "MissionControl"
     * can be selected for a mission.
     */
    private void reloadSquadList() {
        List<CrewMember> available = Storage.getInstance().getCrewByLocation("MissionControl");
        squadAdapter.setCrewList(available);

        boolean noOneThere = available.isEmpty();
        binding.tvNoCrewMsg.setVisibility(noOneThere ? View.VISIBLE : View.GONE);
    }

    // =========================================================================
    // PHASE SWITCHING
    // =========================================================================

    /** Shows the lobby panel and hides the battle panel. */
    private void showLobby() {
        binding.layoutPreMission.setVisibility(View.VISIBLE);
        binding.layoutMission.setVisibility(View.GONE);
        reloadSquadList();
    }

    /** Flips to the battle panel and sets the initial button visibility. */
    private void showBattleScreen() {
        binding.layoutPreMission.setVisibility(View.GONE);
        binding.layoutMission.setVisibility(View.VISIBLE);
        binding.layoutActionButtons.setVisibility(View.VISIBLE);
        binding.btnReturnCrew.setVisibility(View.GONE);
        binding.btnReturnCrew.setText("Return to Base");
    }

    // =========================================================================
    // MISSION START
    // =========================================================================

    /**
     * Validates the squad selection, picks a random mission type,
     * spawns the matching threat, and transitions to the battle screen.
     *
     * Squads must be 2-3 people — solo runs and four-person groups are blocked.
     */
    private void startMission() {
        List<CrewMember> chosen = squadAdapter.getSelectedCrew();

        boolean tooSmall = chosen.size() < 2;
        boolean tooBig   = chosen.size() > 3;

        if (tooSmall || tooBig) {
            Toast.makeText(requireContext(),
                    "Select 2 or 3 crew members to launch.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Snapshot the squad and reset turn state
        squad      = new ArrayList<>(chosen);
        turnIndex  = 0;
        over       = false;
        logAdapter.clearEntries();

        // Randomly pick what kind of mission this will be
        MissionType mType = MissionType.random();
        ActiveMission.setCurrentType(mType);

        // Generate the enemy threat — second arg true = hard mode scaling
        enemy       = MissionControl.generateThreat(mType, true);
        missionData = new MissionResult(squad, enemy, mType);

        squadAdapter.clearSelections();
        showBattleScreen();

        log("=== MISSION STARTED: " + mType.getDisplayName() + " ===");
        log("Threat: " + enemy.getName());
        log("Squad: " + namesOf(squad));

        refreshBattleUI();
    }

    // =========================================================================
    // COMBAT ACTIONS
    // =========================================================================

    /**
     * Standard attack — current crew member deals damage based on their skill
     * and a random roll inside act().  Enemy always retaliates after.
     */
    private void doAttack() {
        if (over) return;

        CrewMember actor = currentActor();
        if (actor == null) return;

        int dmg = actor.act();
        enemy.takeDamage(dmg);

        log(actor.getName() + " attacks for " + dmg + " damage!");
        missionData.addLogEntry(actor.getName() + " attacks for " + dmg);

        boolean enemyDead = enemy.isDefeated();
        if (enemyDead) {
            victory();
            return;
        }

        // Enemy hits back, no defence bonus
        enemyHits(false);
        advanceTurn();
    }

    /**
     * Defend — the acting crew member takes a stance.
     * They deal no damage this turn but incoming damage is halved.
     */
    private void doDefend() {
        if (over) return;

        CrewMember actor = currentActor();
        if (actor == null) return;

        log(actor.getName() + " braces for impact!");
        missionData.addLogEntry(actor.getName() + " defends");

        // Pass true so enemyHits() knows to halve the damage
        enemyHits(true);
        advanceTurn();
    }

    /**
     * Special ability — behaviour depends on the crew member's specialization:
     *   Medic   → heals the most-injured ally, or self-heals if alone
     *   Soldier → double strike (two separate damage rolls)
     *   Others  → boosted attack (+2 flat bonus on top of the normal roll)
     */
    private void doSpecial() {
        if (over) return;

        CrewMember actor = currentActor();
        if (actor == null) return;

        String role = actor.getSpecialization();

        if (role.equals("Medic")) {
            runMedicSpecial(actor);

        } else if (role.equals("Soldier")) {
            boolean killed = runSoldierSpecial(actor);
            if (killed) return;   // victory() was already called inside

        } else {
            boolean killed = runGenericSpecial(actor, role);
            if (killed) return;
        }

        enemyHits(false);
        advanceTurn();
    }

    /**
     * Medic special — heals a teammate if possible, self-heals otherwise.
     *
     * @param actor the Medic who is acting
     */
    private void runMedicSpecial(CrewMember actor) {
        CrewMember target = findAllyToHeal(actor);

        if (target != null) {
            // Heal a teammate
            int hpBefore = target.getEnergy();
            ((Medic) actor).heal(target);
            int healedAmt = target.getEnergy() - hpBefore;
            log(actor.getName() + " heals " + target.getName() + " for " + healedAmt + " HP!");
            missionData.addLogEntry(actor.getName() + " heals " + target.getName() + " +" + healedAmt);
        } else {
            // Nobody else to heal — self-heal
            int hpBefore = actor.getEnergy();
            ((Medic) actor).heal(actor);
            int healedAmt = actor.getEnergy() - hpBefore;
            log(actor.getName() + " self-heals +" + healedAmt + " HP!");
            missionData.addLogEntry(actor.getName() + " self-heals +" + healedAmt);
        }
    }

    /**
     * Soldier special — strikes twice in a single turn.
     *
     * @param actor the Soldier acting
     * @return true if the second strike killed the enemy (so the caller can return early)
     */
    private boolean runSoldierSpecial(CrewMember actor) {
        int hit1  = actor.act();
        int hit2  = actor.act();
        int total = hit1 + hit2;

        enemy.takeDamage(total);

        log(actor.getName() + " double strikes for " + total + " total damage!");
        missionData.addLogEntry(actor.getName() + " double strike: " + total);

        boolean enemyDown = enemy.isDefeated();
        if (enemyDown) {
            victory();
            return true;
        }
        return false;
    }

    /**
     * Generic special — any non-Medic, non-Soldier role gets a flat +2 on top
     * of their normal act() roll.
     *
     * @param actor the crew member acting
     * @param role  their specialization label (for the log message)
     * @return true if the boosted hit killed the enemy
     */
    private boolean runGenericSpecial(CrewMember actor, String role) {
        int baseDmg  = actor.act();
        int boosted  = baseDmg + 2;

        enemy.takeDamage(boosted);

        log(actor.getName() + " [" + role + " special] hits for " + boosted + "!");
        missionData.addLogEntry(actor.getName() + " special: " + boosted);

        boolean enemyDown = enemy.isDefeated();
        if (enemyDown) {
            victory();
            return true;
        }
        return false;
    }

    /** Cleans up mission state and returns the lobby after a mission ends. */
    private void endAndReturn() {
        // Move surviving crew back to their station
        boolean wasVictory = missionData != null && missionData.isVictory();
        if (wasVictory) {
            for (CrewMember survivor : squad) {
                if (survivor.isAlive()) {
                    survivor.setLocation("MissionControl");
                }
            }
        }

        squad.clear();
        enemy       = null;
        missionData = null;
        over        = false;
        logAdapter.clearEntries();
        showLobby();
    }

    // =========================================================================
    // TURN MECHANICS
    // =========================================================================

    /**
     * Enemy counter-attack — hits a random surviving squad member.
     *
     * Instead of always targeting whoever just acted, the enemy picks
     * any living crew member at random.  This spreads damage across the
     * whole squad and makes the Defend action more meaningful as a way
     * to protect the current actor from a lucky enemy strike.
     *
     * @param actorIsDefending if true, damage is halved ONLY if the random
     *                         target happens to be the current actor
     */
    private void enemyHits(boolean actorIsDefending) {
        boolean cannotHit = squad.isEmpty() || enemy == null || enemy.isDefeated();
        if (cannotHit) return;

        // Pick a random living squad member as the target
        int randomIndex  = (int) (Math.random() * squad.size());
        CrewMember target = squad.get(randomIndex);

        int rawHit = enemy.attack();

        // Defend bonus only applies when the defending actor is the one being hit
        CrewMember actor       = currentActor();
        boolean actorIsTarget  = (actor != null && actor.equals(target));
        boolean applyDefend    = actorIsDefending && actorIsTarget;

        if (applyDefend) {
            int reducedHit = rawHit / 2;
            log(enemy.getName() + " attacks! " + target.getName()
                    + " blocks — takes only " + reducedHit + " damage.");
            missionData.addLogEntry(enemy.getName() + " hits " + target.getName()
                    + " for " + reducedHit + " (blocked)");
            target.defend(reducedHit);
        } else {
            log(enemy.getName() + " hits " + target.getName() + " for " + rawHit + "!");
            missionData.addLogEntry(enemy.getName() + " hits " + target.getName() + " for " + rawHit);
            target.defend(rawHit);
        }
    }

    /**
     * Moves to the next surviving crew member's turn.
     *
     * Anyone who dropped to 0 is removed from the squad first and sent
     * to the Medbay.  If nobody is left the mission ends in defeat.
     */
    private void advanceTurn() {
        if (over) return;

        // Collect everyone who just got knocked out
        List<CrewMember> fallen = new ArrayList<>();
        for (CrewMember c : squad) {
            boolean knockedOut = !c.isAlive();
            if (knockedOut) {
                fallen.add(c);
            }
        }

        // Process casualties — remove from squad and send to medbay
        for (CrewMember down : fallen) {
            log(down.getName() + " is incapacitated! (energy: 0)");
            missionData.addLogEntry(down.getName() + " incapacitated");
            MedbayManager.getInstance().sendToMedbay(down);
            squad.remove(down);
        }

        boolean squadWiped = squad.isEmpty();
        if (squadWiped) {
            defeat();
            return;
        }

        // Wrap turnIndex back into range and refresh the UI
        turnIndex = turnIndex % squad.size();
        refreshBattleUI();
    }

    // =========================================================================
    // VICTORY / DEFEAT
    // =========================================================================

    /**
     * Called when the enemy reaches 0 HP.
     *
     * Surviving crew each earn 1 XP and a victory is recorded for them.
     * Everyone (including those already incapacitated) gets a mission count.
     */
    private void victory() {
        over = true;
        missionData.setVictory(true);
        missionData.setMissionComplete(true);

        log("=== MISSION COMPLETE ===");
        log(enemy.getName() + " has been neutralized!");

        // Reward survivors
        for (CrewMember survivor : squad) {
            if (survivor.isAlive()) {
                survivor.gainExperience(1);
                StatisticsManager.getInstance().recordVictory(survivor);
                StatisticsManager.getInstance().recordMission(survivor);
                log(survivor.getName() + " earns 1 XP!");
            }
        }

        // Also tick mission count for anyone incapacitated mid-fight
        List<CrewMember> fullTeam = missionData.getTeam();
        if (fullTeam != null) {
            for (CrewMember participant : fullTeam) {
                boolean alreadyCounted = squad.contains(participant);
                if (!alreadyCounted) {
                    StatisticsManager.getInstance().recordMission(participant);
                }
            }
        }

        MissionControl.incrementMissionCount();

        // Swap action buttons for the return button
        binding.layoutActionButtons.setVisibility(View.GONE);
        binding.btnReturnCrew.setVisibility(View.VISIBLE);
        binding.btnReturnCrew.setText("Victory! Return Crew");

        updateEnemyBar();
        updateSquadStatus();
    }

    /**
     * Called when the last squad member is incapacitated.
     *
     * No XP is awarded but every participant still gets a mission count
     * so the statistics screen stays accurate.
     */
    private void defeat() {
        over = true;
        missionData.setVictory(false);
        missionData.setMissionComplete(true);

        log("=== MISSION FAILED ===");
        log("The whole squad has been incapacitated.");

        List<CrewMember> fullTeam = missionData.getTeam();
        if (fullTeam != null) {
            for (CrewMember participant : fullTeam) {
                StatisticsManager.getInstance().recordMission(participant);
            }
        }

        binding.layoutActionButtons.setVisibility(View.GONE);
        binding.btnReturnCrew.setVisibility(View.VISIBLE);
        binding.btnReturnCrew.setText("Return to Base");
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================

    /** Refreshes the enemy bar, current actor panel, and the full squad status strip. */
    private void refreshBattleUI() {
        updateEnemyBar();
        updateActorPanel();
        updateSquadStatus();
    }

    /**
     * Rebuilds the horizontal squad status strip so all members are always visible.
     *
     * Each member gets a small card showing their name, role, and an energy bar.
     * The card for the current actor gets an accent border so it is easy to spot.
     * Members already knocked out (sent to Medbay) are no longer in the squad list
     * so they disappear from the strip automatically.
     */
    private void updateSquadStatus() {
        LinearLayout strip = binding.layoutSquadStatus;
        strip.removeAllViews();   // clear old cards before rebuilding

        for (int i = 0; i < squad.size(); i++) {
            CrewMember member = squad.get(i);
            boolean isActing  = (i == turnIndex);

            // Outer card container
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(16, 10, 16, 10);

            // Highlight the acting member with a brighter background
            if (isActing) {
                card.setBackgroundColor(Color.parseColor("#1E3A5F"));
            } else {
                card.setBackgroundColor(Color.parseColor("#1E2A3A"));
            }

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    dpToPx(110), LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(6, 0, 6, 0);
            card.setLayoutParams(cardParams);

            // Member name label
            TextView nameLabel = new TextView(requireContext());
            nameLabel.setText(member.getName());
            nameLabel.setTextColor(isActing
                    ? Color.parseColor("#42A5F5")   // accent blue for active turn
                    : Color.WHITE);
            nameLabel.setTextSize(12f);
            nameLabel.setMaxLines(1);
            card.addView(nameLabel);

            // Role label
            TextView roleLabel = new TextView(requireContext());
            roleLabel.setText(member.getSpecialization());
            roleLabel.setTextColor(Color.parseColor("#B0BEC5"));
            roleLabel.setTextSize(10f);
            card.addView(roleLabel);

            // Rank badge — updates live as XP changes during a mission
            TextView rankLabel = new TextView(requireContext());
            rankLabel.setText(member.getRank());
            rankLabel.setTextColor(rankColorFor(member.getRank()));
            rankLabel.setTextSize(9f);
            card.addView(rankLabel);

            // Energy fraction label
            TextView hpLabel = new TextView(requireContext());
            hpLabel.setText(member.getEnergy() + "/" + member.getMaxEnergy());
            hpLabel.setTextColor(Color.parseColor("#B0BEC5"));
            hpLabel.setTextSize(10f);
            card.addView(hpLabel);

            // Mini energy progress bar
            ProgressBar bar = new ProgressBar(requireContext(),
                    null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(member.getMaxEnergy());
            int currentHp = member.getEnergy();
            if (currentHp < 0) currentHp = 0;
            bar.setProgress(currentHp);
            bar.getProgressDrawable().setColorFilter(
                    new android.graphics.PorterDuffColorFilter(
                            Color.parseColor("#4CAF50"),
                            android.graphics.PorterDuff.Mode.SRC_IN));
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6));
            barParams.topMargin = dpToPx(4);
            bar.setLayoutParams(barParams);
            card.addView(bar);

            strip.addView(card);
        }
    }

    /** Converts dp to pixels using the display density. */
    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Each rank gets its own colour in the squad strip so it’s easy to spot
     * who is experienced and who is still fresh.
     */
    private int rankColorFor(String rank) {
        switch (rank) {
            case "Elite":      return Color.parseColor("#FFD700");  // gold
            case "Veteran":    return Color.parseColor("#FF9800");  // orange
            case "Specialist": return Color.parseColor("#4FC3F7");  // light blue
            default:           return Color.parseColor("#78909C");  // grey for Cadet
        }
    }

    /**
     * Updates the enemy health bar and name/stats labels.
     * Clamps the progress bar to 0 so it never goes negative.
     */
    private void updateEnemyBar() {
        if (enemy == null) return;

        binding.tvThreatName.setText(enemy.getName());
        binding.tvThreatStats.setText(enemy.getStatsString());
        binding.pbThreatEnergy.setMax(enemy.getMaxEnergy());

        int enemyHpNow = enemy.getEnergy();
        if (enemyHpNow < 0) enemyHpNow = 0;
        binding.pbThreatEnergy.setProgress(enemyHpNow);
    }

    /**
     * Updates the panel showing whose turn it currently is,
     * along with their current energy bar.
     */
    private void updateActorPanel() {
        CrewMember who = currentActor();
        if (who == null) return;

        String actorLabel = who.getName() + " [" + who.getSpecialization() + "]";
        binding.tvCurrentActor.setText(actorLabel);

        String energyLabel = "Energy: " + who.getEnergy() + "/" + who.getMaxEnergy();
        binding.tvActorEnergy.setText(energyLabel);

        binding.pbActorEnergy.setMax(who.getMaxEnergy());
        int actorHpNow = who.getEnergy();
        if (actorHpNow < 0) actorHpNow = 0;
        binding.pbActorEnergy.setProgress(actorHpNow);
    }

    /**
     * Appends a line to the mission log and scrolls to the newest entry.
     *
     * The scroll is posted to the main thread so it runs after the adapter
     * finishes drawing the new item.
     *
     * @param msg the text line to add
     */
    private void log(String msg) {
        logAdapter.addEntry(msg);

        binding.rvMissionLog.post(new Runnable() {
            @Override
            public void run() {
                int lastPos = logAdapter.getItemCount() - 1;
                if (lastPos >= 0) {
                    binding.rvMissionLog.scrollToPosition(lastPos);
                }
            }
        });
    }

    /**
     * Returns whoever should act this turn.
     * Guards against turnIndex going out of range.
     *
     * @return the current CrewMember, or null if the squad is empty
     */
    private CrewMember currentActor() {
        if (squad.isEmpty()) return null;
        if (turnIndex >= squad.size()) turnIndex = 0;
        return squad.get(turnIndex);
    }

    /**
     * Scans the squad for a living ally other than the one acting.
     * Used by the Medic special — we prefer healing someone else first.
     *
     * @param exclude the acting crew member to skip
     * @return an alive teammate, or null if there isn't one
     */
    private CrewMember findAllyToHeal(CrewMember exclude) {
        for (CrewMember c : squad) {
            boolean isDifferentPerson = !c.equals(exclude);
            boolean stillAlive       = c.isAlive();
            if (isDifferentPerson && stillAlive) {
                return c;
            }
        }
        return null;
    }

    /**
     * Joins crew names with commas for the opening log line.
     *
     * @param members the crew members to name
     * @return e.g. "Alice, Bob, Carol"
     */
    private String namesOf(List<CrewMember> members) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            sb.append(members.get(i).getName());
            boolean notLast = i < members.size() - 1;
            if (notLast) sb.append(", ");
        }
        return sb.toString();
    }
}
