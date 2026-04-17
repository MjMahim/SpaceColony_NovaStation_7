package com.spacecolony;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.spacecolony.databinding.ActivityMainBinding;
import com.spacecolony.model.StorageManager;

/**
 * The single Activity that hosts the whole app.
 * This project uses a single-Activity architecture with the Navigation
 * Component.  All screens are Fragments; this Activity just holds the
 * NavHostFragment and the bottom navigation bar and wires them together.
 * On first launch, StorageManager restores any previously saved colony
 * data from the JSON file.  The same data is saved again whenever the
 * Activity stops (i.e. the user leaves the app or the screen goes off).
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Restore saved colony data before any fragment touches Storage
        StorageManager.loadFromFile(this);

        // Find the NavController inside the NavHostFragment
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHost.getNavController();

        // Wire the bottom bar to the nav controller so tapping tabs navigates automatically
        BottomNavigationView bottomNav = binding.bottomNavigation;
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Persist the colony state every time the user leaves or minimizes the app
        StorageManager.saveToFile(this);
    }
}
