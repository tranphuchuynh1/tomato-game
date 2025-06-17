package com.raindrop.game;

import java.util.List;

public interface ScoreHistoryManager {

    // Class để đại diện cho một bản ghi điểm
    class ScoreRecord {
        public String playerName;
        public int score;
        public long timestamp;

        public ScoreRecord() {}

        public ScoreRecord(String playerName, int score, long timestamp) {
            this.playerName = playerName;
            this.score = score;
            this.timestamp = timestamp;
        }
    }

    // Callback interface để xử lý kết quả
    interface ScoreHistoryCallback {
        void onSuccess(List<ScoreRecord> scoreHistory); // Fixed generic type
        void onFailure(String error);
    }

    // Callback cho việc lưu điểm
    interface SaveScoreCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Lưu điểm mới vào lịch sử
    void saveScore(String playerName, int score, SaveScoreCallback callback);

    // Lấy lịch sử điểm (sắp xếp theo điểm cao nhất)
    void getScoreHistory(ScoreHistoryCallback callback);

    // Xóa toàn bộ lịch sử
    void clearHistory(SaveScoreCallback callback);
}
