package com.raindrop.game;

public interface ScoreManager {
    interface ScoreCallback {
        void onSuccess(long totalScore);
        void onFailure(String error);
    }



    void getCurrentTotalScore(ScoreCallback callback);
    void addScoreToTotal(int newScore, ScoreCallback callback);
    void resetTotalScore(ScoreCallback callback);
}
