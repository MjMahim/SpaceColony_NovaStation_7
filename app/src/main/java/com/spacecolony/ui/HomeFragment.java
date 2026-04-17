package com.spacecolony.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.spacecolony.R;
import com.spacecolony.databinding.FragmentHomeBinding;
import com.spacecolony.model.MissionControl;
import com.spacecolony.model.Storage;

/**
 * The main hub screen.
 *
 * Shows how many crew are in each section of the station,
 * how many missions have been run, and buttons to navigate
 * to every other part of the app.
 *
 * The counts are refreshed every time the user comes back
 * to this screen (in onResume) so they always reflect the
 * current state without needing any special callbacks.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController nav = Navigation.findNavController(view);

        // Show the colony name right away
        binding.tvColonyName.setText(Storage.getInstance().getColonyName());

        // Set up navigation buttons — each just navigates to the matching fragment
        binding.btnGoQuarters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.navigate(R.id.quartersFragment);
            }
        });

        binding.btnGoSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.navigate(R.id.simulatorFragment);
            }
        });

        binding.btnGoMissionControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.navigate(R.id.missionControlFragment);
            }
        });

        binding.btnGoMedbay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.navigate(R.id.medbayFragment);
            }
        });

        binding.btnRecruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nav.navigate(R.id.recruitFragment);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Crew can move between screens so we always refresh the counts here
        updateCounts();
    }

    /**
     * Reads the current crew counts from Storage and updates all
     * the TextViews on the home screen.
     */
    private void updateCounts() {
        Storage s = Storage.getInstance();

        binding.tvColonyName.setText(s.getColonyName());
        binding.tvQuartersCount.setText(String.valueOf(s.countByLocation("Quarters")));
        binding.tvSimulatorCount.setText(String.valueOf(s.countByLocation("Simulator")));
        binding.tvMissionControlCount.setText(String.valueOf(s.countByLocation("MissionControl")));
        binding.tvMedbayCount.setText(String.valueOf(s.countByLocation("Medbay")));
        binding.tvMissionCount.setText(String.valueOf(MissionControl.getMissionCount()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
