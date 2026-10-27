package com.smartpantry.app.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns free-text ingredient names into a canonical form so that simple real-world
 * differences do not break matching:
 * <ul>
 *   <li>case and spacing: "  TOMATOES " == "tomatoes"</li>
 *   <li>singular / plural: "tomato" == "tomatoes", "berry" == "berries", "potatoes" == "potato"</li>
 *   <li>filler words: "fresh large eggs" == "egg"</li>
 *   <li>a small synonym list: "scallion" == "green onion", "yoghurt" == "yogurt"</li>
 * </ul>
 * This is deliberately a small rule-based normaliser, not full NLP.
 */
public final class NameNormalizer {

    private NameNormalizer() {
    }

    /** Words that describe preparation / size and do not change which ingredient it is. */
    private static final Set<String> FILLER = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "ripe", "chopped", "diced", "sliced",
            "minced", "grated", "crushed", "extra", "virgin", "whole", "finely", "raw",
            "boneless", "skinless", "canned", "tinned"));

    /** Plurals that do not follow the normal rules. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Plurals ending in "ies" whose singular ends in "ie" (so we must not turn them into "y"). */
    private static final Set<String> IE_PLURALS = new HashSet<>(Arrays.asList(
            "cookies", "brownies", "smoothies", "veggies", "calories", "movies"));

    /** Single-word spelling variants (applied after singularising). */
    private static final Map<String, String> SPELLING = new HashMap<>();

    /** Whole-phrase synonyms (keys are already singular / normalised). */
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("knives", "knife");
        IRREGULAR.put("chilies", "chili");
        IRREGULAR.put("chillies", "chili");

        SPELLING.put("yoghurt", "yogurt");
        SPELLING.put("chilli", "chili");
        SPELLING.put("mayo", "mayonnaise");
        SPELLING.put("coriander", "cilantro");

        ALIASES.put("mince", "ground beef");
        ALIASES.put("beef mince", "ground beef");
        ALIASES.put("minced beef", "ground beef");
        ALIASES.put("mince beef", "ground beef");
        ALIASES.put("green onion", "spring onion");
        ALIASES.put("scallion", "spring onion");
        ALIASES.put("capsicum", "bell pepper");
        ALIASES.put("aubergine", "eggplant");
        ALIASES.put("courgette", "zucchini");
        ALIASES.put("garbanzo bean", "chickpea");
        ALIASES.put("garbanzo", "chickpea");
        ALIASES.put("prawn", "shrimp");
        ALIASES.put("caster sugar", "sugar");
        ALIASES.put("granulated sugar", "sugar");
        ALIASES.put("white sugar", "sugar");
        ALIASES.put("plain flour", "flour");
        ALIASES.put("all purpose flour", "flour");
        ALIASES.put("bicarbonate of soda", "baking soda");
        ALIASES.put("red onion", "onion");
        ALIASES.put("white onion", "onion");
        ALIASES.put("yellow onion", "onion");
        ALIASES.put("brown onion", "onion");
        ALIASES.put("cherry tomato", "tomato");
        ALIASES.put("roma tomato", "tomato");
        ALIASES.put("plum tomato", "tomato");
        ALIASES.put("full cream milk", "milk");
        ALIASES.put("skim milk", "milk");
        ALIASES.put("skimmed milk", "milk");
        ALIASES.put("low fat milk", "milk");
        ALIASES.put("cheddar cheese", "cheese");
        ALIASES.put("cheddar", "cheese");
        ALIASES.put("chicken fillet", "chicken breast");
        ALIASES.put("garlic clove", "garlic");
        ALIASES.put("vegetable oil", "oil");
        ALIASES.put("sunflower oil", "oil");
        ALIASES.put("cooking oil", "oil");
        ALIASES.put("canola oil", "oil");
    }

    /**
     * Returns the canonical form of an ingredient name (never null; empty string if the
     * input has no usable letters).
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.toLowerCase(Locale.ROOT);

        // "tomatoes, diced"  ->  "tomatoes"
        int comma = s.indexOf(',');
        if (comma >= 0) {
            s = s.substring(0, comma);
        }
        // remove anything in brackets, then everything that is not a letter or space
        s = s.replaceAll("\\([^)]*\\)", " ");
        s = s.replaceAll("[^\\p{L}\\s]", " ");
        s = s.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) {
            return "";
        }

        // singularise every word, fix spelling variants
        String[] words = s.split(" ");
        StringBuilder phrase = new StringBuilder();
        for (String word : words) {
            String w = singularize(word);
            String spelled = SPELLING.get(w);
            if (spelled != null) {
                w = spelled;
            }
            if (phrase.length() > 0) {
                phrase.append(' ');
            }
            phrase.append(w);
        }

        String result = alias(phrase.toString());
        result = removeFillers(result);
        return alias(result);
    }

    /** True when two ingredient names refer to the same ingredient. */
    public static boolean sameIngredient(String a, String b) {
        String na = normalize(a);
        return !na.isEmpty() && na.equals(normalize(b));
    }

    private static String alias(String phrase) {
        String mapped = ALIASES.get(phrase);
        return mapped != null ? mapped : phrase;
    }

    private static String removeFillers(String phrase) {
        String[] words = phrase.split(" ");
        StringBuilder kept = new StringBuilder();
        for (String w : words) {
            if (FILLER.contains(w)) {
                continue;
            }
            if (kept.length() > 0) {
                kept.append(' ');
            }
            kept.append(w);
        }
        // if the whole name was filler words, keep the original rather than returning ""
        return kept.length() == 0 ? phrase : kept.toString();
    }

    /** Rule-based singularisation of one lower-case word. */
    static String singularize(String w) {
        if (w.length() <= 3) {
            return w;
        }
        String irregular = IRREGULAR.get(w);
        if (irregular != null) {
            return irregular;
        }
        if (IE_PLURALS.contains(w)) {
            return w.substring(0, w.length() - 1);              // cookies -> cookie
        }
        if (w.endsWith("ies") && w.length() > 4) {
            return w.substring(0, w.length() - 3) + "y";       // berries -> berry
        }
        if (w.endsWith("oes")) {
            return w.substring(0, w.length() - 2);             // tomatoes -> tomato
        }
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses")
                || w.endsWith("xes") || w.endsWith("zes")) {
            return w.substring(0, w.length() - 2);             // peaches -> peach
        }
        if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) {
            return w;                                           // lemongrass, hummus
        }
        if (w.endsWith("s")) {
            return w.substring(0, w.length() - 1);             // eggs -> egg
        }
        return w;
    }
}
