package com.smartpantry.app.logic;

import com.smartpantry.app.model.RecipeIngredient;

/**
 * The result of checking ONE recipe ingredient against the pantry.
 */
public class IngredientCheck {

    public enum Status {
        /** The pantry holds at least the required quantity. */
        SATISFIED,
        /** The ingredient is in the pantry but not enough of it (or the units cannot be compared). */
        INSUFFICIENT,
        /** The ingredient is not in the pantry at all. */
        MISSING
    }

    private final RecipeIngredient ingredient;
    private final Status status;
    /** How much the pantry holds, expressed in the recipe's unit (0 if not convertible). */
    private final double available;

    public IngredientCheck(RecipeIngredient ingredient, Status status, double available) {
        this.ingredient = ingredient;
        this.status = status;
        this.available = available;
    }

    public RecipeIngredient getIngredient() {
        return ingredient;
    }

    public Status getStatus() {
        return status;
    }

    public double getAvailable() {
        return available;
    }

    public boolean isSatisfied() {
        return status == Status.SATISFIED;
    }
}
