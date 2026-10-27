package com.smartpantry.app.ui;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppPrefs;
import com.smartpantry.app.data.PantryDbHelper;

/**
 * Screen 5 - Settings.
 * - toggle for expiring-soon alerts (SharedPreferences)
 * - preferred unit system (SharedPreferences), used as the default unit in the Add form
 * - button to clear the whole pantry (handy for demos)
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.title_settings);

        SwitchMaterial switchAlerts = findViewById(R.id.switch_alerts);
        RadioGroup rgUnits = findViewById(R.id.rg_units);
        RadioButton rbMetric = findViewById(R.id.rb_metric);
        RadioButton rbImperial = findViewById(R.id.rb_imperial);
        MaterialButton btnClear = findViewById(R.id.btn_clear_pantry);

        // show the saved values first, THEN attach listeners (so loading does not trigger them)
        switchAlerts.setChecked(AppPrefs.isExpiryAlertsEnabled(this));
        if (AppPrefs.UNITS_IMPERIAL.equals(AppPrefs.getUnitSystem(this))) {
            rbImperial.setChecked(true);
        } else {
            rbMetric.setChecked(true);
        }

        switchAlerts.setOnCheckedChangeListener((button, isChecked) ->
                AppPrefs.setExpiryAlertsEnabled(this, isChecked));

        rgUnits.setOnCheckedChangeListener((group, checkedId) -> {
            String system = checkedId == R.id.rb_imperial
                    ? AppPrefs.UNITS_IMPERIAL : AppPrefs.UNITS_METRIC;
            AppPrefs.setUnitSystem(this, system);
        });

        btnClear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.settings_clear_title)
                .setMessage(R.string.settings_clear_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    PantryDbHelper.getInstance(this).deleteAllPantryItems();
                    Toast.makeText(this, R.string.settings_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show());

        setupBottomNav(R.id.nav_settings);
    }
}
