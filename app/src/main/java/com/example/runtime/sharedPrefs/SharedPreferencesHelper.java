package com.example.runtime.sharedPrefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    public static void insertFieldStringToSP(Context context, String fieldKey, String fieldValue) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(fieldKey, fieldValue);
        editor.apply();
    }

    public static String getFieldStringFromSP(Context context, String fieldKey) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Retrieve the username, providing a default value (empty string in this case)
        return preferences.getString(fieldKey, "");
    }

    public static void clearSP(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // Clear all data
        editor.clear();
        editor.apply();
    }

    public static void removeKeySP(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // Clear all data
        editor.remove(key);
        editor.apply();
    }

}
