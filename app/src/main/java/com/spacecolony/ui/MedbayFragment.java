package com.spacecolony.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.spacecolony.adapter.MedbayAdapter;
import com.spacecolony.databinding.FragmentMedbayBinding;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.MedbayManager;
import com.spacecolony.model.Storage;
import java.util.List;

/**
 * The infirmary screen.
 *
 * Crew members end up here when their energy drops to zero during a mission.
 * They stay until a commander (the user) manually taps their row to discharge
 * them back to Quarters.  This is the "no permanent death" feature —
 * everyone can recover and fight again.
 */
public class MedbayFragment extends Fragment {

    private FragmentMedbayBinding binding;

    // Adapter that shows the sickbay roster and handles discharge taps
    private MedbayAdapter sickAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMedbayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvInfo.setText("Tap a crew member to discharge them back to Quarters.");

        // Pass a listener so the adapter can call back when a row is tapped
        sickAdapter = new MedbayAdapter(new MedbayAdapter.DischargeListener() {
            @Override
            public void onDischarge(CrewMember patient) {
                doDischarge(patient);
            }
        });

        binding.rvMedbay.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedbay.setAdapter(sickAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Patients can arrive from MissionControl so refresh every time
        refreshList();
    }

    /**
     * Discharges a single patient — moves them to Quarters and reloads the list.
     *
     * @param patient the crew member being released from the medbay
     */
    private void doDischarge(CrewMember patient) {
        MedbayManager.getInstance().discharge(patient);
        // After discharge the list is shorter, so reload it
        refreshList();
    }

    /**
     * Reads the current medbay occupants from Storage and updates the RecyclerView.
     * Also flips the empty-state label on or off.
     */
    private void refreshList() {
        List<CrewMember> patients = Storage.getInstance().getCrewByLocation("Medbay");

        sickAdapter.setCrewList(patients);

        boolean wardEmpty = patients.isEmpty();
        binding.tvEmpty.setVisibility(wardEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
