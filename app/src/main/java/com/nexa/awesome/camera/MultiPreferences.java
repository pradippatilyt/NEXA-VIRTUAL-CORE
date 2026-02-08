package com.nexa.awesome.camera;

import android.content.Context;
import android.content.SharedPreferences;

import com.nexa.awesome.app.App;

public class MultiPreferences {
    private static final String PREF_NAME = "multi_pref";
    private static MultiPreferences sInstance;
    private final SharedPreferences mPreferences;

    private MultiPreferences(Context context) {
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized MultiPreferences getInstance() {
        if (sInstance == null) {
            sInstance = new MultiPreferences(App.getContext());
        }
        return sInstance;
    }

    public void setInt(String key, int value) {
        mPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    public void setString(String key, String value) {
        mPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        mPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        mPreferences.edit().remove(key).apply();
    }

    public void clear() {
        mPreferences.edit().clear().apply();
    }
}
