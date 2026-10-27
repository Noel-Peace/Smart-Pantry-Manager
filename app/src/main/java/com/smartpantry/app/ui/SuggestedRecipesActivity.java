package com.smartpantry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.smartpantry.app.R;
import com.smartpantry.app.data.PantryDbHelper;
import com.smartpantry.app.logic.IngredientMatcher;
import com.smartpantry.app.logic.MatchResult;
import com.smartpantry.app.model.PantryItem;
import com.smartpantry.app.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3 - Suggested Recipes.
 * Runs the strict-matching algorithm every time the screen is shown.
 * Tab 1 "Can cook now"  = the strict list (every ingredient owned in a sufficient quantity).
 * Tab 2 "Almost there"  = optional bonus list, clearly separate: recipes missing exactly ONE ingredient.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private PantryDbHelper db;
    private RecipeAdapter adapter;
    private MaterialButton btnReady;
    private MaterialButton btnAlmost;
    private TextView tvSummary;
    private TextView tvEmpty;

    private List<MatchResult> ready = new ArrayList<>();
    private List<MatchResult> almost = new ArrayList<>();
    private boolean showingAlmost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle(R.string.title_recipes);

        db = PantryDbHelper.getInstance(this);
        btnReady = findViewById(R.id.btn_tab_ready);
        btnAlmost = findViewById(R.id.btn_tab_almost);
        tvSummary = findViewById(R.id.tv_summary);
        tvEmpty = findViewById(R.id.tv_empty);

        RecyclerView recyclerView = findViewById(R.id.rv_recipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recyclerView.setAdapter(adapter);

        MaterialButtonToggleGroup toggle = findViewById(R.id.toggle_tabs);
        toggle.check(R.id.btn_tab_ready);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showingAlmost = (checkedId == R.id.btn_tab_almost);
                render();
            }
        });

        setupBottomNav(R.id.nav_recipes);
    }

    /** The pantry may have changed since we were last visible, so re-run the matching. */
    @Override
    protected void onResume() {
        super.onResume();
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> recipes = db.getAllRecipes();
        ready = IngredientMatcher.strictMatches(recipes, pantry);
        almost = IngredientMatcher.almostThere(recipes, pantry);
        render();
    }

    private void render() {
        btnReady.setText(getString(R.string.tab_ready, ready.size()));
        btnAlmost.setText(getString(R.string.tab_almost, almost.size()));

        List<MatchResult> shown = showingAlmost ? almost : ready;
        adapter.setMatches(shown, showingAlmost);

        tvSummary.setText(showingAlmost ? R.string.summary_almost : R.string.summary_ready);
        if (shown.isEmpty()) {
            // friendly feedback instead of a blank screen
            tvEmpty.setText(showingAlmost ? R.string.empty_almost : R.string.empty_ready);
            tvEmpty.setVisibility(View.VISIBLE);
            tvSummary.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            tvSummary.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecipeClick(MatchResult match) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, match.getRecipe().getId());
        startActivity(intent);
    }
}
