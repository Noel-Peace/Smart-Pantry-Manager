package com.smartpantry.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NameNormalizerTest {

    @Test
    public void tomatoAndTomatoesAreTheSameIngredient() {
        assertTrue(NameNormalizer.sameIngredient("tomato", "tomatoes"));
        assertTrue(NameNormalizer.sameIngredient("Tomatoes", "  TOMATO "));
    }

    @Test
    public void commonPluralPatterns() {
        assertEquals("egg", NameNormalizer.normalize("Eggs"));
        assertEquals("potato", NameNormalizer.normalize("potatoes"));
        assertEquals("berry", NameNormalizer.normalize("berries"));
        assertEquals("clove", NameNormalizer.normalize("cloves"));
        assertEquals("peach", NameNormalizer.normalize("peaches"));
        assertEquals("loaf", NameNormalizer.normalize("loaves"));
        assertEquals("pea", NameNormalizer.normalize("peas"));
        assertEquals("cookie", NameNormalizer.normalize("cookies"));
        assertEquals("avocado", NameNormalizer.normalize("avocados"));
    }

    @Test
    public void wordsThatEndInSButAreNotPluralStayIntact() {
        assertEquals("hummus", NameNormalizer.normalize("hummus"));
        assertEquals("asparagus", NameNormalizer.normalize("asparagus"));
        assertEquals("watercress", NameNormalizer.normalize("watercress"));
    }

    @Test
    public void fillerWordsAndPunctuationAreIgnored() {
        assertEquals("egg", NameNormalizer.normalize("Fresh Large Eggs"));
        assertEquals("tomato", NameNormalizer.normalize("tomatoes, diced"));
        assertEquals("olive oil", NameNormalizer.normalize("Extra-Virgin Olive Oil"));
        assertEquals("garlic", NameNormalizer.normalize("garlic (minced)"));
    }

    @Test
    public void synonymsAndSpellingVariants() {
        assertTrue(NameNormalizer.sameIngredient("yoghurt", "yogurt"));
        assertTrue(NameNormalizer.sameIngredient("scallions", "green onions"));
        assertTrue(NameNormalizer.sameIngredient("beef mince", "ground beef"));
        assertTrue(NameNormalizer.sameIngredient("red onion", "onion"));
        assertTrue(NameNormalizer.sameIngredient("sunflower oil", "oil"));
    }

    @Test
    public void differentIngredientsAreNotMerged() {
        assertFalse(NameNormalizer.sameIngredient("butter", "peanut butter"));
        assertFalse(NameNormalizer.sameIngredient("tomato", "tomato sauce"));
        assertFalse(NameNormalizer.sameIngredient("rice", "rice flour"));
    }

    @Test
    public void emptyAndNullInputAreSafe() {
        assertEquals("", NameNormalizer.normalize(null));
        assertEquals("", NameNormalizer.normalize("   "));
        assertEquals("", NameNormalizer.normalize("123"));
        assertFalse(NameNormalizer.sameIngredient("", ""));
    }
}
