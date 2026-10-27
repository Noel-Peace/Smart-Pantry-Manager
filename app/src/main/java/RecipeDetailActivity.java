package com.smartpantry.app.ui;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.smartpantry.app.R;
import com.smartpantry.app.data.PantryDbHelper;
import com.smartpantry.app.logic.IngredientCheck;
import com.smartpantry.app.logic.IngredientMatcher;
import com.smartpantry.app.logic.MatchResult;
import com.smartpantry.app.logic.QuantityFormatter;
import com.smartpantry.app.model.Recipe;
import com.smartpantry.app.model.RecipeIngredient;

import java.util.List;

/**
 * Screen 4 - Recipe Detail.
 * Receives a recipe id through the Intent, loads that recipe from SQLite and shows the full
 * ingredient list (each line ticked or crossed against the pantry) and the method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.smartpantry.app.EXTRA_RECIPE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        PantryDbHelper db = PantryDbHelper.getInstance(this);
        Recipe recipe = recipeId == -1 ? null : db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(recipe.getName());
        MatchResult match = IngredientMatcher.evaluate(recipe, db.getAllPantryItems());

        ((TextView) findViewById(R.id.tv_detail_name)).setText(recipe.getName());
        showStatus(match);
        showIngredients(match);
        showSteps(recipe);
    }

    private void showStatus(MatchResult match) {
        TextView status = findViewById(R.id.tv_detail_status);
        if (match.isStrictMatch()) {
            status.setText(R.string.detail_can_cook);
            status.setTextColor(ContextCompat.getColor(this, R.color.ok_green));
        } else {
            status.setText(getString(R.string.detail_missing, match.getUnmetCount()));
            status.setTextColor(ContextCompat.getColor(this, R.color.warn_red));
        }
    }

    /** One line per ingredient: green tick when owned, red cross when missing / not enough. */
    private void showIngredients(MatchResult match) {
        int green = ContextCompat.getColor(this, R.color.ok_green);
        int red = ContextCompat.getColor(this, R.color.warn_red);
        SpannableStringBuilder text = new SpannableStringBuilder();

        List<IngredientCheck> checks = match.getChecks();
        for (int i = 0; i < checks.size(); i++) {
            IngredientCheck check = checks.get(i);
            RecipeIngredient ing = check.getIngredient();
            String description = QuantityFormatter.describe(ing.getQuantity(), ing.getUnit(), ing.getName());

            String line;
            int color;
            if (check.getStatus() == IngredientCheck.Status.SATISFIED) {
                line = "✓  " + description;
                color = green;
            } else if (check.getStatus() == IngredientCheck.Status.INSUFFICIENT) {
                line = "✗  " + description + "  " + getString(R.string.ing_have_short,
                        QuantityFormatter.withUnit(check.getAvailable(), ing.getUnit()));
                color = red;
            } else {
                line = "✗  " + description + "  " + getString(R.string.ing_not_in_pantry);
                color = red;
            }

            int start = text.length();
            text.append(line);
            text.setSpan(new ForegroundColorSpan(color), start, text.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (i < checks.size() - 1) {
                text.append('\n');
            }
        }
        ((TextView) findViewById(R.id.tv_detail_ingredients)).setText(text);
    }

    private void showSteps(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        List<String> steps = recipe.getStepList();
        for (int i = 0; i < steps.size(); i++) {
            sb.append(i + 1).append(". ").append(steps.get(i));
            if (i < steps.size() - 1) {
                sb.append("\n\n");
            }
        }
        ((TextView) findViewById(R.id.tv_detail_steps)).setText(sb.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
