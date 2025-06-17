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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {
    final RaindropGame game;
    OrthographicCamera camera;
    Texture backgroundImage;

    // Add stage for UI elements
    private Stage stage;
    private Viewport viewport;
    private Texture buttonTexture;
    private Texture buttonDownTexture;

    // Custom font
    private BitmapFont menuFont;
    private BitmapFont scoreFont; // Font for displaying total score

    // Score management
    private ScoreManager scoreManager;
    private long totalScore = 0;
    private Texture tomatoIcon; // Icon for tomato

    // UI Components - Add references to buttons
    private TextButton playButton;
    private TextButton storeButton;
    private TextButton achievementsButton;
    private TextButton settingsButton;
    private TextButton exitButton;

    // Track current language state
    private boolean currentLanguageIsVietnamese;

    // Add button style as class member để có thể update
    private TextButtonStyle buttonStyle;

    public MainMenuScreen(final RaindropGame game) {
        this.game = game;
        this.scoreManager = game.scoreManager; // Get ScoreManager from game instance

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);

        // Create viewport and stage for UI
        viewport = new FitViewport(RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT, camera);
        stage = new Stage(viewport, game.batch);

        // Enable stage to receive input events
        Gdx.input.setInputProcessor(stage);

        // Initialize current language state
        currentLanguageIsVietnamese = LocalizationManager.isVietnamese();

        try {
            // Load background image
            backgroundImage = new Texture(Gdx.files.internal("background-screen.png"));

            // Load tomato icon
            tomatoIcon = new Texture(Gdx.files.internal("tomato.png"));

            // Create button textures
            buttonTexture = createButtonTexture(200, 60, new Color(0.8f, 0.6f, 0.3f, 0.8f)); // Sand color
            buttonDownTexture = createButtonTexture(200, 60, new Color(0.7f, 0.5f, 0.2f, 0.9f)); // Darker sand color

            // Initialize custom font that supports Vietnamese - TẠO TRƯỚC KHI createMenu()
            createVietnameseFont();

            // Tạo button style sau khi đã có font
            createButtonStyle();

            createMenu();

            // Load total score from Firestore
            loadTotalScore();

        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading assets: " + e);
        }
    }

    private void loadTotalScore() {
        if (scoreManager != null) {
            scoreManager.getCurrentTotalScore(new ScoreManager.ScoreCallback() {
                @Override
                public void onSuccess(long currentTotal) {
                    totalScore = currentTotal;
                    Gdx.app.log("MainMenuScreen", "Loaded total score: " + totalScore);
                }

                @Override
                public void onFailure(String error) {
                    Gdx.app.error("MainMenuScreen", "Failed to load total score: " + error);
                    totalScore = 0; // Default to 0 if failed to load
                }
            });
        }
    }

    private void createVietnameseFont() {
        try {
            // Tạo font với FreeType để hỗ trợ tiếng Việt
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));

            // Menu font - tăng size để rõ hơn
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 20; // Tăng size lên một chút
            parameter.color = new Color(0.2f, 0.1f, 0f, 1); // Dark brown text để match với buttonStyle

            // QUAN TRỌNG: Thêm đầy đủ ký tự tiếng Việt
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
                "àáảãạâầấẩẫậăằắẳẵặèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ" +
                "ÀÁẢÃẠÂẦẤẨẪẬĂẰẮẲẴẶÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ" +
                "ÀÁẢÃẠÂẦẤẨẪẬĂẰẮẲẴẶÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ";

            // Bật antialiasing cho font đẹp hơn
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;

            menuFont = generator.generateFont(parameter);

            // Score font
            parameter.size = 18;
            parameter.color = Color.YELLOW;
            scoreFont = generator.generateFont(parameter);

            generator.dispose();

            Gdx.app.log("MainMenuScreen", "Vietnamese font created successfully");

        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Font creation failed: " + e.getMessage());

            // Fallback font
            menuFont = new BitmapFont();
            scoreFont = new BitmapFont();

            menuFont.getData().markupEnabled = true;
            scoreFont.getData().markupEnabled = true;

            menuFont.setColor(new Color(0.2f, 0.1f, 0f, 1));
            scoreFont.setColor(Color.YELLOW);
        }
    }

    // Tạo button style riêng biệt
    private void createButtonStyle() {
        buttonStyle = new TextButtonStyle();
        buttonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        buttonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        buttonStyle.font = menuFont; // Sử dụng font đã tạo
        buttonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1); // Dark brown text

        Gdx.app.log("MainMenuScreen", "Button style created with font: " + (menuFont != null ? "OK" : "NULL"));
    }

    private Texture createButtonTexture(int width, int height, Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);

        // Add rounded corners and a border
        pixmap.setColor(new Color(0.9f, 0.7f, 0.4f, 1f));
        pixmap.drawRectangle(0, 0, width-1, height-1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void createMenu() {
        // KIỂM TRA font trước khi tạo button
        if (menuFont == null) {
            Gdx.app.error("MainMenuScreen", "Font is null when creating menu!");
            return;
        }

        // Create a table to organize buttons
        Table table = new Table();
        table.setFillParent(true);

        // Create buttons using LocalizationManager và button style đã tạo
        playButton = new TextButton(LocalizationManager.getText("play_game"), buttonStyle);
        storeButton = new TextButton(LocalizationManager.getText("store"), buttonStyle);
        achievementsButton = new TextButton(LocalizationManager.getText("achievements"), buttonStyle);
        settingsButton = new TextButton(LocalizationManager.getText("settings"), buttonStyle);
        exitButton = new TextButton(LocalizationManager.getText("exit"), buttonStyle);

        // DEBUG: In ra text của button để kiểm tra
        Gdx.app.log("MainMenuScreen", "Button texts:");
        Gdx.app.log("MainMenuScreen", "Play: " + LocalizationManager.getText("play_game"));
        Gdx.app.log("MainMenuScreen", "Store: " + LocalizationManager.getText("store"));
        Gdx.app.log("MainMenuScreen", "Settings: " + LocalizationManager.getText("settings"));

        // Add button listeners
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        storeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new StoreScreen(game));
                dispose();
            }
        });

        achievementsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new AchievementScreen(game));
                dispose();
            }
        });

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game));
                dispose();
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        // Add buttons to table with padding
        table.add(playButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(storeButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(achievementsButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(settingsButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(exitButton).width(200).height(60);

        // Add table to stage
        stage.addActor(table);
    }

    // Method to update button texts when language changes
    private void updateButtonTexts() {
        if (playButton != null) {
            playButton.setText(LocalizationManager.getText("play_game"));
        }
        if (storeButton != null) {
            storeButton.setText(LocalizationManager.getText("store"));
        }
        if (achievementsButton != null) {
            achievementsButton.setText(LocalizationManager.getText("achievements"));
        }
        if (settingsButton != null) {
            settingsButton.setText(LocalizationManager.getText("settings"));
        }
        if (exitButton != null) {
            exitButton.setText(LocalizationManager.getText("exit"));
        }

        // QUAN TRỌNG: Cập nhật lại font style cho các button khi thay đổi ngôn ngữ
        if (buttonStyle != null && menuFont != null) {
            buttonStyle.font = menuFont;

            // Invalidate layout để button được vẽ lại với font mới
            if (playButton != null) playButton.invalidate();
            if (storeButton != null) storeButton.invalidate();
            if (achievementsButton != null) achievementsButton.invalidate();
            if (settingsButton != null) settingsButton.invalidate();
            if (exitButton != null) exitButton.invalidate();
        }
    }

    // Check if language has changed and update accordingly
    private void checkAndUpdateLanguage() {
        boolean newLanguageIsVietnamese = LocalizationManager.isVietnamese();
        if (currentLanguageIsVietnamese != newLanguageIsVietnamese) {
            currentLanguageIsVietnamese = newLanguageIsVietnamese;
            updateButtonTexts();
            Gdx.app.log("MainMenuScreen", "Language updated to: " + (currentLanguageIsVietnamese ? "Vietnamese" : "English"));
        }
    }

    @Override
    public void render(float delta) {
        // Check for language changes
        checkAndUpdateLanguage();

        // Clear screen
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // Update camera
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // Draw background and score
        game.batch.begin();
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        }

        // Draw total score in top left corner with tomato icon
        drawTotalScore();

        game.batch.end();

        // Draw stage (buttons)
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    private void drawTotalScore() {
        if (tomatoIcon != null && scoreFont != null) {
            // Icon size
            float iconSize = 32;
            float iconX = 15;
            float iconY = RaindropGame.GAME_HEIGHT - iconSize - 15; // 15px from top

            // Draw tomato icon
            game.batch.draw(tomatoIcon, iconX, iconY, iconSize, iconSize);

            // Draw text next to icon using LocalizationManager
            float textX = iconX + iconSize + 10; // 10px gap between icon and text
            float textY = iconY + iconSize - 5; // Align with icon, slight adjustment

            scoreFont.draw(game.batch, LocalizationManager.getText("tomato_score") + totalScore, textX, textY);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        // Reload total score when screen is shown (in case it was updated)
        loadTotalScore();

        // Update language state and button texts when screen is shown
        currentLanguageIsVietnamese = LocalizationManager.isVietnamese();
        updateButtonTexts();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        // Reload total score when resuming (in case it was updated)
        loadTotalScore();

        // Update language state and button texts when resuming
        currentLanguageIsVietnamese = LocalizationManager.isVietnamese();
        updateButtonTexts();
    }

    @Override
    public void dispose() {
        if (backgroundImage != null) {
            backgroundImage.dispose();
        }
        if (buttonTexture != null) {
            buttonTexture.dispose();
        }
        if (buttonDownTexture != null) {
            buttonDownTexture.dispose();
        }
        if (tomatoIcon != null) {
            tomatoIcon.dispose();
        }
        if (menuFont != null) {
            menuFont.dispose();
        }
        if (scoreFont != null) {
            scoreFont.dispose();
        }
        stage.dispose();
    }
}
