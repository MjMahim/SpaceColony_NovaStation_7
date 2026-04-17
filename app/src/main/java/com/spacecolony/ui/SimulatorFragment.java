package com.spacecolony.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.databinding.FragmentSimulatorBinding;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Quarters;
import com.spacecolony.model.Simulator;
import com.spacecolony.model.Storage;
import java.util.List;

/**
 * The training deck — crew members stationed here can run drills
 * to earn XP, or be sent back to Quarters to rest and refill energy.
 *
 * The fragment uses a multi-select RecyclerView so you can train
 * or return several people in one tap.
 */
public class SimulatorFragment extends Fragment {

    private FragmentSimulatorBinding binding;
    private CrewMemberAdapter crewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSimulatorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Multi-select adapter — true means checkboxes are shown
        crewAdapter = new CrewMemberAdapter(true);
        binding.rvCrew.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCrew.setAdapter(crewAdapter);

        binding.btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runDrills();
            }
        });

        binding.btnReturnQuarters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRest();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Crew can move between screens so reload every time we come back
        refreshList();
    }

    /**
     * Runs a training drill for every selected crew member.
     * Each member gains +1 XP and the result is logged in StatisticsManager.
     */
    private void runDrills() {
        List<CrewMember> picked = crewAdapter.getSelectedCrew();

        boolean nobodyPicked = picked.isEmpty();
        if (nobodyPicked) {
            Toast.makeText(requireContext(), "Select crew to train.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (CrewMember trainee : picked) {
            Simulator.train(trainee);   // +1 xp, logged internally
        }

        String resultMsg = "Trained " + picked.size() + " member(s).";
        Toast.makeText(requireContext(), resultMsg, Toast.LENGTH_SHORT).show();
        refreshList();
    }

    /**
     * Moves selected crew back to Quarters and refills their energy.
     * Useful before a mission so they show up fully rested.
     */
    private void sendToRest() {
        List<CrewMember> picked = crewAdapter.getSelectedCrew();

        boolean nobodyPicked = picked.isEmpty();
        if (nobodyPicked) {
            Toast.makeText(requireContext(), "Select crew to send back.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (CrewMember tired : picked) {
            Quarters.restoreEnergy(tired);  // moves to Quarters and refills hp
        }

        String resultMsg = picked.size() + " crew resting in Quarters.";
        Toast.makeText(requireContext(), resultMsg, Toast.LENGTH_SHORT).show();
        refreshList();
    }

    /** Loads only the crew whose location is "Simulator" and shows/hides the empty label. */
    private void refreshList() {
        List<CrewMember> hereNow = Storage.getInstance().getCrewByLocation("Simulator");
        crewAdapter.setCrewList(hereNow);

        boolean nobody = hereNow.isEmpty();
        binding.tvEmpty.setVisibility(nobody ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
