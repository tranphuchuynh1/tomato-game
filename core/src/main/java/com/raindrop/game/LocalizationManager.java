package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {
    private static Map<String, String> vietnameseTexts;
    private static Map<String, String> englishTexts;

    static {
        initializeTexts();
    }

    private static void initializeTexts() {
        // Vietnamese texts
        vietnameseTexts = new HashMap<>();
        vietnameseTexts.put("play_game", "CHOI GAME");
        vietnameseTexts.put("store", "CUA HÀNG");
        vietnameseTexts.put("achievements", "THÀNH TÍCH");
        vietnameseTexts.put("settings", "CÀI DAT");
        vietnameseTexts.put("exit", "THOÁT");
        vietnameseTexts.put("tomato_score", "Cà Chua: ");
        vietnameseTexts.put("total_score", "Tổng Điểm: ");
        vietnameseTexts.put("pause", "Tạm Dừng");
        vietnameseTexts.put("continue", "Tiếp tục");
        vietnameseTexts.put("game_over", "Game Over");
        vietnameseTexts.put("play_again", "Chơi tiếp");
        vietnameseTexts.put("sound_on", "ÂM THANH: BẬT");
        vietnameseTexts.put("sound_off", "ÂM THANH: TẮT");
        vietnameseTexts.put("language", "NGÔN NGỮ: VIỆT");
        vietnameseTexts.put("back", "QUAY LẠI");

        // English texts
        englishTexts = new HashMap<>();
        englishTexts.put("play_game", "PLAY GAME");
        englishTexts.put("store", "STORE");
        englishTexts.put("achievements", "ACHIEVEMENTS");
        englishTexts.put("settings", "SETTINGS");
        englishTexts.put("exit", "EXIT");
        englishTexts.put("tomato_score", "Tomatoes: ");
        englishTexts.put("total_score", "Total Score: ");
        englishTexts.put("pause", "Paused");
        englishTexts.put("continue", "Continue");
        englishTexts.put("game_over", "Game Over");
        englishTexts.put("play_again", "Play Again");
        englishTexts.put("sound_on", "SOUND: ON");
        englishTexts.put("sound_off", "SOUND: OFF");
        englishTexts.put("language", "LANGUAGE: ENG");
        englishTexts.put("back", "BACK");
    }

    public static String getText(String key) {
        boolean isVietnamese = SettingsScreen.isVietnamese();
        Map<String, String> currentTexts = isVietnamese ? vietnameseTexts : englishTexts;

        String text = currentTexts.get(key);
        if (text == null) {
            Gdx.app.error("LocalizationManager", "Text not found for key: " + key);
            return key.toUpperCase(); // Return key as fallback
        }
        return text;
    }

    public static boolean isVietnamese() {
        return SettingsScreen.isVietnamese();
    }

    public static boolean isSoundEnabled() {
        return SettingsScreen.isSoundEnabled();
    }
}
