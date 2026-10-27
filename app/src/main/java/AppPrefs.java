package com.smartpantry.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The two settings of the Settings screen, stored in SharedPreferences.
 */
public final class AppPrefs {

    private AppPrefs() {
    }

    public static final String UNITS_METRIC = "metric";
    public static final String UNITS_IMPERIAL = "imperial";

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_UNIT_SYSTEM = "unit_system";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** Either {@link #UNITS_METRIC} (default) or {@link #UNITS_IMPERIAL}. */
    public static String getUnitSystem(Context context) {
        return prefs(context).getString(KEY_UNIT_SYSTEM, UNITS_METRIC);
    }

    public static void setUnitSystem(Context context, String unitSystem) {
        prefs(context).edit().putString(KEY_UNIT_SYSTEM, unitSystem).apply();
    }
}
