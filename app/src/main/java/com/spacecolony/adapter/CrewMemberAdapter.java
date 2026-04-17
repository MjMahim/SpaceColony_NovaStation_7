package com.spacecolony.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.spacecolony.R;
import com.spacecolony.databinding.ItemCrewMemberBinding;
import com.spacecolony.model.CrewMember;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

/**
 * Adapter for the crew member list used in Quarters, Simulator,
 * and Mission Control.
 *
 * When multiSelect is true, each row shows a checkbox.
 * The caller can then call getSelectedCrew() to get only
 * the ones that were checked.
 */
public class CrewMemberAdapter extends RecyclerView.Adapter<CrewMemberAdapter.ViewHolder> {

    private ArrayList<CrewMember> people = new ArrayList<>();
    private HashSet<Integer> ticked = new HashSet<>();  // IDs of checked people
    private boolean multiSelect;

    public CrewMemberAdapter(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    /** Replace the current list and clear any previous selections. */
    public void setCrewList(List<CrewMember> incoming) {
        people = new ArrayList<>(incoming);
        ticked.clear();
        notifyDataSetChanged();
    }

    /** Returns only the crew members whose checkbox is checked. */
    public List<CrewMember> getSelectedCrew() {
        List<CrewMember> chosen = new ArrayList<>();
        for (CrewMember p : people) {
            boolean isChecked = ticked.contains(p.getId());
            if (isChecked) {
                chosen.add(p);
            }
        }
        return chosen;
    }

    /** Unchecks all checkboxes. */
    public void clearSelections() {
        ticked.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCrewMemberBinding b = ItemCrewMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(people.get(position));
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    // -------------------------------------------------------------------------

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemCrewMemberBinding b;

        ViewHolder(ItemCrewMemberBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(final CrewMember person) {
            // Fill in the text fields
            // Show rank next to the name so the player can see progression at a glance
            String name = person.getName();
            String rank = person.getRank();

            SpannableString nameWithRank = new SpannableString(name + "  [" + rank + "]");
            int rankStart = name.length() + 2;
            int rankEnd   = nameWithRank.length();
            nameWithRank.setSpan(
                    new ForegroundColorSpan(rankColorFor(rank)),
                    rankStart, rankEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            b.tvCrewName.setText(nameWithRank);
            b.tvCrewSpec.setText(person.getSpecialization());
            // getEffectiveSkill() = baseSkill + xp, so this reflects training gains
            b.tvCrewStats.setText(
                    "Skill: "  + person.getEffectiveSkill()
                    + "  Res: "  + person.getResilience()
                    + "  XP: "   + person.getExperience());

            // Energy bar
            b.pbEnergy.setMax(person.getMaxEnergy());
            b.pbEnergy.setProgress(person.getEnergy());
            b.tvEnergyLabel.setText("Energy: " + person.getEnergy() + "/" + person.getMaxEnergy());

            // Colour strip and icon on the left
            String hexColor = colorFor(person.getSpecialization());
            b.viewColorStrip.setBackgroundColor(Color.parseColor(hexColor));
            b.ivSpecImage.setImageResource(iconFor(person.getSpecialization()));

            if (multiSelect) {
                b.cbSelect.setVisibility(View.VISIBLE);
                b.cbSelect.setChecked(ticked.contains(person.getId()));

                // Clicking the row OR the checkbox toggles the selection
                View.OnClickListener toggle = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int personId = person.getId();
                        if (ticked.contains(personId)) {
                            ticked.remove(personId);
                        } else {
                            ticked.add(personId);
                        }
                        notifyItemChanged(getAdapterPosition());
                    }
                };
                b.cbSelect.setOnClickListener(toggle);
                b.getRoot().setOnClickListener(toggle);

            } else {
                b.cbSelect.setVisibility(View.GONE);
            }
        }
    }

    // -------------------------------------------------------------------------
    // helpers

    /** Returns the hex colour associated with a specialization. */
    private String colorFor(String spec) {
        switch (spec) {
            case "Pilot":     return "#2196F3";   // blue
            case "Engineer":  return "#FFC107";   // amber
            case "Medic":     return "#4CAF50";   // green
            case "Scientist": return "#9C27B0";   // purple
            case "Soldier":   return "#F44336";   // red
            default:          return "#607D8B";   // grey fallback
        }
    }

    /** Returns the drawable resource for the role icon. */
    private int iconFor(String spec) {
        switch (spec) {
            case "Engineer":  return R.drawable.ic_engineer;
            case "Medic":     return R.drawable.ic_medic;
            case "Scientist": return R.drawable.ic_scientist;
            case "Soldier":   return R.drawable.ic_soldier;
            default:          return R.drawable.ic_pilot;
        }
    }
    private int rankColorFor(String rank) {
        switch (rank) {
            case "Specialist": return Color.parseColor("#4FC3F7");  // light blue
            case "Veteran":    return Color.parseColor("#FF9800");  // orange
            case "Elite":      return Color.parseColor("#FFD700");  // gold
            default:           return Color.parseColor("#78909C");  // grey (Cadet)
        }
    }
}
