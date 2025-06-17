package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSettings {
    private static final String PREFS_NAME = "raindrop_game_settings";
    private static final String SOUND_ENABLED_KEY = "sound_enabled";
    private static final String LANGUAGE_VIETNAMESE_KEY = "language_vietnamese";

    // Default values
    private static final boolean DEFAULT_SOUND_ENABLED = true;
    private static final boolean DEFAULT_LANGUAGE_VIETNAMESE = true;

    private static Preferences preferences;

    // Initialize preferences
    private static void initPreferences() {
        if (preferences == null) {
            preferences = Gdx.app.getPreferences(PREFS_NAME);
        }
    }

    // Sound settings
    public static boolean isSoundEnabled() {
        initPreferences();
        return preferences.getBoolean(SOUND_ENABLED_KEY, DEFAULT_SOUND_ENABLED);
    }

    public static void setSoundEnabled(boolean enabled) {
        initPreferences();
        preferences.putBoolean(SOUND_ENABLED_KEY, enabled);
        preferences.flush(); // Important: Save to disk immediately
        Gdx.app.log("GameSettings", "Sound setting saved: " + enabled);
    }

    // Language settings
    public static boolean isVietnamese() {
        initPreferences();
        return preferences.getBoolean(LANGUAGE_VIETNAMESE_KEY, DEFAULT_LANGUAGE_VIETNAMESE);
    }

    public static void setVietnamese(boolean vietnamese) {
        initPreferences();
        preferences.putBoolean(LANGUAGE_VIETNAMESE_KEY, vietnamese);
        preferences.flush(); // Important: Save to disk immediately
        Gdx.app.log("GameSettings", "Language setting saved: " + (vietnamese ? "Vietnamese" : "English"));
    }

    // Reset all settings to default
    public static void resetToDefaults() {
        initPreferences();
        preferences.clear();
        preferences.flush();
        Gdx.app.log("GameSettings", "Settings reset to defaults");
    }

    // Debug method to print current settings
    public static void printCurrentSettings() {
        Gdx.app.log("GameSettings", "Current settings:");
        Gdx.app.log("GameSettings", "  Sound enabled: " + isSoundEnabled());
        Gdx.app.log("GameSettings", "  Language Vietnamese: " + isVietnamese());
    }
}
