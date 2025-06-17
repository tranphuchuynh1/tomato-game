package com.raindrop.game.android;

import com.badlogic.gdx.Gdx;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.raindrop.game.ScoreHistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseScoreHistoryManager implements ScoreHistoryManager {
    private static final String COLLECTION_NAME = "score_history";

    private FirebaseFirestore db;

    public FirebaseScoreHistoryManager() {
        try {
            db = FirebaseFirestore.getInstance();
            Gdx.app.log("FirebaseScoreHistoryManager", "Firebase initialized successfully");
        } catch (Exception e) {
            Gdx.app.error("FirebaseScoreHistoryManager", "Failed to initialize Firebase: " + e.getMessage());
        }
    }

    @Override
    public void saveScore(String playerName, int score, SaveScoreCallback callback) {
        if (db == null) {
            if (callback != null) {
                callback.onFailure("Firebase not initialized");
            }
            return;
        }

        // Validate input
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Unknown Player"; // Default name
        }

        Map<String, Object> scoreData = new HashMap<>(); // Fixed generic type
        scoreData.put("playerName", playerName);
        scoreData.put("score", score);
        scoreData.put("timestamp", System.currentTimeMillis());

        Gdx.app.log("FirebaseScoreHistoryManager", "Saving score: " + playerName + " - " + score);

        db.collection(COLLECTION_NAME)
            .add(scoreData)
            .addOnSuccessListener(documentReference -> {
                Gdx.app.log("FirebaseScoreHistoryManager", "Score saved successfully with ID: " + documentReference.getId());
                if (callback != null) {
                    callback.onSuccess();
                }
            })
            .addOnFailureListener(e -> {
                Gdx.app.error("FirebaseScoreHistoryManager", "Failed to save score: " + e.getMessage());
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            });
    }

    @Override
    public void getScoreHistory(ScoreHistoryCallback callback) {
        if (db == null) {
            if (callback != null) {
                callback.onFailure("Firebase not initialized");
            }
            return;
        }

        Gdx.app.log("FirebaseScoreHistoryManager", "Loading score history...");

        db.collection(COLLECTION_NAME)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(50) // Giới hạn 50 bản ghi cao nhất
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<ScoreRecord> scoreHistory = new ArrayList<>();

                    task.getResult().forEach(document -> {
                        try {
                            String playerName = document.getString("playerName");
                            Long score = document.getLong("score");
                            Long timestamp = document.getLong("timestamp");

                            if (playerName != null && score != null && timestamp != null) {
                                scoreHistory.add(new ScoreRecord(
                                    playerName,
                                    score.intValue(),
                                    timestamp
                                ));
                            }
                        } catch (Exception e) {
                            Gdx.app.error("FirebaseScoreHistoryManager", "Error parsing document: " + e.getMessage());
                        }
                    });

                    Gdx.app.log("FirebaseScoreHistoryManager", "Loaded " + scoreHistory.size() + " score records");

                    if (callback != null) {
                        callback.onSuccess(scoreHistory);
                    }
                } else {
                    String errorMsg = task.getException() != null ?
                        task.getException().getMessage() : "Unknown error loading scores";
                    Gdx.app.error("FirebaseScoreHistoryManager", "Failed to load scores: " + errorMsg);

                    if (callback != null) {
                        callback.onFailure(errorMsg);
                    }
                }
            });
    }

    @Override
    public void clearHistory(SaveScoreCallback callback) {
        if (db == null) {
            if (callback != null) {
                callback.onFailure("Firebase not initialized");
            }
            return;
        }

        Gdx.app.log("FirebaseScoreHistoryManager", "Clearing score history...");

        db.collection(COLLECTION_NAME)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Delete all documents
                    for (com.google.firebase.firestore.DocumentSnapshot document : task.getResult()) {
                        document.getReference().delete();
                    }

                    Gdx.app.log("FirebaseScoreHistoryManager", "Score history cleared successfully");

                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String errorMsg = task.getException() != null ?
                        task.getException().getMessage() : "Unknown error clearing history";
                    Gdx.app.error("FirebaseScoreHistoryManager", "Failed to clear history: " + errorMsg);

                    if (callback != null) {
                        callback.onFailure(errorMsg);
                    }
                }
            });
    }
}
