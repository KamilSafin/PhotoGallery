package com.example.kamil.photogallery;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class QueryPreferences {

    private static final String PREFERENCES_QUERY = QueryPreferences.class.getName() + "PREFERENCES_QUERY";
    private static final String PREF_LAST_RESULT_ID = QueryPreferences.class.getName() + "PREF_LAST_RESULT_ID";

    public static void setQuery(Context context, String query) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREFERENCES_QUERY, query);
        editor.apply();
    }

    public static String getQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCES_QUERY, null);
    }

    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_RESULT_ID, null);
    }

    public static void setLastResultId(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_LAST_RESULT_ID, lastResultId).apply();
    }
}
