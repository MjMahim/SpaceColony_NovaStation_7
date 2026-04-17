package com.spacecolony.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Statistics;
import com.spacecolony.model.StatisticsManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the per-crew stats list on the Statistics screen.
 *
 * Each row is just a single TextView created in code — no XML layout
 * needed because the content is always a one-line summary string.
 */
public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.RowHolder> {

    private ArrayList<CrewMember> crewList = new ArrayList<>();

    /** Swaps in a new list and refreshes the display. */
    public void setCrewList(List<CrewMember> newData) {
        crewList = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Build the TextView row programmatically
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(28, 18, 28, 18);
        tv.setTextSize(13.5f);
        tv.setTextColor(Color.WHITE);
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RowHolder(tv);
    }

    @Override
    public void onBindViewHolder(RowHolder holder, int position) {
        holder.fill(crewList.get(position));
    }

    @Override
    public int getItemCount() {
        return crewList.size();
    }

    // -------------------------------------------------------------------------

    static class RowHolder extends RecyclerView.ViewHolder {

        private final TextView label;

        RowHolder(TextView tv) {
            super(tv);
            label = tv;
        }

        void fill(CrewMember cm) {
            Statistics s = StatisticsManager.getInstance().getStats(cm);

            int missions = s.getMissionsPlayed();
            int wins     = s.getVictories();
            int drills   = s.getTrainingSessions();

            String line = cm.getName() + " [" + cm.getSpecialization() + "]"
                    + "   M:" + missions
                    + "  W:" + wins
                    + "  T:" + drills;

            label.setText(line);
        }
    }
}
