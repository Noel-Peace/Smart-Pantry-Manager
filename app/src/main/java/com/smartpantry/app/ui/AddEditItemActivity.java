package com.smartpantry.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppPrefs;
import com.smartpantry.app.data.PantryDbHelper;
import com.smartpantry.app.logic.ExpiryUtils;
import com.smartpantry.app.logic.QuantityFormatter;
import com.smartpantry.app.logic.UnitConverter;
import com.smartpantry.app.model.PantryItem;

import java.util.Calendar;

/**
 * Screen 2 - Add / Edit Ingredient.
 * Opened without extras = add mode (CREATE).
 * Opened with EXTRA_ITEM_ID = edit mode (UPDATE) with a Delete button (DELETE).
 * All input is validated before anything is written to the database.
 */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.smartpantry.app.EXTRA_ITEM_ID";

    private static final int MAX_NAME_LENGTH = 50;
    private static final double MAX_QUANTITY = 100000;

    private PantryDbHelper db;
    private EditText etName;
    private EditText etQuantity;
    private EditText etExpiry;
    private Spinner spUnit;
    private MaterialButton btnDelete;

    /** -1 when adding a new item. */
    private long itemId = -1;
    private PantryItem editingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = PantryDbHelper.getInstance(this);
        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etExpiry = findViewById(R.id.et_expiry);
        spUnit = findViewById(R.id.sp_unit);
        btnDelete = findViewById(R.id.btn_delete);
        MaterialButton btnSave = findViewById(R.id.btn_save);
        MaterialButton btnClearDate = findViewById(R.id.btn_clear_date);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UnitConverter.SPINNER_UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = db.getPantryItem(itemId);
            if (editingItem == null) {
                Toast.makeText(this, R.string.toast_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            setTitle(R.string.title_edit_item);
            etName.setText(editingItem.getName());
            etQuantity.setText(QuantityFormatter.format(editingItem.getQuantity()));
            selectUnit(editingItem.getUnit());
            if (editingItem.getExpiryDate() != null) {
                etExpiry.setText(editingItem.getExpiryDate());
            }
            btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            setTitle(R.string.title_add_item);
            // the Settings screen decides the default unit for new items
            boolean imperial = AppPrefs.UNITS_IMPERIAL.equals(AppPrefs.getUnitSystem(this));
            selectUnit(imperial ? "oz" : "g");
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> etExpiry.setText(""));
        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void selectUnit(String unit) {
        String canonical = UnitConverter.canonicalUnit(unit);
        for (int i = 0; i < UnitConverter.SPINNER_UNITS.length; i++) {
            if (UnitConverter.SPINNER_UNITS[i].equals(canonical)) {
                spUnit.setSelection(i);
                return;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String current = etExpiry.getText().toString().trim();
        if (ExpiryUtils.isValidDate(current)) {
            String[] parts = current.split("-");
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2]));
        }
        new DatePickerDialog(this,
                (view, year, month, day) -> etExpiry.setText(ExpiryUtils.format(year, month, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /** Validates the form; on success writes to the database and closes the screen. */
    private void saveItem() {
        etName.setError(null);
        etQuantity.setError(null);

        // ---- name
        String name = etName.getText().toString().trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            fail(etName, R.string.err_name_required);
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            fail(etName, R.string.err_name_too_long);
            return;
        }
        if (!name.matches(".*\\p{L}.*")) {
            fail(etName, R.string.err_name_letters);
            return;
        }

        // ---- quantity (some phones use a comma as the decimal separator)
        String qtyText = etQuantity.getText().toString().trim().replace(',', '.');
        if (qtyText.isEmpty()) {
            fail(etQuantity, R.string.err_qty_required);
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            fail(etQuantity, R.string.err_qty_invalid);
            return;
        }
        if (Double.isNaN(quantity) || quantity <= 0) {
            fail(etQuantity, R.string.err_qty_positive);
            return;
        }
        if (quantity > MAX_QUANTITY) {
            fail(etQuantity, R.string.err_qty_too_large);
            return;
        }

        // ---- expiry (optional)
        String expiry = etExpiry.getText().toString().trim();
        if (!expiry.isEmpty() && !ExpiryUtils.isValidDate(expiry)) {
            fail(etExpiry, R.string.err_expiry_invalid);
            return;
        }

        String unit = (String) spUnit.getSelectedItem();
        PantryItem item = new PantryItem(itemId, name, quantity, unit, expiry.isEmpty() ? null : expiry);

        if (itemId == -1) {
            long newId = db.addPantryItem(item);
            if (newId == -1) {
                Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, getString(R.string.toast_added, name), Toast.LENGTH_SHORT).show();
        } else {
            int rows = db.updatePantryItem(item);
            if (rows != 1) {
                Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, getString(R.string.toast_updated, name), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void fail(EditText field, int messageRes) {
        field.setError(getString(messageRes));
        field.requestFocus();
    }

    private void confirmDelete() {
        if (editingItem == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, editingItem.getName()))
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    db.deletePantryItem(editingItem.getId());
                    Toast.makeText(this, getString(R.string.toast_deleted, editingItem.getName()),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
