package com.smartpantry.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UnitConverterTest {

    private static final double DELTA = 1e-6;

    @Test
    public void unitAliasesAreRecognised() {
        assertEquals("g", UnitConverter.canonicalUnit("Grams"));
        assertEquals("tbsp", UnitConverter.canonicalUnit("TBSP."));
        assertEquals("clove", UnitConverter.canonicalUnit("cloves"));
        assertEquals("l", UnitConverter.canonicalUnit("Litres"));
        assertEquals("piece", UnitConverter.canonicalUnit(""));
        assertEquals("piece", UnitConverter.canonicalUnit(null));
        assertNull(UnitConverter.canonicalUnit("furlong"));
    }

    @Test
    public void everySpinnerUnitIsKnown() {
        for (String unit : UnitConverter.SPINNER_UNITS) {
            assertEquals(unit, UnitConverter.canonicalUnit(unit));
            assertNotNull(UnitConverter.dimensionOf(unit));
        }
    }

    @Test
    public void sameDimensionConversions() {
        assertEquals(500.0, UnitConverter.convert(0.5, "kg", "g", "x"), DELTA);
        assertEquals(1500.0, UnitConverter.convert(1.5, "l", "ml", "x"), DELTA);
        assertEquals(30.0, UnitConverter.convert(2, "tbsp", "ml", "x"), DELTA);
        assertEquals(10.0, UnitConverter.convert(2, "tsp", "ml", "x"), DELTA);
        assertEquals(240.0, UnitConverter.convert(1, "cup", "ml", "x"), DELTA);
        assertEquals(3.0, UnitConverter.convert(3, "clove", "piece", "x"), DELTA);
    }

    @Test
    public void massToVolumeNeedsAKnownDensity() {
        // 1 cup of flour = 240 ml * 0.53 g/ml = 127.2 g
        assertEquals(127.2, UnitConverter.convert(1, "cup", "g", "flour"), DELTA);
        assertEquals(240.0, UnitConverter.convert(127.2, "g", "ml", "flour"), 1e-4);
        assertNull(UnitConverter.convert(1, "cup", "g", "unobtainium"));
    }

    @Test
    public void countToMassNeedsAKnownPieceWeight() {
        assertEquals(100.0, UnitConverter.convert(2, "piece", "g", "egg"), DELTA);
        assertEquals(5.0, UnitConverter.convert(600, "g", "piece", "tomato"), DELTA);
        assertNull(UnitConverter.convert(2, "piece", "g", "unobtainium"));
    }

    @Test
    public void incomparableUnitsReturnNull() {
        assertNull(UnitConverter.convert(1, "cup", "piece", "flour"));
        assertNull(UnitConverter.convert(1, "furlong", "g", "flour"));
    }
}
