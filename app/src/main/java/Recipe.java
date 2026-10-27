package com.smartpantry.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe: name, the ingredients it requires and the preparation steps.
 * Steps are stored as one text block with one step per line.
 */
public class Recipe {

    private final long id;
    private final String name;
    private final String steps;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients == null
                ? new ArrayList<RecipeIngredient>()
                : ingredients;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** The raw steps text (one step per line). */
    public String getSteps() {
        return steps;
    }

    /** The steps split into a list, ignoring blank lines. */
    public List<String> getStepList() {
        List<String> result = new ArrayList<>();
        if (steps == null) {
            return result;
        }
        for (String line : steps.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}
