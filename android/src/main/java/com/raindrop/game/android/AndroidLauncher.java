package com.raindrop.game.android;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.raindrop.game.RaindropGame;
import com.raindrop.game.payment.VNPayHelper;
import com.raindrop.game.android.AndroidPaymentPlatform;

import java.util.HashMap;
import java.util.Map;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        configuration.useAccelerometer = false;
        configuration.useCompass = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // Khởi tạo PaymentPlatform trước
        AndroidPaymentPlatform paymentPlatform = new AndroidPaymentPlatform(this);
        VNPayHelper.initialize(paymentPlatform);

        // Khởi tạo game với constructor mặc định
        RaindropGame game = new RaindropGame();

        // Gán payment platform sau khi tạo game

        // Khởi tạo LibGDX trước
        initialize(game, configuration);

        // SAU KHI LibGDX được khởi tạo, tạo Firebase managers
        initializeFirebaseManagers(game);
    }

    private void initializeFirebaseManagers(RaindropGame game) {
        try {
            // Bây giờ Gdx.app đã sẵn sàng
            FirebaseScoreManager scoreManager = new FirebaseScoreManager();
            FirebaseScoreHistoryManager scoreHistoryManager = new FirebaseScoreHistoryManager();

            // Gán vào game
            game.scoreManager = scoreManager;
            game.scoreHistoryManager = scoreHistoryManager;

            Gdx.app.log("AndroidLauncher", "Firebase managers initialized successfully");
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("AndroidLauncher", "Error initializing Firebase managers: " + e.getMessage());
            } else {
                // Fallback nếu Gdx.app vẫn chưa sẵn sàng
                System.err.println("AndroidLauncher: Error initializing Firebase managers: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri uri = intent.getData();

            if ("vnpay".equals(uri.getScheme()) && "payment".equals(uri.getHost()) &&
                "/result".equals(uri.getPath())) {

                Map<String, String> params = parseVNPayParams(uri);

                if (!params.isEmpty()) {
                    if (Gdx.app != null) {
                        Gdx.app.log("AndroidLauncher", "Received VNPay callback with " + params.size() + " parameters");
                    }
                    VNPayHelper.handlePaymentResult(params);
                } else {
                    if (Gdx.app != null) {
                        Gdx.app.log("AndroidLauncher", "No VNPay parameters found in deep link");
                    }
                }
            }
        }
    }

    private Map<String, String> parseVNPayParams(Uri uri) {
        Map<String, String> params = new HashMap<>();

        try {
            String query = uri.getQuery();
            if (query != null && !query.isEmpty()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                        params.put(key, value);
                    }
                }

                if (Gdx.app != null) {
                    Gdx.app.log("AndroidLauncher", "Parsed VNPay parameters: " + params.toString());
                }
            }

            String fragment = uri.getFragment();
            if (fragment != null && !fragment.isEmpty()) {
                String[] pairs = fragment.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                        params.put(key, value);
                    }
                }
            }

        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("AndroidLauncher", "Error parsing VNPay parameters: " + e.getMessage());
            } else {
                System.err.println("AndroidLauncher: Error parsing VNPay parameters: " + e.getMessage());
            }
        }

        return params;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
            setIntent(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
