package com.spacecolony.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.spacecolony.R;
import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.databinding.FragmentQuartersBinding;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Storage;
import java.util.List;

/**
 * Shows the crew currently resting in Quarters.
 *
 * From here the player can:
 *   - Select crew and move them to the Simulator for training
 *   - Select crew and move them to Mission Control for a mission
 *   - Navigate to the recruit screen to add new crew members
 */
public class QuartersFragment extends Fragment {

    private FragmentQuartersBinding binding;
    private CrewMemberAdapter crewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQuartersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Checkboxes enabled so the player can select multiple crew at once
        crewAdapter = new CrewMemberAdapter(true);
        binding.rvCrew.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCrew.setAdapter(crewAdapter);

        binding.btnMoveSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CrewMember> picked = crewAdapter.getSelectedCrew();

                if (picked.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Select at least one crew member first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (CrewMember c : picked) {
                    c.setLocation("Simulator");
                }

                Toast.makeText(requireContext(),
                        picked.size() + " crew moved to Simulator.", Toast.LENGTH_SHORT).show();
                refreshList();
            }
        });

        binding.btnMoveMissionControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CrewMember> picked = crewAdapter.getSelectedCrew();

                if (picked.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Select at least one crew member first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (CrewMember c : picked) {
                    c.setLocation("MissionControl");
                }

                Toast.makeText(requireContext(),
                        picked.size() + " crew moved to Mission Control.", Toast.LENGTH_SHORT).show();
                refreshList();
            }
        });

        binding.btnRecruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_quarters_to_recruit);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Crew could have been moved from another screen, so always reload
        refreshList();
    }

    /** Pulls the current Quarters roster from Storage and updates the list. */
    private void refreshList() {
        List<CrewMember> here = Storage.getInstance().getCrewByLocation("Quarters");
        crewAdapter.setCrewList(here);

        boolean isEmpty = here.isEmpty();
        binding.tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
