package com.spacecolony.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.spacecolony.R;
import com.spacecolony.databinding.FragmentRecruitBinding;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Engineer;
import com.spacecolony.model.Medic;
import com.spacecolony.model.Pilot;
import com.spacecolony.model.Quarters;
import com.spacecolony.model.Scientist;
import com.spacecolony.model.Soldier;

/**
 * Recruitment screen.
 *
 * The user types a name, picks a specialization from the spinner,
 * sees a live stat preview, and taps Recruit to add the new crew
 * member to Quarters.  Cancel goes straight back without saving anything.
 *
 * Each specialization is a different subclass of CrewMember, so
 * polymorphism kicks in the moment a mission calls act() on them.
 */
public class RecruitFragment extends Fragment {

    private FragmentRecruitBinding binding;

    // The order here matches the spinner positions (0-4)
    private static final String[] ROLES = {
            "Pilot", "Engineer", "Medic", "Scientist", "Soldier"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecruitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinner();
        showPreview("Pilot");   // default selection on entry

        binding.spinnerSpec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Update preview card whenever the user changes the spinner
                showPreview(ROLES[pos]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.btnRecruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRecruit(v);
            }
        });

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    /** Wires up the role spinner with a simple array adapter. */
    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ROLES
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSpec.setAdapter(spinnerAdapter);
    }

    /**
     * Validates the name field, creates the right subclass, places them in Quarters,
     * and navigates back to the previous screen.
     *
     * @param v the view that was clicked (needed for NavController lookup)
     */
    private void tryRecruit(View v) {
        // Read and clean up whatever the user typed
        String rawInput = binding.etCrewName.getText() != null
                ? binding.etCrewName.getText().toString()
                : "";
        String enteredName = rawInput.trim();

        boolean nameEmpty = enteredName.isEmpty();
        if (nameEmpty) {
            Toast.makeText(requireContext(), "Please enter a name first.", Toast.LENGTH_SHORT).show();
            return;
        }

        int spinnerPos     = binding.spinnerSpec.getSelectedItemPosition();
        String chosenRole  = ROLES[spinnerPos];
        CrewMember recruit = buildCrewMember(chosenRole, enteredName);

        // Place in Quarters — sets location and adds to Storage
        Quarters.createCrewMember(recruit);

        String successMsg = chosenRole + " \"" + enteredName + "\" joined the crew!";
        Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show();

        Navigation.findNavController(v).navigateUp();
    }

    /**
     * Updates the preview card below the spinner — shows stats and the role icon.
     *
     * Stats are hard-coded here to match each subclass constructor so the user
     * knows exactly what they are getting before confirming.
     *
     * @param role the currently selected role name
     */
    private void showPreview(String role) {
        String info;
        int icon;

        switch (role) {
            case "Pilot":
                info = "Skill: 5  Resilience: 4  Max Energy: 20\nSpecialty: Navigation & evasion";
                icon = R.drawable.ic_pilot;
                break;
            case "Engineer":
                info = "Skill: 6  Resilience: 3  Max Energy: 19\nSpecialty: Repair & construction";
                icon = R.drawable.ic_engineer;
                break;
            case "Medic":
                info = "Skill: 7  Resilience: 2  Max Energy: 18\nSpecialty: Healing & recovery";
                icon = R.drawable.ic_medic;
                break;
            case "Scientist":
                info = "Skill: 8  Resilience: 1  Max Energy: 17\nSpecialty: Research & analysis";
                icon = R.drawable.ic_scientist;
                break;
            case "Soldier":
                info = "Skill: 9  Resilience: 0  Max Energy: 16\nSpecialty: Combat & defense";
                icon = R.drawable.ic_soldier;
                break;
            default:
                info = "";
                icon = R.drawable.ic_pilot;
                break;
        }

        binding.tvStatsPreview.setText(info);
        binding.ivSpecPreview.setImageResource(icon);
    }

    /**
     * Factory — returns the correct subclass instance for the chosen role.
     * Falls back to Pilot if something unexpected comes through.
     *
     * @param role  the selected role string
     * @param name  the name the user entered
     * @return a fully initialised CrewMember subclass
     */
    private CrewMember buildCrewMember(String role, String name) {
        switch (role) {
            case "Engineer":  return new Engineer(name);
            case "Medic":     return new Medic(name);
            case "Scientist": return new Scientist(name);
            case "Soldier":   return new Soldier(name);
            default:          return new Pilot(name);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
