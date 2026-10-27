package com.smartpantry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppPrefs;
import com.smartpantry.app.data.PantryDbHelper;
import com.smartpantry.app.logic.ExpiryUtils;
import com.smartpantry.app.model.PantryItem;

import java.util.Calendar;
import java.util.List;

/**
 * Screen 1 - the Pantry List (launcher screen).
 * READ: shows every pantry item from SQLite in a RecyclerView.
 * Tap a row to edit it (UPDATE), tap Delete to remove it (DELETE), tap + to add one (CREATE).
 */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private PantryDbHelper db;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private TextView bannerExpiring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_pantry);

        db = PantryDbHelper.getInstance(this);
        tvEmpty = findViewById(R.id.tv_empty);
        bannerExpiring = findViewById(R.id.banner_expiring);

        RecyclerView recyclerView = findViewById(R.id.rv_pantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    /** Reload every time the screen comes back (after adding / editing / deleting elsewhere). */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        boolean alerts = AppPrefs.isExpiryAlertsEnabled(this);
        adapter.setItems(items, alerts);

        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int expiring = 0;
        if (alerts) {
            Calendar today = Calendar.getInstance();
            for (PantryItem item : items) {
                Integer days = ExpiryUtils.daysUntil(item.getExpiryDate(), today);
                if (days != null && days <= ExpiryUtils.EXPIRING_SOON_DAYS) {
                    expiring++;
                }
            }
        }
        if (expiring > 0) {
            bannerExpiring.setText(getString(R.string.banner_expiring, expiring));
            bannerExpiring.setVisibility(View.VISIBLE);
        } else {
            bannerExpiring.setVisibility(View.GONE);
        }
    }

    // ---- PantryAdapter.Listener

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId()); // data passed with the Intent
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, item.getName()))
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, getString(R.string.toast_deleted, item.getName()),
                            Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }
}
