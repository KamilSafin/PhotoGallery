package com.example.kamil.photogallery;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class QueryPreferences {

    private static final String PREFERENCES_QUERY = QueryPreferences.class.getName() + "PREFERENCES_QUERY";

    public static void setQuery(Context context, String query) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREFERENCES_QUERY, query);
        editor.apply();
    }

    public static String getQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCES_QUERY, null);
    }
}
