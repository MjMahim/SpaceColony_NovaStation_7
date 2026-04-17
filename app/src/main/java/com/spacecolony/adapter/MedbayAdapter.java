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
import java.util.List;

/**
 * Adapter for the Medbay patient list.
 *
 * Each row shows a recovering crew member and a hint that tapping
 * will discharge them. The actual discharge logic is handled by the
 * Fragment through the DischargeListener interface.
 */
public class MedbayAdapter extends RecyclerView.Adapter<MedbayAdapter.ViewHolder> {

    /**
     * The Fragment implements this and passes it in via the constructor.
     * Keeps the adapter from knowing anything about Fragment logic.
     */
    public interface DischargeListener {
        void onDischarge(CrewMember cm);
    }

    private ArrayList<CrewMember> patients = new ArrayList<>();
    private final DischargeListener listener;

    public MedbayAdapter(DischargeListener listener) {
        this.listener = listener;
    }

    public void setCrewList(List<CrewMember> list) {
        patients = new ArrayList<>(list);
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
        holder.bind(patients.get(position));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    // -------------------------------------------------------------------------

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemCrewMemberBinding b;

        ViewHolder(ItemCrewMemberBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(final CrewMember patient) {
            // Rank is shown even in Medbay — a Veteran is still a Veteran when injured
            String nameWithRank = patient.getName() + "  [" + patient.getRank() + "]";
            b.tvCrewName.setText(nameWithRank);
            b.tvCrewSpec.setText(patient.getSpecialization() + " — Recovering");
            // getEffectiveSkill() reflects any XP gained from training
            b.tvCrewStats.setText(
                    "Skill: " + patient.getEffectiveSkill()
                    + "  Res: " + patient.getResilience()
                    + "  XP: " + patient.getExperience());

            b.pbEnergy.setMax(patient.getMaxEnergy());
            b.pbEnergy.setProgress(patient.getEnergy());
            b.tvEnergyLabel.setText("Tap to discharge");

            String hexColor = colorFor(patient.getSpecialization());
            b.viewColorStrip.setBackgroundColor(Color.parseColor(hexColor));
            b.ivSpecImage.setImageResource(iconFor(patient.getSpecialization()));

            // No checkbox needed in the Medbay
            b.cbSelect.setVisibility(View.GONE);

            // Tapping the row fires the discharge callback
            b.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDischarge(patient);
                    }
                }
            });
        }
    }

    // -------------------------------------------------------------------------

    private String colorFor(String spec) {
        switch (spec) {
            case "Pilot":     return "#2196F3";
            case "Engineer":  return "#FFC107";
            case "Medic":     return "#4CAF50";
            case "Scientist": return "#9C27B0";
            case "Soldier":   return "#F44336";
            default:          return "#607D8B";
        }
    }

    private int iconFor(String spec) {
        switch (spec) {
            case "Engineer":  return R.drawable.ic_engineer;
            case "Medic":     return R.drawable.ic_medic;
            case "Scientist": return R.drawable.ic_scientist;
            case "Soldier":   return R.drawable.ic_soldier;
            default:          return R.drawable.ic_pilot;
        }
    }
}
