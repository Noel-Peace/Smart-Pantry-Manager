package com.smartpantry.app.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartpantry.app.R;

/**
 * Shared behaviour of the three top-level screens (Pantry, Recipes, Settings):
 * the bottom navigation bar, which moves between screens with explicit Intents.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Wires up the bottom navigation bar of the current screen.
     *
     * @param selectedItemId the menu item that represents the screen we are on
     */
    protected void setupBottomNav(final int selectedItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already here
            }

            if (id == R.id.nav_pantry) {
                // The Pantry screen is the root: bring the existing one back and close the rest.
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                Intent intent;
                if (id == R.id.nav_recipes) {
                    intent = new Intent(this, SuggestedRecipesActivity.class);
                } else {
                    intent = new Intent(this, SettingsActivity.class);
                }
                startActivity(intent);
                if (selectedItemId != R.id.nav_pantry) {
                    finish(); // keep the back stack as: Pantry -> current top-level screen
                }
            }
            // false = do not move the highlight here; the new screen shows its own highlight
            return false;
        });
    }
}
