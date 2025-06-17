package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class QRPaymentDialog extends Dialog {

    public interface QRPaymentCallback {
        void onPaymentConfirmed(String orderId, String itemId);
        void onPaymentCancelled();
    }

    private String getServerHost() {
        com.badlogic.gdx.Application.ApplicationType appType = Gdx.app.getType();

        switch (appType) {
            case Android:
                // Khi dùng ADB port forwarding, luôn dùng localhost
                return "localhost";
            case Desktop:
                return "localhost"; // Desktop thường chạy localhost
            default:
                return "localhost";
        }
    }

    // Thêm method mới để lấy WiFi IP
    private String getWiFiIPAddress() {
        try {
            // Thử lấy IP của device hiện tại
            java.util.Enumeration<java.net.NetworkInterface> networkInterfaces =
                java.net.NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    java.util.Enumeration<java.net.InetAddress> inetAddresses =
                        networkInterface.getInetAddresses();

                    while (inetAddresses.hasMoreElements()) {
                        java.net.InetAddress inetAddress = inetAddresses.nextElement();

                        if (!inetAddress.isLoopbackAddress() &&
                            inetAddress instanceof java.net.Inet4Address) {
                            String ip = inetAddress.getHostAddress();
                            Gdx.app.log("QRPaymentDialog", "Found device IP: " + ip);
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("QRPaymentDialog", "Error getting WiFi IP: " + e.getMessage());
        }

        return "192.168.1.100"; // Fallback
    }

    private BitmapFont dialogFont;
    private BitmapFont titleFont;
    private String itemName;
    private String itemId;
    private int priceVND;
    private QRPaymentCallback callback;
    private Texture qrCodeTexture;
    private String orderId;

    // Server connection settings
    private static final String AUTH_SERVER_HOST = "localhost";
    private static final int AUTH_SERVER_PORT = 8888;

    // UI Elements
    private Label priceLabel;
    private Image qrImage;
    private TextButton confirmButton;
    private TextButton cancelButton;
    private Label waitingLabel;

    public QRPaymentDialog(String itemName, String itemId, int priceVND, QRPaymentCallback callback, Stage stage) {
        super("THANH TOÁN QR CODE", createDefaultSkin());

        this.itemName = itemName;
        this.itemId = itemId;
        this.priceVND = priceVND;
        this.callback = callback;
        this.orderId = generateOrderId();

        createFonts();
        loadQRCode();
        setupDialog();

        // Hiển thị dialog ngay
        showCentered(stage);
    }


    private boolean isRunningOnEmulator() {
        try {
            // Chỉ check khi đang chạy trên Android
            if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android) {
                // Sử dụng reflection để tránh lỗi compile trên desktop
                Class<?> buildClass = Class.forName("android.os.Build");
                String fingerprint = (String) buildClass.getField("FINGERPRINT").get(null);
                String model = (String) buildClass.getField("MODEL").get(null);
                String brand = (String) buildClass.getField("BRAND").get(null);

                return fingerprint.contains("generic") ||
                    model.contains("Emulator") ||
                    brand.contains("generic");
            }
            return false; // Không phải Android
        } catch (Exception e) {
            Gdx.app.log("QRPaymentDialog", "Cannot detect emulator: " + e.getMessage());
            return false;
        }
    }

    // Constructor đơn giản cho spaceship (default values)
    public QRPaymentDialog(Stage stage) {
        this("Phi thuyền", "spaceship", 5000, new QRPaymentCallback() {
            @Override
            public void onPaymentConfirmed(String orderId, String itemId) {
                Gdx.app.log("QRPaymentDialog", "Payment confirmed for " + itemId);
            }

            @Override
            public void onPaymentCancelled() {
                Gdx.app.log("QRPaymentDialog", "Payment cancelled");
            }
        }, stage);
    }

    private String generateOrderId() {
        return "ORDER_" + System.currentTimeMillis();
    }

    private void loadQRCode() {
        try {
            // QR code
            if (Gdx.files.internal("VietQR_thanhtoan.png").exists()) {
                qrCodeTexture = new Texture(Gdx.files.internal("VietQR_thanhtoan.png"));
                Gdx.app.log("QRPaymentDialog", "Loaded QR code from assets");
            } else {
                Gdx.app.log("QRPaymentDialog", "QR code file not found, creating fallback");
            }
        } catch (Exception e) {
            Gdx.app.error("QRPaymentDialog", "Error loading QR code: " + e.getMessage());
        }
    }

    private static Skin createDefaultSkin() {
        Skin skin = new Skin();

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.1f, 0.2f, 0.95f));
        pixmap.fill();
        Texture backgroundTexture = new Texture(pixmap);
        pixmap.dispose();

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = new TextureRegionDrawable(new TextureRegion(backgroundTexture));
        windowStyle.titleFont = new BitmapFont();
        windowStyle.titleFontColor = Color.WHITE;

        skin.add("default", windowStyle);
        return skin;
    }

    private void createFonts() {
        try {
            dialogFont = new BitmapFont();
            titleFont = new BitmapFont();

            dialogFont.setColor(Color.WHITE);
            titleFont.setColor(Color.YELLOW);

        } catch (Exception e) {
            Gdx.app.error("QRPaymentDialog", "Error creating fonts: " + e.getMessage());
            dialogFont = new BitmapFont();
            titleFont = new BitmapFont();
        }
    }

    private void setupDialog() {
        this.setModal(true);
        this.setMovable(false);
        this.setResizable(false);

        Table contentTable = new Table();
        contentTable.pad(20);

        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.YELLOW);
        Label titleLabel = new Label("THANH TOÁN QR CODE", titleStyle);
        contentTable.add(titleLabel).colspan(2).padBottom(20).center();
        contentTable.row();

        // Item info
        Label.LabelStyle labelStyle = new Label.LabelStyle(dialogFont, Color.WHITE);
        Label itemLabel = new Label("Vật phẩm: " + itemName, labelStyle);
        contentTable.add(itemLabel).colspan(2).center().padBottom(10);
        contentTable.row();

        priceLabel = new Label("Giá: " + String.format("%,d VND", priceVND), labelStyle);
        priceLabel.setColor(Color.ORANGE);
        contentTable.add(priceLabel).colspan(2).center().padBottom(20);
        contentTable.row();

        // QR Code - Tăng kích thước để dễ quét
        if (qrCodeTexture != null) {
            qrImage = new Image(qrCodeTexture);
            contentTable.add(qrImage).width(300).height(300).colspan(2).center().padBottom(20);
            contentTable.row();
        }

        // Bank info
        Label bankInfoLabel = new Label(
            "Ngân hàng: Vietcombank\n" +
                "STK: 1030164053\n" +
                "Chủ TK: TRAN PHUC HUYNH\n" +
                "Nội dung: " + orderId,
            labelStyle
        );
        bankInfoLabel.setColor(Color.CYAN);
        bankInfoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        contentTable.add(bankInfoLabel).colspan(2).center().padBottom(20);
        contentTable.row();

        // Waiting message
        waitingLabel = new Label("Vui lòng chuyển khoản và bấm 'Xác nhận'", labelStyle);
        waitingLabel.setColor(Color.YELLOW);
        waitingLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        contentTable.add(waitingLabel).colspan(2).center().padBottom(15);
        contentTable.row();

        // Server status info
        Label serverInfoLabel = new Label("Kết nối server xác thực: " + AUTH_SERVER_HOST + ":" + AUTH_SERVER_PORT, labelStyle);
        serverInfoLabel.setColor(Color.LIGHT_GRAY);
        serverInfoLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        contentTable.add(serverInfoLabel).colspan(2).center().padBottom(15);
        contentTable.row();

        // Buttons
        Table buttonTable = new Table();

        TextButton.TextButtonStyle confirmButtonStyle = createButtonStyle(new Color(0.2f, 0.8f, 0.2f, 0.8f));
        TextButton.TextButtonStyle cancelButtonStyle = createButtonStyle(new Color(0.8f, 0.2f, 0.2f, 0.8f));

        confirmButton = new TextButton("XÁC NHẬN ĐÃ CHUYỂN", confirmButtonStyle);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleConfirmPayment();
            }
        });

        cancelButton = new TextButton("HỦY", cancelButtonStyle);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleCancelPayment();
            }
        });

        buttonTable.add(confirmButton).width(150).height(40).padRight(10);
        buttonTable.add(cancelButton).width(100).height(40);

        contentTable.add(buttonTable).colspan(2).center();

        this.getContentTable().add(contentTable);

        this.pack();
        this.setWidth(Math.max(this.getWidth(), 400));
        this.setHeight(Math.max(this.getHeight(), 550));
    }

    private TextButton.TextButtonStyle createButtonStyle(Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(120, 40,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, 120, 40);

        pixmap.setColor(new Color(color.r * 0.8f, color.g * 0.8f, color.b * 0.8f, 1f));
        pixmap.drawRectangle(0, 0, 119, 39);

        Texture buttonTexture = new Texture(pixmap);
        pixmap.dispose();

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        style.down = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        style.font = dialogFont;
        style.fontColor = Color.WHITE;

        return style;
    }

    private void handleConfirmPayment() {
        // Hiển thị "Đang kiểm tra giao dịch..." trước
        waitingLabel.setText("Đang kiểm tra giao dịch...");
        waitingLabel.setColor(Color.ORANGE);
        confirmButton.setDisabled(true);

        // Kiểm tra với server xác thực
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.postRunnable(() -> {
                    checkPaymentWithServer();
                });
            }
        }, 2.0f);
    }

    private void checkPaymentWithServer() {
        // Chạy trong thread riêng để không block UI
        new Thread(() -> {
            try {
                boolean isAuthenticated = checkAuthenticationServer();

                // Quay lại main thread để update UI
                Gdx.app.postRunnable(() -> {
                    if (isAuthenticated) {
                        // Thanh toán thành công
                        waitingLabel.setText("Giao dịch thành công!");
                        waitingLabel.setColor(Color.GREEN);

                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                Gdx.app.postRunnable(() -> {
                                    if (callback != null) {
                                        callback.onPaymentConfirmed(orderId, itemId);
                                    }
                                    hide();
                                });
                            }
                        }, 1.0f);

                    } else {
                        // Giao dịch chưa được xác thực
                        waitingLabel.setText("Giao dịch chưa hoàn thành!");
                        waitingLabel.setColor(Color.RED);

                        // Enable lại nút sau 2 giây
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                Gdx.app.postRunnable(() -> {
                                    confirmButton.setDisabled(false);
                                    waitingLabel.setText("Vui lòng chuyển khoản và bấm 'Xác nhận'");
                                    waitingLabel.setColor(Color.YELLOW);
                                });
                            }
                        }, 2.0f);
                    }
                });

            } catch (Exception e) {
                Gdx.app.error("QRPaymentDialog", "Error checking authentication server: " + e.getMessage());

                Gdx.app.postRunnable(() -> {
                    waitingLabel.setText("Lỗi kết nối server! Thử lại sau.");
                    waitingLabel.setColor(Color.RED);

                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            Gdx.app.postRunnable(() -> {
                                confirmButton.setDisabled(false);
                                waitingLabel.setText("Vui lòng chuyển khoản và bấm 'Xác nhận'");
                                waitingLabel.setColor(Color.YELLOW);
                            });
                        }
                    }, 2.0f);
                });
            }
        }).start();
    }

    private boolean checkAuthenticationServer() throws IOException {
        String primaryHost = getServerHost();

        String[] possibleHosts = {
            primaryHost,           // IP được detect
            "localhost",          // Local
            "127.0.0.1",         // Local IP
            "10.0.2.2",          // Emulator
            "192.168.1.1",       // Router
            "192.168.0.1",       // Router khác
            "192.168.1.100",     // Common IP 1
            "192.168.1.101",     // Common IP 2
            "192.168.1.102",     // Common IP 3
            "192.168.0.100",     // Common IP 4
            "192.168.0.101"      // Common IP 5
        };

        for (String host : possibleHosts) {
            try {
                Gdx.app.log("QRPaymentDialog", "🔍 Trying: " + host + ":" + AUTH_SERVER_PORT);

                if (testConnection(host)) {
                    Gdx.app.log("QRPaymentDialog", "✅ Connected to: " + host);
                    return performAuthCheck(host);
                } else {
                    Gdx.app.log("QRPaymentDialog", "❌ Failed: " + host);
                }

            } catch (Exception e) {
                Gdx.app.log("QRPaymentDialog", "❌ Error connecting to " + host + ": " + e.getMessage());
            }
        }

        throw new IOException("❌ Could not connect to any server. Check if PaymentAuthServer is running!");
    }


    private boolean testConnection(String host) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, AUTH_SERVER_PORT), 2000); // 2s timeout
            Gdx.app.log("QRPaymentDialog", "✅ Test connection to " + host + " successful");
            return true;
        } catch (Exception e) {
            Gdx.app.log("QRPaymentDialog", "❌ Test connection to " + host + " failed: " + e.getMessage());
            return false;
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (Exception e) {}
            }
        }
    }

    private boolean performAuthCheck(String host) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(host, AUTH_SERVER_PORT);
            socket.setSoTimeout(5000);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("CHECK_PAYMENT:" + orderId);
            String response = in.readLine();

            Gdx.app.log("QRPaymentDialog", "Server response: " + response);
            return "SUCCESS".equals(response);

        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }


    private void handleCancelPayment() {
        if (callback != null) {
            callback.onPaymentCancelled();
        }
        hide();
    }

    public void showCentered(Stage stage) {
        this.show(stage);
        this.setPosition(
            (stage.getWidth() - this.getWidth()) / 2,
            (stage.getHeight() - this.getHeight()) / 2
        );
    }

    @Override
    public void hide() {
        super.hide();
        dispose();
    }

    public void dispose() {
        if (dialogFont != null) dialogFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (qrCodeTexture != null) qrCodeTexture.dispose();
        if (getSkin() != null) getSkin().dispose();
    }
}
