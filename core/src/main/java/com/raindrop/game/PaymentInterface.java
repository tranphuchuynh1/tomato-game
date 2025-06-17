package com.raindrop.game;

public interface PaymentInterface {

    interface PaymentCallback {
        void onPaymentSuccess(String transactionId, String orderId);
        void onPaymentFailure(String errorMessage);
        void onPaymentCancelled();
    }

    interface PaymentDialogCallback {
        void onPaymentInitiated(String orderId, String itemId);
        void onPaymentCancelled();

        void onPaymentSuccess(String s, String orderId);
    }

    // Khởi tạo thanh toán
    void initiatePayment(String orderId, int amount, String orderInfo, PaymentCallback callback);

    // Hiển thị dialog thanh toán
    void showPaymentDialog(String itemId, String itemName, int priceVND,
                           String texturePath, PaymentDialogCallback callback);

    // Format giá tiền
    String formatVND(int amount);

    // Tạo order ID
    String generateOrderId(String itemId);
}
