package com.smartpantry.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.smartpantry.app.data.SeedData;
import com.smartpantry.app.model.PantryItem;
import com.smartpantry.app.model.Recipe;
import com.smartpantry.app.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the strict-matching rule from assignment section 2.3.
 */
public class IngredientMatcherTest {

    private static PantryItem pantry(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    /** A recipe that needs exactly five ingredients. */
    private static Recipe fiveIngredientRecipe() {
        return new Recipe(1, "Five Things", "Mix.", Arrays.asList(
                need("eggs", 2, "piece"),
                need("flour", 200, "g"),
                need("milk", 300, "ml"),
                need("sugar", 20, "g"),
                need("butter", 20, "g")));
    }

    private static List<PantryItem> fullPantry() {
        return new ArrayList<>(Arrays.asList(
                pantry("Eggs", 6, "piece"),
                pantry("Flour", 1, "kg"),
                pantry("Milk", 1, "l"),
                pantry("Sugar", 500, "g"),
                pantry("Butter", 250, "g")));
    }

    // ---- the headline rule: 5 needed, 4 owned => NOT suggested

    @Test
    public void recipeWithAllFiveIngredientsIsSuggested() {
        List<MatchResult> matches = IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), fullPantry());
        assertEquals(1, matches.size());
    }

    @Test
    public void recipeNeedingFiveWithOnlyFourOwnedIsNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4); // remove the butter
        List<MatchResult> matches = IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry);
        assertTrue("a partial match must never be suggested", matches.isEmpty());
    }

    @Test
    public void addingTheMissingIngredientMakesTheRecipeAppear() {
        List<PantryItem> pantry = fullPantry();
        PantryItem butter = pantry.remove(4);
        Recipe recipe = fiveIngredientRecipe();
        assertEquals(0, IngredientMatcher.strictMatches(Arrays.asList(recipe), pantry).size());
        pantry.add(butter);
        assertEquals(1, IngredientMatcher.strictMatches(Arrays.asList(recipe), pantry).size());
    }

    // ---- quantity

    @Test
    public void notEnoughQuantityIsNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, pantry("eggs", 1, "piece")); // need 2
        assertTrue(IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).isEmpty());
    }

    @Test
    public void exactlyTheRequiredQuantityIsEnough() {
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                pantry("eggs", 2, "piece"), pantry("flour", 200, "g"), pantry("milk", 300, "ml"),
                pantry("sugar", 20, "g"), pantry("butter", 20, "g")));
        assertEquals(1, IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).size());
    }

    @Test
    public void justBelowTheRequiredQuantityIsNotEnough() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(3, pantry("sugar", 19.9, "g")); // need 20 g
        assertTrue(IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).isEmpty());
    }

    @Test
    public void severalRowsOfTheSameIngredientAreAddedTogether() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, pantry("eggs", 1, "piece"));
        pantry.add(pantry("Eggs", 1, "piece")); // 1 + 1 = 2
        assertEquals(1, IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).size());
    }

    @Test
    public void itemsWithZeroQuantityAreIgnored() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(4, pantry("butter", 0, "g"));
        assertTrue(IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).isEmpty());
    }

    // ---- messiness: names and units

    @Test
    public void singularPluralAndCaseDifferencesDoNotBreakMatching() {
        Recipe recipe = new Recipe(2, "Tomato thing", "Cook.",
                Arrays.asList(need("tomatoes", 4, "piece")));
        assertEquals(1, IngredientMatcher.strictMatches(Arrays.asList(recipe),
                Arrays.asList(pantry("TOMATO", 4, "piece"))).size());

        Recipe recipe2 = new Recipe(3, "Tomato thing 2", "Cook.",
                Arrays.asList(need("tomato", 4, "piece")));
        assertEquals(1, IngredientMatcher.strictMatches(Arrays.asList(recipe2),
                Arrays.asList(pantry("Tomatoes", 5, "piece"))).size());
    }

    @Test
    public void unitDifferencesDoNotBreakMatching() {
        // pantry in kg / l / cups / grams, recipe in g / ml / g / piece
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                pantry("eggs", 3, "pieces"),
                pantry("flour", 1.5, "cups"),   // 1.5 * 240 * 0.53 = 190.8 g  (NOT enough for 200 g)
                pantry("milk", 0.5, "l"),
                pantry("sugar", 0.1, "kg"),
                pantry("butter", 4, "tbsp")));  // 4 * 15 * 0.95 = 57 g
        assertTrue("190.8 g of flour is less than 200 g", IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).isEmpty());

        pantry.set(1, pantry("flour", 2, "cups")); // 254.4 g
        assertEquals(1, IngredientMatcher.strictMatches(
                Arrays.asList(fiveIngredientRecipe()), pantry).size());
    }

    @Test
    public void weightInPantryCanSatisfyPieceCountInRecipe() {
        Recipe recipe = new Recipe(4, "Tomatoes", "Cook.", Arrays.asList(need("tomatoes", 4, "piece")));
        // 500 g / 120 g per tomato = 4.17 tomatoes
        assertEquals(1, IngredientMatcher.strictMatches(Arrays.asList(recipe),
                Arrays.asList(pantry("tomato", 500, "g"))).size());
        // 400 g = 3.33 tomatoes -> not enough
        assertTrue(IngredientMatcher.strictMatches(Arrays.asList(recipe),
                Arrays.asList(pantry("tomato", 400, "g"))).isEmpty());
    }

    @Test
    public void unitsThatCannotBeComparedAreNotGuessedInTheUsersFavour() {
        Recipe recipe = new Recipe(5, "Tuna", "Mix.", Arrays.asList(need("tuna", 1, "can")));
        assertTrue(IngredientMatcher.strictMatches(Arrays.asList(recipe),
                Arrays.asList(pantry("tuna", 500, "g"))).isEmpty());
    }

    // ---- edge cases

    @Test
    public void emptyPantryMatchesNothing() {
        assertTrue(IngredientMatcher.strictMatches(SeedData.recipes(),
                new ArrayList<PantryItem>()).isEmpty());
    }

    @Test
    public void recipeWithNoIngredientsIsNeverSuggested() {
        Recipe empty = new Recipe(6, "Nothing", "Do nothing.", new ArrayList<RecipeIngredient>());
        assertTrue(IngredientMatcher.strictMatches(Arrays.asList(empty), fullPantry()).isEmpty());
    }

    // ---- "Almost there" (bonus) must stay separate from the strict list

    @Test
    public void almostThereHoldsRecipesMissingExactlyOneIngredient() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4); // butter missing
        Recipe recipe = fiveIngredientRecipe();

        assertTrue(IngredientMatcher.strictMatches(Arrays.asList(recipe), pantry).isEmpty());
        List<MatchResult> almost = IngredientMatcher.almostThere(Arrays.asList(recipe), pantry);
        assertEquals(1, almost.size());
        assertTrue(almost.get(0).describeUnmet().startsWith("butter"));
    }

    @Test
    public void recipeMissingTwoIngredientsIsNeitherStrictNorAlmostThere() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4);
        pantry.remove(3);
        Recipe recipe = fiveIngredientRecipe();
        assertTrue(IngredientMatcher.strictMatches(Arrays.asList(recipe), pantry).isEmpty());
        assertTrue(IngredientMatcher.almostThere(Arrays.asList(recipe), pantry).isEmpty());
    }

    @Test
    public void aRecipeCanNeverBeInBothLists() {
        for (Recipe recipe : SeedData.recipes()) {
            for (int owned = 0; owned <= recipe.getIngredients().size(); owned++) {
                List<PantryItem> pantry = new ArrayList<>();
                for (int i = 0; i < owned; i++) {
                    RecipeIngredient ing = recipe.getIngredients().get(i);
                    pantry.add(pantry(ing.getName(), ing.getQuantity(), ing.getUnit()));
                }
                MatchResult m = IngredientMatcher.evaluate(recipe, pantry);
                assertFalse(m.isStrictMatch() && m.isAlmostThere());
            }
        }
    }

    @Test
    public void describeUnmetExplainsShortages() {
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(pantry("garlic", 1, "clove")));
        Recipe recipe = new Recipe(7, "Garlic", "Cook.", Arrays.asList(need("garlic", 2, "clove")));
        MatchResult m = IngredientMatcher.evaluate(recipe, pantry);
        assertEquals("garlic (need 2 cloves, have 1 clove)", m.describeUnmet());
    }
}
