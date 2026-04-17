package com.spacecolony.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.spacecolony.databinding.ItemMissionLogBinding;
import java.util.ArrayList;

/**
 * Adapter for the mission combat log.
 *
 * Each entry is a String added as the mission plays out.
 * Text is colour-coded so the player can quickly scan what happened:
 *
 *   Green  — success (mission complete, healed, xp gained)
 *   Red    — failure (mission failed, crew incapacitated)
 *   Amber  — combat action (attack, damage, strike)
 *   Grey   — neutral info (mission started, squad names, etc.)
 */
public class MissionLogAdapter extends RecyclerView.Adapter<MissionLogAdapter.LogRow> {

    private ArrayList<String> lines = new ArrayList<>();

    // Colour constants — defined once up here to avoid re-parsing hex strings every bind
    private static final int GREEN = Color.parseColor("#4CAF50");
    private static final int RED   = Color.parseColor("#F44336");
    private static final int AMBER = Color.parseColor("#FFC107");
    private static final int GREY  = Color.parseColor("#E0E0E0");

    /** Appends one line to the log and animates it in. */
    public void addEntry(String text) {
        lines.add(text);
        notifyItemInserted(lines.size() - 1);
    }

    /** Clears the log — called at the start of a new mission. */
    public void clearEntries() {
        lines.clear();
        notifyDataSetChanged();
    }

    @Override
    public LogRow onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemMissionLogBinding b = ItemMissionLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LogRow(b);
    }

    @Override
    public void onBindViewHolder(LogRow holder, int position) {
        holder.show(lines.get(position));
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

    // -------------------------------------------------------------------------

    static class LogRow extends RecyclerView.ViewHolder {

        private final ItemMissionLogBinding b;

        LogRow(ItemMissionLogBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void show(String text) {
            b.tvLogEntry.setText(text);
            b.tvLogEntry.setTextColor(pickColor(text));
        }

        /**
         * Decides which colour to use based on keywords in the log line.
         * Checking lowercase avoids case-sensitivity issues.
         */
        private int pickColor(String text) {
            if (text == null) {
                return GREY;
            }

            String lower = text.toLowerCase();

            boolean isGood = lower.contains("complete")
                    || lower.contains("neutralized")
                    || lower.contains("gains")
                    || lower.contains("heals")
                    || lower.contains("victory");

            boolean isBad = lower.contains("failed")
                    || lower.contains("incapacitated")
                    || lower.contains("energy: 0")
                    || lower.contains("defeated");

            boolean isAction = lower.contains("attacks")
                    || lower.contains("damage")
                    || lower.contains("strike")
                    || lower.contains("hits");

            if (isGood)   return GREEN;
            if (isBad)    return RED;
            if (isAction) return AMBER;

            return GREY;
        }
    }
}
