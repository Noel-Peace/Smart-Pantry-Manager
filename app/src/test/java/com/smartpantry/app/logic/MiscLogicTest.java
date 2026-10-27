package com.smartpantry.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;

public class MiscLogicTest {

    // ---- QuantityFormatter

    @Test
    public void wholeNumbersHaveNoDecimals() {
        assertEquals("2", QuantityFormatter.format(2.0));
        assertEquals("0.5", QuantityFormatter.format(0.5));
        assertEquals("2.25", QuantityFormatter.format(2.25));
        assertEquals("0.33", QuantityFormatter.format(1.0 / 3.0));
    }

    @Test
    public void descriptionsReadNaturally() {
        assertEquals("3 eggs", QuantityFormatter.describe(3, "piece", "eggs"));
        assertEquals("200 g pasta", QuantityFormatter.describe(200, "g", "pasta"));
        assertEquals("2 cloves garlic", QuantityFormatter.describe(2, "clove", "garlic"));
        assertEquals("1 can tuna", QuantityFormatter.describe(1, "can", "tuna"));
        assertEquals("0.5 onion", QuantityFormatter.describe(0.5, "piece", "onion"));
    }

    // ---- ExpiryUtils

    private static Calendar day(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(y, m - 1, d, 15, 30, 0);
        return c;
    }

    @Test
    public void daysUntilCountsWholeDays() {
        Calendar today = day(2026, 10, 24);
        assertEquals(Integer.valueOf(0), ExpiryUtils.daysUntil("2026-10-24", today));
        assertEquals(Integer.valueOf(1), ExpiryUtils.daysUntil("2026-10-25", today));
        assertEquals(Integer.valueOf(7), ExpiryUtils.daysUntil("2026-10-31", today));
        assertEquals(Integer.valueOf(-2), ExpiryUtils.daysUntil("2026-10-22", today));
        // across a month and a year boundary
        assertEquals(Integer.valueOf(8), ExpiryUtils.daysUntil("2026-11-01", today));
        assertEquals(Integer.valueOf(69), ExpiryUtils.daysUntil("2027-01-01", today));
    }

    @Test
    public void invalidDatesAreRejected() {
        assertNull(ExpiryUtils.daysUntil(null, day(2026, 10, 24)));
        assertNull(ExpiryUtils.daysUntil("", day(2026, 10, 24)));
        assertNull(ExpiryUtils.daysUntil("tomorrow", day(2026, 10, 24)));
        assertFalse(ExpiryUtils.isValidDate("2026-02-30"));
        assertFalse(ExpiryUtils.isValidDate("2026-13-01"));
        assertFalse(ExpiryUtils.isValidDate("2026-10-24abc"));
        assertFalse(ExpiryUtils.isValidDate("24/10/2026"));
        assertTrue(ExpiryUtils.isValidDate("2028-02-29")); // leap year
        assertTrue(ExpiryUtils.isValidDate(" 2026-10-24 "));
    }

    @Test
    public void formatPadsMonthAndDay() {
        assertEquals("2026-01-05", ExpiryUtils.format(2026, 0, 5));
        assertNotNull(ExpiryUtils.format(2026, 11, 31));
        assertEquals("2026-12-31", ExpiryUtils.format(2026, 11, 31));
    }
}
