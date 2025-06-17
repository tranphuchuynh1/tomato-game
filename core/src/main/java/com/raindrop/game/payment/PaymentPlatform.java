package com.raindrop.game.payment;

import java.util.Map;

public interface PaymentPlatform {
    void openBrowser(String url);
    String getDeviceIP();
}
