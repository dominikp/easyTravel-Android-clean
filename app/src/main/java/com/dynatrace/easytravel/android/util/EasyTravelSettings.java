package com.dynatrace.easytravel.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

public class EasyTravelSettings {
    public static final String KEY_SERVER_HOSTNAME = "pref_hostname";
    public static final String KEY_SERVER_PORT = "pref_port";

    private static final String KEY_EVENT_CRASH_LOGIN_ENABLED = "pref_event_crash_login";
    private static final String KEY_EVENT_ERROR_ON_BOOKING_AND_SEARCH = "pref_event_error_on_booking_and_search";

    // TODO: (1) enter the URL to your easyTravel environment
    private static final String DEFAULT_SERVER_HOST = "http://ec2-52-17-244-177.eu-west-1.compute.amazonaws.com";
    private static final String DEFAULT_SERVER_PORT = "8080";

    /* Frontend Host */

    public static String getServerHostName(@NonNull Context context) {
        return getPreferences(context).getString(KEY_SERVER_HOSTNAME, DEFAULT_SERVER_HOST);
    }

    public static String getServerPort(@NonNull Context context) {
        return getPreferences(context).getString(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
    }

    /* Preferences for crash events */

    public static boolean shouldCrashOnLogin(@NonNull Context context) {
        return getPreferences(context).getBoolean(KEY_EVENT_CRASH_LOGIN_ENABLED, false);
    }

    public static boolean shouldHaveErrorOnBookingAndSearch(@NonNull Context context) {
        return getPreferences(context).getBoolean(KEY_EVENT_ERROR_ON_BOOKING_AND_SEARCH, false);
    }

    private static SharedPreferences getPreferences(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
