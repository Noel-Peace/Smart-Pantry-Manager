package com.smartpantry.app.logic;

import java.util.Locale;

/**
 * Display helpers for quantities: "2" instead of "2.0", "0.5" instead of "0.50",
 * and "3 eggs" / "2 cloves garlic" style descriptions.
 */
public final class QuantityFormatter {

    private QuantityFormatter() {
    }

    /** Whole numbers without decimals, otherwise up to 2 decimals with trailing zeros removed. */
    public static String format(double quantity) {
        if (Math.abs(quantity - Math.rint(quantity)) < 1e-9) {
            return String.valueOf((long) Math.rint(quantity));
        }
        String s = String.format(Locale.US, "%.2f", quantity);
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** The unit as it should be displayed for a quantity ("" for plain pieces). */
    public static String unitLabel(String unit, double quantity) {
        if (unit == null || unit.isEmpty() || "piece".equals(unit)) {
            return "";
        }
        if (quantity > 1.0000001) {
            switch (unit) {
                case "clove":
                case "slice":
                case "can":
                case "packet":
                case "cup":
                    return unit + "s";
                case "bunch":
                    return "bunches";
                case "pinch":
                    return "pinches";
                default:
                    return unit;
            }
        }
        return unit;
    }

    /** e.g. (3, "piece", "eggs") -> "3 eggs";  (200, "g", "pasta") -> "200 g pasta". */
    public static String describe(double quantity, String unit, String name) {
        StringBuilder sb = new StringBuilder(format(quantity));
        String label = unitLabel(unit, quantity);
        if (!label.isEmpty()) {
            sb.append(' ').append(label);
        }
        sb.append(' ').append(name);
        return sb.toString();
    }

    /** e.g. (250, "g") -> "250 g";  (3, "piece") -> "3". */
    public static String withUnit(double quantity, String unit) {
        String label = unitLabel(unit, quantity);
        return label.isEmpty() ? format(quantity) : format(quantity) + " " + label;
    }
}
