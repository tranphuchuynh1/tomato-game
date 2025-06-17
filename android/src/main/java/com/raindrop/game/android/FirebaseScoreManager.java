package com.raindrop.game.android;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.raindrop.game.ScoreManager;

import java.util.HashMap;
import java.util.Map;

public class FirebaseScoreManager implements ScoreManager {
    private static final String COLLECTION_NAME = "player_scores";
    private static final String DOCUMENT_ID = "player_total_score";
    private static final String FIELD_TOTAL_SCORE = "totalScore";

    private FirebaseFirestore db;

    public FirebaseScoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void getCurrentTotalScore(ScoreCallback callback) {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(DOCUMENT_ID);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Long totalScore = document.getLong(FIELD_TOTAL_SCORE);
                    callback.onSuccess(totalScore != null ? totalScore : 0);
                } else {
                    callback.onSuccess(0);
                }
            } else {
                callback.onFailure(task.getException() != null ?
                    task.getException().getMessage() : "Unknown error");
            }
        });
    }

    @Override
    public void addScoreToTotal(int newScore, ScoreCallback callback) {
        getCurrentTotalScore(new ScoreCallback() {
            @Override
            public void onSuccess(long currentTotal) {
                long newTotal = currentTotal + newScore;

                Map<String, Object> scoreData = new HashMap<>();
                scoreData.put(FIELD_TOTAL_SCORE, newTotal);
                scoreData.put("lastUpdated", System.currentTimeMillis());

                DocumentReference docRef = db.collection(COLLECTION_NAME).document(DOCUMENT_ID);
                docRef.set(scoreData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> callback.onSuccess(newTotal))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    @Override
    public void resetTotalScore(ScoreCallback callback) {
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put(FIELD_TOTAL_SCORE, 0);
        scoreData.put("lastUpdated", System.currentTimeMillis());

        DocumentReference docRef = db.collection(COLLECTION_NAME).document(DOCUMENT_ID);
        docRef.set(scoreData, SetOptions.merge())
            .addOnSuccessListener(aVoid -> callback.onSuccess(0))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
