package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AchievementScreen implements Screen {
    final RaindropGame game;
    private ScoreHistoryManager scoreHistoryManager;

    // Camera và input
    private OrthographicCamera camera;
    private Vector3 touchPos;

    // UI Elements
    private Texture backgroundImage;
    private Texture panelImage;
    private Rectangle backButton;
    private Rectangle clearButton;

    // Fonts
    private BitmapFont vietnameseFont;
    private BitmapFont vietnameseTitleFont;
    private BitmapFont vietnameseSmallFont;

    // Colors
    private Color backButtonColor;
    private Color clearButtonColor;
    private Color buttonTextColor;
    private Color panelColor;

    // Scroll variables
    private float scrollY = 0;
    private float maxScrollY = 0;
    private static final float SCROLL_SPEED = 300f;
    private static final float ROW_HEIGHT = 50f;

    // Data
    private List<ScoreHistoryManager.ScoreRecord> scoreHistory;
    private boolean isLoading = true;
    private String errorMessage = null;

    public AchievementScreen(final RaindropGame game) {
        this.game = game;

        // Get ScoreHistoryManager from game
        this.scoreHistoryManager = game.scoreHistoryManager;

        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        touchPos = new Vector3();

        // Load assets
        loadAssets();

        // Initialize UI elements
        initializeUI();

        // Create fonts with error handling
        createVietnameseFonts();

        // Load score history
        loadScoreHistory();
    }

    private void loadAssets() {
        try {
            backgroundImage = new Texture(Gdx.files.internal("backgr-play.png"));
            panelImage = new Texture(Gdx.files.internal("pause_panel.png"));
        } catch (Exception e) {
            Gdx.app.error("AchievementScreen", "Error loading assets: " + e);
        }
    }

    private void initializeUI() {
        // Back button
        backButton = new Rectangle();
        backButton.width = 100;
        backButton.height = 40;
        backButton.x = 20;
        backButton.y = RaindropGame.GAME_HEIGHT - 60;

        // Clear button
        clearButton = new Rectangle();
        clearButton.width = 120;
        clearButton.height = 40;
        clearButton.x = RaindropGame.GAME_WIDTH - 140;
        clearButton.y = RaindropGame.GAME_HEIGHT - 60;

        // Colors - THAY ĐỔI ĐỂ GIỐNG STORESCREEN
        backButtonColor = new Color(0.9f, 0.7f, 0.4f, 0.8f); // Màu cam giống StoreScreen
        clearButtonColor = new Color(0.9f, 0.7f, 0.4f, 0.8f); // Cùng màu cam
        buttonTextColor = new Color(0.2f, 0.1f, 0f, 1); // Màu text nâu đậm giống StoreScreen
        panelColor = new Color(0.76f, 0.6f, 0.42f, 1); // Brown
    }

    private void createVietnameseFonts() {
        try {
            // Try to create custom fonts first
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Arial-Unicode.ttf"));

            // Regular font
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 18;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = new Color(0, 0, 0, 0.3f);
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";
            vietnameseFont = generator.generateFont(parameter);

            // Title font
            parameter.size = 24;
            parameter.borderWidth = 2;
            vietnameseTitleFont = generator.generateFont(parameter);

            // Small font
            parameter.size = 14;
            parameter.borderWidth = 1;
            vietnameseSmallFont = generator.generateFont(parameter);

            generator.dispose();

            Gdx.app.log("AchievementScreen", "Vietnamese fonts created successfully");

        } catch (Exception e) {
            Gdx.app.error("AchievementScreen", "Error creating Vietnamese fonts: " + e);

            // Fallback to default fonts
            createDefaultFonts();
        }
    }

    private void createDefaultFonts() {
        try {
            vietnameseFont = new BitmapFont();
            vietnameseFont.getData().setScale(1.2f);
            vietnameseFont.setColor(Color.WHITE);

            vietnameseTitleFont = new BitmapFont();
            vietnameseTitleFont.getData().setScale(1.5f);
            vietnameseTitleFont.setColor(Color.WHITE);

            vietnameseSmallFont = new BitmapFont();
            vietnameseSmallFont.getData().setScale(1.0f);
            vietnameseSmallFont.setColor(Color.WHITE);

            Gdx.app.log("AchievementScreen", "Default fonts created successfully");

        } catch (Exception e) {
            Gdx.app.error("AchievementScreen", "Error creating default fonts: " + e);

            // Last resort - use game's font
            vietnameseFont = game.font;
            vietnameseTitleFont = game.font;
            vietnameseSmallFont = game.font;
        }
    }

    private void loadScoreHistory() {
        isLoading = true;
        errorMessage = null;

        if (scoreHistoryManager == null) {
            errorMessage = "ScoreHistoryManager not initialized";
            isLoading = false;
            scoreHistory = new ArrayList<>();
            return;
        }

        scoreHistoryManager.getScoreHistory(new ScoreHistoryManager.ScoreHistoryCallback() {
            @Override
            public void onSuccess(List<ScoreHistoryManager.ScoreRecord> history) {
                scoreHistory = history;
                isLoading = false;

                // Calculate max scroll
                maxScrollY = Math.max(0, (scoreHistory.size() + 2) * ROW_HEIGHT - 400);
            }

            @Override
            public void onFailure(String error) {
                errorMessage = error;
                isLoading = false;
                scoreHistory = new ArrayList<>();
            }
        });
    }

    private void clearScoreHistory() {
        if (scoreHistoryManager == null) {
            errorMessage = "Cannot clear history - ScoreHistoryManager not initialized";
            return;
        }

        scoreHistoryManager.clearHistory(new ScoreHistoryManager.SaveScoreCallback() {
            @Override
            public void onSuccess() {
                loadScoreHistory(); // Reload after clearing
            }

            @Override
            public void onFailure(String error) {
                errorMessage = "Khong the xoa lich su: " + error; // Simplified Vietnamese
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        handleInput();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Draw background
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        }

        // Draw main panel
        drawMainPanel();

        // Draw buttons
        drawButtons();

        // Draw content
        if (isLoading) {
            drawLoadingMessage();
        } else if (errorMessage != null) {
            drawErrorMessage();
        } else {
            drawScoreHistory();
        }

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (backButton.contains(touchPos.x, touchPos.y)) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }

            if (clearButton.contains(touchPos.x, touchPos.y)) {
                clearScoreHistory();
                return;
            }
        }

        // Handle scrolling
        if (Gdx.input.isTouched() && !isLoading && scoreHistory != null && scoreHistory.size() > 8) {
            float deltaY = Gdx.input.getDeltaY();
            scrollY += deltaY * 2; // Scroll sensitivity

            // Clamp scroll
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        }
    }

    private void drawMainPanel() {
        float panelWidth = RaindropGame.GAME_WIDTH - 40;
        float panelHeight = RaindropGame.GAME_HEIGHT - 120;
        float panelX = 20;
        float panelY = 40;

        // Draw panel background
        game.batch.setColor(panelColor);
        if (panelImage != null) {
            game.batch.draw(panelImage, panelX, panelY, panelWidth, panelHeight);
        }
        game.batch.setColor(1, 1, 1, 1);

        // Draw title
        if (vietnameseTitleFont != null) {
            vietnameseTitleFont.setColor(1, 1, 0, 1); // Yellow
            vietnameseTitleFont.draw(game.batch, "THÀNH TÍCH", // Simplified Vietnamese
                RaindropGame.GAME_WIDTH / 2f - 60, RaindropGame.GAME_HEIGHT - 90);
            vietnameseTitleFont.setColor(1, 1, 1, 1);
        }
    }

    private void drawButtons() {
        // Back button - màu cam
        game.batch.setColor(backButtonColor);
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, backButton.x, backButton.y, backButton.width, backButton.height);
        }

        // Clear button - cùng màu cam
        game.batch.setColor(clearButtonColor);
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, clearButton.x, clearButton.y, clearButton.width, clearButton.height);
        }

        // Reset color for text - màu text nâu đậm
        game.batch.setColor(buttonTextColor);

        // Button texts - căn giữa text trong nút
        if (vietnameseFont != null) {
            String backText = "BACK";
            String clearText = "CLEAR";

            // Căn giữa text trong nút back
            float backTextWidth = vietnameseFont.draw(game.batch, "", 0, 0).width;
            vietnameseFont.draw(game.batch, backText,
                backButton.x + (backButton.width - backTextWidth) / 6,
                backButton.y + backButton.height / 2 + 5);

            // Căn giữa text trong nút clear
            float clearTextWidth = vietnameseFont.draw(game.batch, "", 0, 0).width;
            vietnameseFont.draw(game.batch, clearText,
                clearButton.x + (clearButton.width - clearTextWidth) / 6,
                clearButton.y + clearButton.height / 2 + 5);
        }

        // Reset color
        game.batch.setColor(1, 1, 1, 1);
    }

    private void drawLoadingMessage() {
        if (vietnameseFont != null) {
            vietnameseFont.draw(game.batch, "Dang tai...",
                RaindropGame.GAME_WIDTH / 2f - 50, RaindropGame.GAME_HEIGHT / 2f);
        }
    }

    private void drawErrorMessage() {
        if (vietnameseFont != null) {
            vietnameseFont.setColor(1, 0, 0, 1); // Red
            vietnameseFont.draw(game.batch, "Loi: " + errorMessage,
                50, RaindropGame.GAME_HEIGHT / 2f);
            vietnameseFont.setColor(1, 1, 1, 1);
        }
    }

    private void drawScoreHistory() {
        if (scoreHistory == null || scoreHistory.isEmpty()) {
            if (vietnameseFont != null) {
                vietnameseFont.draw(game.batch, "Chua co thanh tich nao",
                    RaindropGame.GAME_WIDTH / 2f - 100, RaindropGame.GAME_HEIGHT / 2f);
            }
            return;
        }

        // Draw table headers
        float startY = RaindropGame.GAME_HEIGHT - 160;
        float headerY = startY + scrollY;

        if (vietnameseFont != null) {
            vietnameseFont.setColor(1, 1, 0, 1); // Yellow headers
            vietnameseFont.draw(game.batch, "STT", 50, headerY);
            vietnameseFont.draw(game.batch, "Ten", 100, headerY);
            vietnameseFont.draw(game.batch, "Diem", 200, headerY);
            vietnameseFont.draw(game.batch, "Thoi gian", 280, headerY);
            vietnameseFont.setColor(1, 1, 1, 1);
        }

        // Draw score records
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (int i = 0; i < scoreHistory.size(); i++) {
            ScoreHistoryManager.ScoreRecord record = scoreHistory.get(i);
            float rowY = headerY - (i + 1) * ROW_HEIGHT;

            // Skip rows that are outside visible area
            if (rowY < 40 || rowY > RaindropGame.GAME_HEIGHT) {
                continue;
            }

            // Highlight top 3 scores
            if (vietnameseSmallFont != null) {
                if (i < 3) {
                    Color highlightColor = i == 0 ? Color.GOLD :
                        i == 1 ? Color.LIGHT_GRAY :
                            new Color(0.8f, 0.5f, 0.2f, 1); // Bronze
                    vietnameseSmallFont.setColor(highlightColor);
                } else {
                    vietnameseSmallFont.setColor(1, 1, 1, 1);
                }

                // Draw row data
                vietnameseSmallFont.draw(game.batch, String.valueOf(i + 1), 50, rowY);
                vietnameseSmallFont.draw(game.batch, record.playerName, 100, rowY);
                vietnameseSmallFont.draw(game.batch, String.valueOf(record.score), 200, rowY);
                vietnameseSmallFont.draw(game.batch, dateFormat.format(new Date(record.timestamp)), 280, rowY);
            }
        }

        if (vietnameseFont != null) {
            vietnameseFont.setColor(1, 1, 1, 1); // Reset color
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        if (backgroundImage != null) backgroundImage.dispose();
        if (panelImage != null) panelImage.dispose();
        if (vietnameseFont != null && vietnameseFont != game.font) vietnameseFont.dispose();
        if (vietnameseTitleFont != null && vietnameseTitleFont != game.font) vietnameseTitleFont.dispose();
        if (vietnameseSmallFont != null && vietnameseSmallFont != game.font) vietnameseSmallFont.dispose();
    }
}
