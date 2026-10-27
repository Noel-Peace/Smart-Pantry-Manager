package com.smartpantry.app.logic;

import com.smartpantry.app.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * How well one recipe matches the current pantry.
 */
public class MatchResult {

    private final Recipe recipe;
    private final List<IngredientCheck> checks;

    public MatchResult(Recipe recipe, List<IngredientCheck> checks) {
        this.recipe = recipe;
        this.checks = checks;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<IngredientCheck> getChecks() {
        return checks;
    }

    /** Number of ingredients that are missing or not available in a sufficient quantity. */
    public int getUnmetCount() {
        int count = 0;
        for (IngredientCheck c : checks) {
            if (!c.isSatisfied()) {
                count++;
            }
        }
        return count;
    }

    /**
     * THE strict rule: a recipe qualifies only if it has at least one ingredient and
     * EVERY ingredient is present in at least the required quantity.
     */
    public boolean isStrictMatch() {
        return !checks.isEmpty() && getUnmetCount() == 0;
    }

    /** "Almost there": exactly one ingredient is missing / short. Never counts as a strict match. */
    public boolean isAlmostThere() {
        return getUnmetCount() == 1;
    }

    /** Human readable list of what is missing, e.g. "garlic (need 2 cloves, have 1)". */
    public String describeUnmet() {
        List<String> parts = new ArrayList<>();
        for (IngredientCheck c : checks) {
            if (c.isSatisfied()) {
                continue;
            }
            String need = QuantityFormatter.withUnit(
                    c.getIngredient().getQuantity(), c.getIngredient().getUnit());
            if (c.getStatus() == IngredientCheck.Status.INSUFFICIENT) {
                String have = QuantityFormatter.withUnit(c.getAvailable(), c.getIngredient().getUnit());
                parts.add(c.getIngredient().getName() + " (need " + need + ", have " + have + ")");
            } else {
                parts.add(c.getIngredient().getName() + " (need " + need + ")");
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
