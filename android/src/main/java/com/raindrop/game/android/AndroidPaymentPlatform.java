package com.raindrop.game.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.content.Context;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import com.badlogic.gdx.Gdx;
import com.raindrop.game.payment.PaymentPlatform;

public class AndroidPaymentPlatform implements PaymentPlatform {
    private Activity activity;

    public AndroidPaymentPlatform(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void openBrowser(String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(browserIntent);
        } catch (Exception e) {
            Gdx.app.error("AndroidPaymentPlatform", "Error opening browser: " + e.getMessage());
        }
    }

    @Override
    public String getDeviceIP() {
        try {
            WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

            // Convert little-endian to big-endian if needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ipAddress = Integer.reverseBytes(ipAddress);
            }

            byte[] ipByteArray = new byte[4];
            ipByteArray[0] = (byte) (ipAddress & 0xff);
            ipByteArray[1] = (byte) (ipAddress >> 8 & 0xff);
            ipByteArray[2] = (byte) (ipAddress >> 16 & 0xff);
            ipByteArray[3] = (byte) (ipAddress >> 24 & 0xff);

            InetAddress inetAddress = InetAddress.getByAddress(ipByteArray);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
