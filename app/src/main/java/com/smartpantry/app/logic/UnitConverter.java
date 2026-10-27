package com.smartpantry.app.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities between units so that "1 kg" in the pantry satisfies "500 g" in a recipe.
 *
 * <ul>
 *   <li>Same dimension (mass, volume, count): plain conversion factor.</li>
 *   <li>Mass &lt;-&gt; volume: only for ingredients with a known density (flour, milk, oil...).</li>
 *   <li>Count &lt;-&gt; mass: only for ingredients with a known average piece weight (egg, onion...).</li>
 *   <li>Anything else cannot be compared, so {@link #convert} returns null and the matcher treats
 *       the pantry stock as not sufficient (the strict rule never guesses in the user's favour).</li>
 * </ul>
 */
public final class UnitConverter {

    private UnitConverter() {
    }

    public enum Dimension { MASS, VOLUME, COUNT }

    /** The units offered in the Add/Edit spinner (all canonical names). */
    public static final String[] SPINNER_UNITS = {
            "g", "kg", "oz", "lb", "ml", "l", "tsp", "tbsp", "cup",
            "piece", "clove", "slice", "can", "packet", "bunch", "pinch"
    };

    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Map<String, Dimension> DIMENSIONS = new HashMap<>();
    /** Factor to the base unit of the dimension: grams, millilitres or 1 piece. */
    private static final Map<String, Double> FACTORS = new HashMap<>();
    /** Grams per millilitre. Keys are normalised ingredient names. */
    private static final Map<String, Double> DENSITY = new HashMap<>();
    /** Average grams per piece. Keys are normalised ingredient names. */
    private static final Map<String, Double> GRAMS_PER_PIECE = new HashMap<>();

    static {
        // --- mass
        unit("g", Dimension.MASS, 1.0, "g", "gram", "grams", "gm", "gms");
        unit("kg", Dimension.MASS, 1000.0, "kg", "kgs", "kilogram", "kilograms", "kilo", "kilos");
        unit("oz", Dimension.MASS, 28.35, "oz", "ounce", "ounces");
        unit("lb", Dimension.MASS, 453.6, "lb", "lbs", "pound", "pounds");
        // --- volume (metric kitchen measures)
        unit("ml", Dimension.VOLUME, 1.0, "ml", "milliliter", "milliliters", "millilitre", "millilitres");
        unit("l", Dimension.VOLUME, 1000.0, "l", "liter", "liters", "litre", "litres", "ltr");
        unit("tsp", Dimension.VOLUME, 5.0, "tsp", "tsps", "teaspoon", "teaspoons");
        unit("tbsp", Dimension.VOLUME, 15.0, "tbsp", "tbsps", "tbs", "tablespoon", "tablespoons");
        unit("cup", Dimension.VOLUME, 240.0, "cup", "cups");
        unit("fl oz", Dimension.VOLUME, 30.0, "fl oz", "floz", "fluid ounce", "fluid ounces");
        // --- count-like units (all count as "1 each")
        unit("piece", Dimension.COUNT, 1.0, "", "piece", "pieces", "pc", "pcs", "unit", "units",
                "whole", "item", "items", "each");
        unit("clove", Dimension.COUNT, 1.0, "clove", "cloves");
        unit("slice", Dimension.COUNT, 1.0, "slice", "slices");
        unit("can", Dimension.COUNT, 1.0, "can", "cans", "tin", "tins");
        unit("packet", Dimension.COUNT, 1.0, "packet", "packets", "pack", "packs");
        unit("bunch", Dimension.COUNT, 1.0, "bunch", "bunches");
        unit("pinch", Dimension.COUNT, 1.0, "pinch", "pinches");

        // --- densities (g per ml)
        DENSITY.put("flour", 0.53);
        DENSITY.put("sugar", 0.83);
        DENSITY.put("butter", 0.95);
        DENSITY.put("rice", 0.80);
        DENSITY.put("oat", 0.38);
        DENSITY.put("milk", 1.03);
        DENSITY.put("water", 1.0);
        DENSITY.put("oil", 0.92);
        DENSITY.put("olive oil", 0.92);
        DENSITY.put("honey", 1.42);
        DENSITY.put("salt", 1.2);
        DENSITY.put("soy sauce", 1.15);
        DENSITY.put("vegetable stock", 1.0);
        DENSITY.put("yogurt", 1.03);
        DENSITY.put("mayonnaise", 0.95);
        DENSITY.put("peanut butter", 1.07);
        DENSITY.put("lentil", 0.80);
        DENSITY.put("curry powder", 0.50);

        // --- average piece weights (grams)
        GRAMS_PER_PIECE.put("egg", 50.0);
        GRAMS_PER_PIECE.put("onion", 150.0);
        GRAMS_PER_PIECE.put("potato", 170.0);
        GRAMS_PER_PIECE.put("tomato", 120.0);
        GRAMS_PER_PIECE.put("carrot", 70.0);
        GRAMS_PER_PIECE.put("banana", 120.0);
        GRAMS_PER_PIECE.put("lemon", 60.0);
        GRAMS_PER_PIECE.put("avocado", 170.0);
        GRAMS_PER_PIECE.put("bell pepper", 150.0);
        GRAMS_PER_PIECE.put("garlic", 5.0);
        GRAMS_PER_PIECE.put("bread", 30.0);
        GRAMS_PER_PIECE.put("apple", 180.0);
        GRAMS_PER_PIECE.put("mushroom", 20.0);
        GRAMS_PER_PIECE.put("chicken breast", 200.0);
    }

    private static void unit(String canonical, Dimension dim, double factor, String... names) {
        DIMENSIONS.put(canonical, dim);
        FACTORS.put(canonical, factor);
        for (String n : names) {
            ALIASES.put(n, canonical);
        }
    }

    /**
     * Maps what the user typed ("Grams", "TBSP.", "cloves") to a canonical unit name,
     * or null if the unit is not recognised. A blank unit means "piece".
     */
    public static String canonicalUnit(String raw) {
        if (raw == null) {
            return "piece";
        }
        String key = raw.toLowerCase(Locale.ROOT).replace(".", "").trim().replaceAll("\\s+", " ");
        return ALIASES.get(key);
    }

    public static Dimension dimensionOf(String canonicalUnit) {
        return DIMENSIONS.get(canonicalUnit);
    }

    /**
     * Converts {@code quantity} from {@code fromUnit} to {@code toUnit}.
     *
     * @param normalizedName the normalised ingredient name (needed for density / piece weight)
     * @return the converted quantity, or null if the two units cannot be compared
     */
    public static Double convert(double quantity, String fromUnit, String toUnit, String normalizedName) {
        String from = canonicalUnit(fromUnit);
        String to = canonicalUnit(toUnit);
        if (from == null || to == null) {
            return null;
        }
        if (from.equals(to)) {
            return quantity;
        }
        Dimension dFrom = DIMENSIONS.get(from);
        Dimension dTo = DIMENSIONS.get(to);
        double fFrom = FACTORS.get(from);
        double fTo = FACTORS.get(to);

        if (dFrom == dTo) {
            return quantity * fFrom / fTo;
        }

        double base = quantity * fFrom; // grams, millilitres or pieces

        if (dFrom == Dimension.MASS && dTo == Dimension.VOLUME) {
            Double density = DENSITY.get(normalizedName);
            return density == null ? null : (base / density) / fTo;
        }
        if (dFrom == Dimension.VOLUME && dTo == Dimension.MASS) {
            Double density = DENSITY.get(normalizedName);
            return density == null ? null : (base * density) / fTo;
        }
        if (dFrom == Dimension.COUNT && dTo == Dimension.MASS) {
            Double gpp = GRAMS_PER_PIECE.get(normalizedName);
            return gpp == null ? null : (base * gpp) / fTo;
        }
        if (dFrom == Dimension.MASS && dTo == Dimension.COUNT) {
            Double gpp = GRAMS_PER_PIECE.get(normalizedName);
            return gpp == null ? null : (base / gpp) / fTo;
        }
        return null; // e.g. count <-> volume
    }
}
