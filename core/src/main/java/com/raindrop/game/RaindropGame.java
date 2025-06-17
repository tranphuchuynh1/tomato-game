package com.raindrop.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class RaindropGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public ScoreManager scoreManager; // Thêm ScoreManager
    public ScoreHistoryManager scoreHistoryManager;
    public ItemManager itemManager;
    public PaymentInterface paymentManager;

    // Define game dimensions as constants for easy reference
    public static final int GAME_WIDTH = 500;
    public static final int GAME_HEIGHT = 1000;

    public RaindropGame(PaymentInterface paymentManager) {
        this.paymentManager = paymentManager;
    }

    // Constructor để inject ScoreManager từ platform
    public RaindropGame() {
        this.scoreManager = scoreManager;
        this.scoreHistoryManager = scoreHistoryManager;
    }

    @Override
    public void create() {
        itemManager = new ItemManager();
        batch = new SpriteBatch();
        font = new BitmapFont(); // Default Arial font
        font.getData().setScale(2.0f); // Make font twice as big
        this.setScreen(new MainMenuScreen(this));
        // KHỞI TẠO PAYMENT MANAGER
        initializePaymentManager();
    }

    private void initializePaymentManager() {
        try {
            // Nếu bạn có implementation của PaymentInterface
            // paymentManager = new PaymentManagerImpl();

            // Hoặc tạo implementation đơn giản
            paymentManager = new PaymentInterface() {
                @Override
                public void initiatePayment(String orderId, int amount, String orderInfo, PaymentCallback callback) {
                    // Implement payment logic here
                    Gdx.app.log("PaymentManager", "Initiating payment: " + orderId + ", amount: " + amount);
                    // For testing, you can call success callback
                    // callback.onPaymentSuccess("test_transaction", orderId);
                }

                @Override
                public void showPaymentDialog(String itemId, String itemName, int priceVND,
                                              String texturePath, PaymentDialogCallback callback) {
                    Gdx.app.log("PaymentManager", "Showing payment dialog for: " + itemName);

                    // TẠO VÀ HIỂN THỊ PAYMENT DIALOG
                    try {
                        // Lấy stage từ screen hiện tại
                        if (getScreen() instanceof StoreScreen) {
                            StoreScreen storeScreen = (StoreScreen) getScreen();

                            // Tạo dialog và hiển thị
                            showPaymentDialogImpl(itemId, itemName, priceVND, texturePath, callback, storeScreen);
                        }
                    } catch (Exception e) {
                        Gdx.app.error("PaymentManager", "Error showing payment dialog: " + e.getMessage());
                        callback.onPaymentCancelled();
                    }
                }

                @Override
                public String formatVND(int amount) {
                    return String.format("%,d VND", amount);
                }

                @Override
                public String generateOrderId(String itemId) {
                    return itemId + "_" + System.currentTimeMillis();
                }
            };

            Gdx.app.log("RaindropGame", "PaymentManager initialized successfully");

        } catch (Exception e) {
            Gdx.app.error("RaindropGame", "Failed to initialize PaymentManager: " + e.getMessage());
            paymentManager = null;
        }
    }

    // Helper method để hiển thị dialog - FIXED VERSION
    private void showPaymentDialogImpl(String itemId, String itemName, int priceVND,
                                       String texturePath, PaymentInterface.PaymentDialogCallback callback,
                                       StoreScreen storeScreen) {
        try {
            // Tạo QRPaymentDialog với đúng constructor
            QRPaymentDialog dialog = new QRPaymentDialog(
                itemName,
                itemId,
                priceVND,
                new QRPaymentDialog.QRPaymentCallback() {
                    @Override
                    public void onPaymentConfirmed(String orderId, String itemId) {
                        // Gọi callback của PaymentInterface
                        if (callback != null) {
                            callback.onPaymentSuccess("transaction_" + orderId, orderId);
                        }
                    }

                    @Override
                    public void onPaymentCancelled() {
                        // Gọi callback của PaymentInterface
                        if (callback != null) {
                            callback.onPaymentCancelled();
                        }
                    }
                },
                storeScreen.getStage()
            );

            // Dialog sẽ tự động hiển thị vì constructor đã gọi showCentered()

        } catch (Exception e) {
            Gdx.app.error("PaymentManager", "Error in showPaymentDialogImpl: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onPaymentCancelled();
            }
        }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
