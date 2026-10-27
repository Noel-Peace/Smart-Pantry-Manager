package com.smartpantry.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.smartpantry.app.logic.IngredientMatcher;
import com.smartpantry.app.logic.MatchResult;
import com.smartpantry.app.logic.UnitConverter;
import com.smartpantry.app.model.PantryItem;
import com.smartpantry.app.model.Recipe;
import com.smartpantry.app.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sanity checks on the pre-loaded recipe collection.
 */
public class SeedDataTest {

    @Test
    public void thereAreAtLeastFifteenRecipes() {
        assertTrue(SeedData.recipes().size() >= 15);
    }

    @Test
    public void recipeNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (Recipe r : SeedData.recipes()) {
            assertTrue("duplicate recipe: " + r.getName(), names.add(r.getName()));
        }
    }

    @Test
    public void everyRecipeIsWellFormed() {
        for (Recipe r : SeedData.recipes()) {
            assertNotNull(r.getName());
            assertFalse(r.getName().trim().isEmpty());
            assertTrue(r.getName() + " has no steps", r.getStepList().size() >= 2);
            assertTrue(r.getName() + " has no ingredients", r.getIngredients().size() >= 2);
            for (RecipeIngredient i : r.getIngredients()) {
                assertTrue(r.getName() + ": quantity must be > 0", i.getQuantity() > 0);
                assertNotNull(r.getName() + ": unknown unit " + i.getUnit(),
                        UnitConverter.canonicalUnit(i.getUnit()));
            }
        }
    }

    /** Owning exactly what a recipe needs must make that recipe appear in the suggestions. */
    @Test
    public void everyRecipeIsSuggestedWhenThePantryHoldsExactlyItsIngredients() {
        List<Recipe> all = SeedData.recipes();
        for (Recipe r : all) {
            List<PantryItem> pantry = new ArrayList<>();
            for (RecipeIngredient i : r.getIngredients()) {
                pantry.add(new PantryItem(i.getName(), i.getQuantity(), i.getUnit(), null));
            }
            boolean found = false;
            for (MatchResult m : IngredientMatcher.strictMatches(all, pantry)) {
                if (m.getRecipe().getName().equals(r.getName())) {
                    found = true;
                }
            }
            assertTrue(r.getName() + " should be suggested", found);
        }
    }

    /** Removing any ONE ingredient must remove that recipe from the suggestions. */
    @Test
    public void removingAnyOneIngredientHidesTheRecipe() {
        List<Recipe> all = SeedData.recipes();
        for (Recipe r : all) {
            for (int skip = 0; skip < r.getIngredients().size(); skip++) {
                List<PantryItem> pantry = new ArrayList<>();
                for (int k = 0; k < r.getIngredients().size(); k++) {
                    if (k == skip) {
                        continue;
                    }
                    RecipeIngredient i = r.getIngredients().get(k);
                    pantry.add(new PantryItem(i.getName(), i.getQuantity(), i.getUnit(), null));
                }
                for (MatchResult m : IngredientMatcher.strictMatches(all, pantry)) {
                    assertFalse(r.getName() + " must not be suggested without "
                                    + r.getIngredients().get(skip).getName(),
                            m.getRecipe().getName().equals(r.getName()));
                }
            }
        }
    }

    /** Having less than the required amount of any ONE ingredient hides the recipe too. */
    @Test
    public void halfTheQuantityOfAnyOneIngredientHidesTheRecipe() {
        List<Recipe> all = SeedData.recipes();
        for (Recipe r : all) {
            for (int low = 0; low < r.getIngredients().size(); low++) {
                List<PantryItem> pantry = new ArrayList<>();
                for (int k = 0; k < r.getIngredients().size(); k++) {
                    RecipeIngredient i = r.getIngredients().get(k);
                    double q = (k == low) ? i.getQuantity() / 2.0 : i.getQuantity();
                    pantry.add(new PantryItem(i.getName(), q, i.getUnit(), null));
                }
                for (MatchResult m : IngredientMatcher.strictMatches(all, pantry)) {
                    assertFalse(m.getRecipe().getName().equals(r.getName()));
                }
            }
        }
        assertEquals(true, all.size() > 0);
    }
}
