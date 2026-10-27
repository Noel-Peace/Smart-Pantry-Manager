package com.smartpantry.app.logic;

import com.smartpantry.app.model.PantryItem;
import com.smartpantry.app.model.Recipe;
import com.smartpantry.app.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * THE STRICT-MATCHING ALGORITHM (assignment section 2.3).
 *
 * <p>A recipe is "suggested" only if EVERY ingredient it needs is in the pantry in at least the
 * required quantity. One missing or short ingredient and the recipe is excluded.</p>
 *
 * <p>Robustness:</p>
 * <ul>
 *   <li>names are compared after {@link NameNormalizer#normalize} (tomato == Tomatoes)</li>
 *   <li>quantities are compared after {@link UnitConverter#convert} (1 kg satisfies 500 g)</li>
 *   <li>several pantry rows of the same ingredient are added together (2 + 3 eggs = 5 eggs)</li>
 *   <li>pantry rows with a quantity of 0 or less are ignored</li>
 * </ul>
 *
 * This class has no Android dependencies, so it is unit-tested on the plain JVM.
 */
public final class IngredientMatcher {

    private IngredientMatcher() {
    }

    /** Tolerance for floating point rounding only (not a "close enough" allowance). */
    private static final double EPSILON = 1e-9;

    /** Checks a single recipe ingredient against the whole pantry. */
    public static IngredientCheck checkIngredient(RecipeIngredient needed, List<PantryItem> pantry) {
        String neededName = NameNormalizer.normalize(needed.getName());

        boolean foundByName = false;
        double available = 0;

        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue; // an item with nothing left is not "in the pantry"
            }
            if (neededName.isEmpty() || !neededName.equals(NameNormalizer.normalize(item.getName()))) {
                continue;
            }
            foundByName = true;
            Double converted = UnitConverter.convert(
                    item.getQuantity(), item.getUnit(), needed.getUnit(), neededName);
            if (converted != null) {
                available += converted;
            }
        }

        if (!foundByName) {
            return new IngredientCheck(needed, IngredientCheck.Status.MISSING, 0);
        }
        if (available + EPSILON >= needed.getQuantity()) {
            return new IngredientCheck(needed, IngredientCheck.Status.SATISFIED, available);
        }
        return new IngredientCheck(needed, IngredientCheck.Status.INSUFFICIENT, available);
    }

    /** Checks every ingredient of one recipe. */
    public static MatchResult evaluate(Recipe recipe, List<PantryItem> pantry) {
        List<IngredientCheck> checks = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            checks.add(checkIngredient(ingredient, pantry));
        }
        return new MatchResult(recipe, checks);
    }

    /** The "Suggested Recipes" list: only recipes whose ingredients are ALL available. */
    public static List<MatchResult> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult match = evaluate(recipe, pantry);
            if (match.isStrictMatch()) {
                result.add(match);
            }
        }
        return result;
    }

    /** Optional "Almost There" list: recipes missing exactly ONE ingredient (never in the strict list). */
    public static List<MatchResult> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult match = evaluate(recipe, pantry);
            if (match.isAlmostThere()) {
                result.add(match);
            }
        }
        return result;
    }
}
