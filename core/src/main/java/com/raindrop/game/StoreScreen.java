package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.raindrop.game.payment.VNPayHelper;

import java.util.HashMap;
import java.util.Map;


public class StoreScreen implements Screen {
    final RaindropGame game;
    OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private BitmapFont storeFont;
    private BitmapFont titleFont;
    private ItemManager itemManager;
    private ScoreManager scoreManager;
    private PaymentInterface paymentInterface;
    private long playerTomatoes = 0;
    private QRPaymentDialog currentPaymentDialog;
    private String pendingOrderId;
    private String pendingItemId;

    // VNPay items với giá tiền thật
    private Map<String, Integer> vnpayPrices;
    private TextButton spaceshipButton;

    // Textures
    private Texture backgroundImage;
    private Texture panelTexture;
    private Texture buttonTexture;
    private Texture buttonDownTexture;
    private Texture buttonDisabledTexture;
    private Texture buttonEquippedTexture;

    // Store items textures
    private Map<String, Texture> itemTextures;
    private Texture tomatoTexture;

    // UI Components
    private TextButton backButton;
    private Label tomatoCountLabel;
    private Label messageLabel;
    private Map<String, TextButton> itemButtons;

    // Button styles
    private TextButtonStyle buyButtonStyle;
    private TextButtonStyle useButtonStyle;
    private TextButtonStyle usingButtonStyle;
    private TextButtonStyle disabledButtonStyle;

    public StoreScreen(final RaindropGame game) {
        this.game = game;
        this.itemManager = game.itemManager;
        this.scoreManager = game.scoreManager;
        this.paymentInterface = game.paymentManager; // Lấy từ game instance
        this.itemButtons = new HashMap<>();
        initializeVNPayPrices();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);

        viewport = new FitViewport(RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT, camera);
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        loadAssets();
        createFonts();
        loadPlayerTomatoes();
        createStoreUI();
    }
    public Stage getStage() {
        return stage;
    }

    private void initializeVNPayPrices() {
        vnpayPrices = new HashMap<>();
        vnpayPrices.put("spaceship", 5000); // 5,000 VND
        // Có thể thêm các vật phẩm khác cần mua bằng tiền thật
        // vnpayPrices.put("premium_bucket", 10000); // 10,000 VND
    }

    private void loadAssets() {
        try {
            backgroundImage = new Texture(Gdx.files.internal("background-screen.png"));
            panelTexture = new Texture(Gdx.files.internal("pause_panel.png"));
            tomatoTexture = new Texture(Gdx.files.internal("tomato.png"));

            // Load item textures
            itemTextures = new HashMap<>();
            for (ItemManager.Item item : itemManager.getAllItems().values()) {
                try {
                    itemTextures.put(item.id, new Texture(Gdx.files.internal(item.texturePath)));
                } catch (Exception e) {
                    Gdx.app.error("StoreScreen", "Could not load texture: " + item.texturePath);
                }
            }

            // Create button textures with different colors
            buttonTexture = createButtonTexture(120, 40, new Color(0.8f, 0.6f, 0.3f, 0.8f)); // Sand color
            buttonDownTexture = createButtonTexture(120, 40, new Color(0.7f, 0.5f, 0.2f, 0.9f)); // Darker sand
            buttonDisabledTexture = createButtonTexture(120, 40, new Color(0.5f, 0.5f, 0.5f, 0.6f)); // Gray
            buttonEquippedTexture = createButtonTexture(120, 40, new Color(0.2f, 0.8f, 0.2f, 0.8f)); // Green
        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error loading assets: " + e);
        }
    }

    private void createFonts() {
        try {
            // Check if freetype is available
            if (!Gdx.files.internal("fonts/Arial-Unicode.ttf").exists()) {
                Gdx.app.error("StoreScreen", "Font file not found, using default fonts");
                storeFont = new BitmapFont();
                titleFont = new BitmapFont();
                return;
            }

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Arial-Unicode.ttf"));

            FreeTypeFontParameter itemParameter = new FreeTypeFontParameter();
            itemParameter.size = 18;
            itemParameter.color = new Color(0.2f, 0.1f, 0f, 1);
            // Reduced character set to avoid memory issues
            itemParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
                "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";

            storeFont = generator.generateFont(itemParameter);

            FreeTypeFontParameter titleParameter = new FreeTypeFontParameter();
            titleParameter.size = 32;
            titleParameter.color = new Color(0.2f, 0.1f, 0f, 1);
            titleParameter.borderWidth = 1;
            titleParameter.borderColor = new Color(0, 0, 0, 0.3f);
            titleParameter.characters = itemParameter.characters;
            titleFont = generator.generateFont(titleParameter);

            generator.dispose();
            Gdx.app.log("StoreScreen", "Fonts created successfully");

        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error creating fonts: " + e.getMessage());
            // Fallback to default fonts
            storeFont = new BitmapFont();
            titleFont = new BitmapFont();

            // Set colors for default fonts
            storeFont.setColor(new Color(0.2f, 0.1f, 0f, 1));
            titleFont.setColor(new Color(0.2f, 0.1f, 0f, 1));
        }
    }

    private void loadPlayerTomatoes() {
        if (scoreManager != null) {
            scoreManager.getCurrentTotalScore(new ScoreManager.ScoreCallback() {
                @Override
                public void onSuccess(long currentTotal) {
                    playerTomatoes = currentTotal;
                    updateTomatoDisplay();
                    Gdx.app.log("StoreScreen", "Loaded tomatoes: " + playerTomatoes);
                }

                @Override
                public void onFailure(String error) {
                    Gdx.app.error("StoreScreen", "Failed to load tomatoes: " + error);
                    playerTomatoes = 0;
                    updateTomatoDisplay();
                }
            });
        }
    }

    private Texture createButtonTexture(int width, int height, Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);

        pixmap.setColor(new Color(0.9f, 0.7f, 0.4f, 1f));
        pixmap.drawRectangle(0, 0, width-1, height-1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void createButtonStyles() {
        // Buy button style - màu cam giống phi thuyền
        buyButtonStyle = new TextButtonStyle();
        buyButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        buyButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        buyButtonStyle.font = storeFont;
        buyButtonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1);

        // Use button style - màu xanh dương
        useButtonStyle = new TextButtonStyle();
        useButtonStyle.up = new TextureRegionDrawable(new TextureRegion(createButtonTexture(120, 40, new Color(0.2f, 0.5f, 0.8f, 0.8f))));
        useButtonStyle.down = new TextureRegionDrawable(new TextureRegion(createButtonTexture(120, 40, new Color(0.1f, 0.4f, 0.7f, 0.9f))));
        useButtonStyle.font = storeFont;
        useButtonStyle.fontColor = Color.WHITE;

        // Using button style - màu xanh lá
        usingButtonStyle = new TextButtonStyle();
        usingButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonEquippedTexture));
        usingButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonEquippedTexture));
        usingButtonStyle.font = storeFont;
        usingButtonStyle.fontColor = Color.WHITE;

        // Disabled button style - màu xám
        disabledButtonStyle = new TextButtonStyle();
        disabledButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonDisabledTexture));
        disabledButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDisabledTexture));
        disabledButtonStyle.font = storeFont;
        disabledButtonStyle.fontColor = new Color(0.3f, 0.3f, 0.3f, 1);
    }

    private void createStoreUI() {
        createButtonStyles();

        Table mainTable = new Table();
        mainTable.setFillParent(true);

        Image panelImage = new Image(panelTexture);
        float panelWidth = RaindropGame.GAME_WIDTH * 0.85f;
        float panelHeight = RaindropGame.GAME_HEIGHT * 0.85f;

        Table containerTable = new Table();
        containerTable.setFillParent(true);

        Table storeTable = new Table();

        // Title
        LabelStyle titleStyle = new LabelStyle(titleFont, Color.WHITE);
        Label titleLabel = new Label("CỬA HÀNG", titleStyle);

        // Tomato count display
        LabelStyle itemStyle = new LabelStyle(storeFont, Color.WHITE);
        Table tomatoDisplay = new Table();
        tomatoCountLabel = new Label("0", itemStyle);
        Image tomatoIcon = new Image(tomatoTexture);
        tomatoDisplay.add(tomatoIcon).width(24).height(24).padRight(5);
        tomatoDisplay.add(tomatoCountLabel);

        // Message label for notifications
        messageLabel = new Label("", new LabelStyle(storeFont, Color.RED));
        messageLabel.setVisible(false);

        // Items grid
        Table itemsTable = new Table();
        createItemsGrid(itemsTable, itemStyle);

        // Back button
        TextButtonStyle backButtonStyle = new TextButtonStyle();
        backButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        backButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        backButtonStyle.font = storeFont;
        backButtonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1);

        backButton = new TextButton("QUAY LẠI", backButtonStyle);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        // Layout
        storeTable.add(titleLabel).padTop(20).padBottom(20);
        storeTable.row();
        storeTable.add(tomatoDisplay).padBottom(10);
        storeTable.row();
        storeTable.add(messageLabel).padBottom(10);
        storeTable.row();
        storeTable.add(itemsTable);
        storeTable.row();
        storeTable.add(backButton).width(140).height(40).padTop(30);

        mainTable.add(panelImage).width(panelWidth).height(panelHeight);
        containerTable.add(storeTable).expand().center();

        stage.addActor(mainTable);
        stage.addActor(containerTable);

        updateTomatoDisplay();
    }

    private void createItemsGrid(Table itemsTable, LabelStyle labelStyle) {
        // First row (Bowl and Box)
        Table row1 = new Table();
        addStoreItem(row1, "bowl", labelStyle);
        row1.add().width(40);
        addStoreItem(row1, "box", labelStyle);

        // Second row (Bucket and Spaceship)
        Table row2 = new Table();
        addStoreItem(row2, "bucket", labelStyle);
        row2.add().width(40);

        // Spaceship (special item with money price)
        addSpaceshipItem(row2, labelStyle);

        itemsTable.add(row1).padBottom(20);
        itemsTable.row();
        itemsTable.add(row2);
    }

    private void addStoreItem(Table itemsTable, String itemId, LabelStyle labelStyle) {
        ItemManager.Item item = itemManager.getItem(itemId);
        if (item == null) return;

        Table itemTable = new Table();

        // Item image
        Texture itemTexture = itemTextures.get(itemId);
        if (itemTexture != null) {
            Image itemImage = new Image(itemTexture);
            itemTable.add(itemImage).width(90).height(90).padBottom(5);
            itemTable.row();
        }

        // Item name
        Label nameLabel = new Label(item.name, labelStyle);
        itemTable.add(nameLabel).padBottom(5);
        itemTable.row();

        // Price with tomato icon
        Table priceTable = new Table();
        Label priceLabel = new Label(String.valueOf(item.price) + " ", labelStyle);
        Image tomatoIcon = new Image(tomatoTexture);
        priceTable.add(priceLabel);
        priceTable.add(tomatoIcon).width(16).height(16);
        itemTable.add(priceTable).padBottom(5);
        itemTable.row();

        // Button
        TextButton itemButton = createItemButton(item);
        itemButtons.put(itemId, itemButton);
        itemTable.add(itemButton).width(100).height(35);

        itemsTable.add(itemTable).pad(10);
    }

    private TextButton createItemButton(final ItemManager.Item item) {
        String buttonText;
        TextButtonStyle buttonStyle;

        if (itemManager.isItemEquipped(item.id)) {
            buttonText = "ĐANG SỬ DỤNG";
            buttonStyle = usingButtonStyle;
        } else if (itemManager.isItemPurchased(item.id)) {
            buttonText = "SỬ DỤNG";
            buttonStyle = useButtonStyle;
        } else if (itemManager.canPurchase(item.id, playerTomatoes)) {
            buttonText = "MUA";
            buttonStyle = buyButtonStyle;
        } else {
            buttonText = "MUA";
            buttonStyle = disabledButtonStyle;
        }

        TextButton button = new TextButton(buttonText, buttonStyle);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleItemButtonClick(item);
            }
        });

        return button;
    }

    private void handleItemButtonClick(ItemManager.Item item) {
        if (itemManager.isItemEquipped(item.id)) {
            // Already equipped, do nothing
            return;
        }

        if (itemManager.isItemPurchased(item.id)) {
            // Use item
            itemManager.equipItem(item.id);
            showMessage("Đã trang bị " + item.name + "!", Color.GREEN);
            updateAllItemButtons();
        } else if (itemManager.canPurchase(item.id, playerTomatoes)) {
            // Purchase item
            purchaseItem(item);
        } else {
            // Cannot afford
            showMessage("Bạn không đủ cà chua để mua vật phẩm!", Color.RED);
        }
    }

    private void purchaseItem(final ItemManager.Item item) {
        if (scoreManager != null) {
            // Deduct tomatoes from Firebase - truyền số âm để trừ đi
            scoreManager.addScoreToTotal(-item.price, new ScoreManager.ScoreCallback() {
                @Override
                public void onSuccess(long newTotal) {
                    // Purchase successful
                    itemManager.purchaseItem(item.id, playerTomatoes);
                    playerTomatoes = newTotal;
                    updateTomatoDisplay();
                    showMessage("Mua thành công " + item.name + "!", Color.GREEN);
                    updateItemButton(item.id);
                    Gdx.app.log("StoreScreen", "Purchased " + item.name + ", remaining tomatoes: " + newTotal);
                }

                @Override
                public void onFailure(String error) {
                    showMessage("Lỗi khi mua vật phẩm!", Color.RED);
                    Gdx.app.error("StoreScreen", "Purchase failed: " + error);
                }
            });
        }
    }

    private void addSpaceshipItem(Table itemsTable, LabelStyle labelStyle) {
        Table spaceshipTable = new Table();

        Texture spaceshipTexture = itemTextures.get("spaceship");
        if (spaceshipTexture == null) {
            try {
                spaceshipTexture = new Texture(Gdx.files.internal("spaceship.png"));
                itemTextures.put("spaceship", spaceshipTexture);
            } catch (Exception e) {
                Gdx.app.error("StoreScreen", "Could not load spaceship texture");
            }
        }

        if (spaceshipTexture != null) {
            Image spaceshipImage = new Image(spaceshipTexture);
            spaceshipTable.add(spaceshipImage).width(90).height(90).padBottom(5);
            spaceshipTable.row();
        }

        Label spaceshipLabel = new Label("Phi thuyen", labelStyle);
        spaceshipTable.add(spaceshipLabel).padBottom(5);
        spaceshipTable.row();

        // Hiển thị giá tiền thật
        int price = vnpayPrices.get("spaceship");
        Label spaceshipPrice = new Label(VNPayHelper.formatVND(price), labelStyle);
        spaceshipPrice.setColor(Color.GOLD);
        spaceshipTable.add(spaceshipPrice).padBottom(5);
        spaceshipTable.row();

        // Tạo button và lưu reference
        spaceshipButton = createSpaceshipButton();
        spaceshipTable.add(spaceshipButton).width(100).height(35);
        itemsTable.add(spaceshipTable);
    }

    // Replace the showPaymentDialog method in StoreScreen with this:
    // Fixed showPaymentDialog method in StoreScreen.java




    // Giả lập xác thực giao dịch
    private boolean verifyPayment(String orderId, String itemId) {
        // Thực tế: Gọi API backend để kiểm tra giao dịch
        // Hiện tại: Giả lập thành công (70% success rate)
        return Math.random() > 0.3;
    }


    // Tạo method riêng để tạo spaceship button
    private TextButton createSpaceshipButton() {
        TextButton button;

        try {
            if (itemManager.isItemPurchased("spaceship")) {
                if (itemManager.isItemEquipped("spaceship")) {
                    button = new TextButton("ĐANG SỬ DỤNG", usingButtonStyle);
                } else {
                    button = new TextButton("SỬ DỤNG", useButtonStyle);
                    button.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            try {
                                itemManager.equipItem("spaceship");
                                showMessage("Đã trang bị Phi thuyen!", Color.GREEN);
                                updateAllItemButtons(); // Cập nhật tất cả buttons
                            } catch (Exception e) {
                                Gdx.app.error("StoreScreen", "Error equipping spaceship: " + e.getMessage());
                                showMessage("Lỗi khi trang bị phi thuyen!", Color.RED);
                            }
                        }
                    });
                }
            } else {
                button = new TextButton("MUA", buyButtonStyle);
                button.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        try {
                            new QRPaymentDialog("Phi thuyền", "spaceship", 5000, new QRPaymentDialog.QRPaymentCallback() {
                                @Override
                                public void onPaymentConfirmed(String orderId, String itemId) {
                                    // Mua item
                                    itemManager.purchaseItem("spaceship", 0);

                                    // Debug log
                                    Gdx.app.log("StoreScreen", "QR Payment confirmed for spaceship");
                                    Gdx.app.log("StoreScreen", "Is purchased: " + itemManager.isItemPurchased("spaceship"));

                                    // Hiển thị thông báo
                                    showMessage("Thanh toán thành công! Bạn đã nhận được Phi thuyền", Color.GREEN);

                                    // QUAN TRỌNG: Cập nhật UI trên main thread
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Cập nhật lại toàn bộ UI
                                            updateAllItemButtons();

                                            // Double check - force update spaceship button
                                            updateSpaceshipButton();
                                        }
                                    });
                                }

                                @Override
                                public void onPaymentCancelled() {
                                    showMessage("Đã hủy thanh toán", Color.YELLOW);
                                }
                            }, getStage());
                        } catch (Exception e) {
                            Gdx.app.error("StoreScreen", "Error showing QR payment dialog: " + e.getMessage());
                            showMessage("Lỗi khi hiển thị thanh toán!", Color.RED);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error creating spaceship button: " + e.getMessage());
            button = new TextButton("LỖI", disabledButtonStyle);
        }

        return button;
    }

    private void updateSpaceshipButton() {
        if (spaceshipButton == null) {
            Gdx.app.error("StoreScreen", "Spaceship button is null!");
            return;
        }

        try {
            // Debug log
            boolean isPurchased = itemManager.isItemPurchased("spaceship");
            boolean isEquipped = itemManager.isItemEquipped("spaceship");
            Gdx.app.log("StoreScreen", "Updating spaceship button - Purchased: " + isPurchased + ", Equipped: " + isEquipped);

            spaceshipButton.clearListeners(); // Clear listeners first

            if (isPurchased) {
                if (isEquipped) {
                    spaceshipButton.setText("ĐANG SỬ DỤNG");
                    spaceshipButton.setStyle(usingButtonStyle);
                    Gdx.app.log("StoreScreen", "Spaceship button updated to: ĐANG SỬ DỤNG");
                } else {
                    spaceshipButton.setText("SỬ DỤNG");
                    spaceshipButton.setStyle(useButtonStyle);

                    // Add listener for equip
                    spaceshipButton.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            itemManager.equipItem("spaceship");
                            showMessage("Đã trang bị Phi thuyen!", Color.GREEN);
                            updateAllItemButtons();
                        }
                    });
                    Gdx.app.log("StoreScreen", "Spaceship button updated to: SỬ DỤNG");
                }
            } else {
                spaceshipButton.setText("MUA");
                spaceshipButton.setStyle(buyButtonStyle);

                // Add listener for purchase
                spaceshipButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        new QRPaymentDialog("Phi thuyền", "spaceship", 5000, new QRPaymentDialog.QRPaymentCallback() {
                            @Override
                            public void onPaymentConfirmed(String orderId, String itemId) {
                                itemManager.purchaseItem("spaceship", 0);
                                showMessage("Thanh toán thành công! Bạn đã nhận được Phi thuyền", Color.GREEN);

                                // Update UI on main thread
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateAllItemButtons();
                                    }
                                });
                            }

                            @Override
                            public void onPaymentCancelled() {
                                showMessage("Đã hủy thanh toán", Color.YELLOW);
                            }
                        }, getStage());
                    }
                });
                Gdx.app.log("StoreScreen", "Spaceship button updated to: MUA");
            }
        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error updating spaceship button: " + e.getMessage());
        }
    }




    // Xử lý khi thanh toán thành công
    private void handlePaymentSuccess(String transactionId, String orderId) {
        if (pendingItemId != null && orderId.equals(pendingOrderId)) {
            try {
                // Purchase item first
                itemManager.purchaseItem(pendingItemId, 0);

                // Save transaction record
                saveTransactionRecord(transactionId, orderId, pendingItemId);

                // Get item name for message
                String itemName = pendingItemId.equals("spaceship") ? "Phi thuyen" :
                    itemManager.getItem(pendingItemId).name;

                showMessage("Thanh toán thành công! Bạn đã nhận được " + itemName, Color.GREEN);

                // FIX: Force update spaceship button immediately after purchase
                if (pendingItemId.equals("spaceship")) {
                    // Ensure the button is updated to "SỬ DỤNG" state
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            updateSpaceshipButton();
                        }
                    });
                }

                // Update all other item buttons
                updateAllItemButtons();

                // Reset pending values
                pendingOrderId = null;
                pendingItemId = null;

                Gdx.app.log("StoreScreen", "Payment successful: " + transactionId);
            } catch (Exception e) {
                Gdx.app.error("StoreScreen", "Error processing payment success: " + e.getMessage());
                showMessage("Lỗi xử lý thanh toán thành công!", Color.RED);
            }
        } else {
            showMessage("Lỗi xác nhận giao dịch!", Color.RED);
            Gdx.app.error("StoreScreen", "Order ID mismatch: expected " + pendingOrderId + ", got " + orderId);
        }
    }

    private boolean isPaymentInterfaceReady() {
        if (paymentInterface == null) {
            Gdx.app.error("StoreScreen", "PaymentInterface is null");
            showMessage("Tính năng thanh toán chưa sẵn sàng!", Color.RED);
            return false;
        }
        return true;
    }

    // Xử lý khi thanh toán thất bại
    private void handlePaymentFailure(String errorMessage) {
        showMessage("Thanh toán thất bại: " + errorMessage, Color.RED);

        // Reset pending values
        pendingOrderId = null;
        pendingItemId = null;

        Gdx.app.error("StoreScreen", "Payment failed: " + errorMessage);
    }

    // Xử lý khi người dùng hủy thanh toán
    private void handlePaymentCancelled() {
        showMessage("Đã hủy thanh toán", Color.YELLOW);

        // Reset pending values
        pendingOrderId = null;
        pendingItemId = null;
    }

    // Lưu thông tin giao dịch
    private void saveTransactionRecord(String transactionId, String orderId, String itemId) {
        try {
            // Lưu vào SharedPreferences
            com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("payment_records");
            String key = "transaction_" + orderId;
            String value = transactionId + "," + itemId + "," + System.currentTimeMillis();
            prefs.putString(key, value);
            prefs.flush();

            Gdx.app.log("StoreScreen", "Transaction saved: " + orderId + " -> " + transactionId);

            // Có thể gửi lên server để verify
            // sendTransactionToServer(transactionId, orderId, itemId);

        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error saving transaction: " + e.getMessage());
        }
    }


    private void updateTomatoDisplay() {
        if (tomatoCountLabel != null) {
            tomatoCountLabel.setText(String.valueOf(playerTomatoes));
        }
    }

    private void updateItemButton(String itemId) {
        TextButton button = itemButtons.get(itemId);
        ItemManager.Item item = itemManager.getItem(itemId);
        if (button != null && item != null) {
            if (itemManager.isItemPurchased(item.id)) {
                button.setText("SỬ DỤNG");
                button.setStyle(useButtonStyle);
            }
        }
    }

    private void updateAllItemButtons() {
        try {
            // Cập nhật các item button thông thường (bowl, box, bucket)
            for (Map.Entry<String, TextButton> entry : itemButtons.entrySet()) {
                String itemId = entry.getKey();
                TextButton button = entry.getValue();
                ItemManager.Item item = itemManager.getItem(itemId);

                if (item != null) {
                    button.clearListeners(); // Clear old listeners

                    if (itemManager.isItemEquipped(item.id)) {
                        button.setText("ĐANG SỬ DỤNG");
                        button.setStyle(usingButtonStyle);
                    } else if (itemManager.isItemPurchased(item.id)) {
                        button.setText("SỬ DỤNG");
                        button.setStyle(useButtonStyle);
                        // Add listener for use button
                        final ItemManager.Item finalItem = item;
                        button.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                itemManager.equipItem(finalItem.id);
                                showMessage("Đã trang bị " + finalItem.name + "!", Color.GREEN);
                                updateAllItemButtons();
                            }
                        });
                    } else if (itemManager.canPurchase(item.id, playerTomatoes)) {
                        button.setText("MUA");
                        button.setStyle(buyButtonStyle);
                        // Add listener for buy button
                        final ItemManager.Item finalItem = item;
                        button.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                handleItemButtonClick(finalItem);
                            }
                        });
                    } else {
                        button.setText("MUA");
                        button.setStyle(disabledButtonStyle);
                    }
                }
            }

            // QUAN TRỌNG: Cập nhật spaceship button
            updateSpaceshipButton();

            Gdx.app.log("StoreScreen", "All item buttons updated successfully");

        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error updating all item buttons: " + e.getMessage());
        }
    }

    private void showMessage(String text, Color color) {
        messageLabel.setText(text);
        messageLabel.getStyle().fontColor = color;
        messageLabel.setVisible(true);

        // Auto-hide message after 3 seconds
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            messageLabel.setVisible(false);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        }
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {


        // Existing dispose code...
        if (backgroundImage != null) backgroundImage.dispose();
        if (panelTexture != null) panelTexture.dispose();
        if (buttonTexture != null) buttonTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
        if (buttonDisabledTexture != null) buttonDisabledTexture.dispose();
        if (buttonEquippedTexture != null) buttonEquippedTexture.dispose();
        if (itemTextures != null) {
            for (Texture texture : itemTextures.values()) {
                if (texture != null) texture.dispose();
            }
        }
        if (tomatoTexture != null) tomatoTexture.dispose();
        if (storeFont != null) storeFont.dispose();
        if (titleFont != null) titleFont.dispose();
        stage.dispose();
    }
}
