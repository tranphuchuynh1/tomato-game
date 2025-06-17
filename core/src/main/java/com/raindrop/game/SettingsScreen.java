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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen implements Screen {
    final RaindropGame game;
    OrthographicCamera camera;
    Texture backgroundImage;
    Texture panelImage;

    // Stage for UI elements
    private Stage stage;
    private Viewport viewport;

    // Button textures
    private Texture buttonTexture;
    private Texture buttonDownTexture;
    private Texture buttonOnTexture;
    private Texture buttonOffTexture;

    // Custom fonts
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    // Settings state
    private boolean soundEnabled;
    private boolean isVietnamese;

    // UI Components
    private TextButton soundButton;
    private TextButton languageButton;
    private TextButton backButton;

    // Button styles
    private TextButtonStyle regularButtonStyle;
    private TextButtonStyle onButtonStyle;
    private TextButtonStyle offButtonStyle;

    public SettingsScreen(final RaindropGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);

        // Create viewport and stage for UI
        viewport = new FitViewport(RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT, camera);
        stage = new Stage(viewport, game.batch);

        // Enable stage to receive input events
        Gdx.input.setInputProcessor(stage);

        try {
            // Load background and panel images
            backgroundImage = new Texture(Gdx.files.internal("background-screen.png"));
            panelImage = new Texture(Gdx.files.internal("pause_panel.png"));

            // Create button textures
            createButtonTextures();

            // Initialize custom fonts
            createFonts();

            // Load settings from preferences - Load FIRST
            loadSettings();

            // Print current settings for debugging
            GameSettings.printCurrentSettings();

            // Create UI after loading settings
            createSettingsUI();

        } catch (Exception e) {
            Gdx.app.error("SettingsScreen", "Error loading assets: " + e);
        }
    }

    private void createButtonTextures() {
        // Regular button textures
        buttonTexture = createColoredTexture(180, 50, new Color(0.8f, 0.6f, 0.3f, 0.8f)); // Sand color
        buttonDownTexture = createColoredTexture(180, 50, new Color(0.7f, 0.5f, 0.2f, 0.9f)); // Darker sand

        // ON/OFF button textures
        buttonOnTexture = createColoredTexture(180, 50, new Color(0.2f, 0.8f, 0.2f, 0.8f)); // Green for ON
        buttonOffTexture = createColoredTexture(180, 50, new Color(0.8f, 0.2f, 0.2f, 0.8f)); // Red for OFF
    }

    private Texture createColoredTexture(int width, int height, Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);

        // Add border
        pixmap.setColor(new Color(0.9f, 0.7f, 0.4f, 1f));
        pixmap.drawRectangle(0, 0, width-1, height-1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void createFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/NotoSans-Regular.ttf"));

            // Title font
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 28;
            parameter.color = new Color(0.2f, 0.1f, 0f, 1);
            parameter.borderWidth = 2;
            parameter.borderColor = new Color(0, 0, 0, 0.3f);
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
                "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";

            titleFont = generator.generateFont(parameter);

            // Button font
            parameter.size = 18;
            parameter.borderWidth = 1;
            buttonFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("SettingsScreen", "Error creating fonts: " + e);
            titleFont = new BitmapFont();
            buttonFont = new BitmapFont();
        }
    }

    private void loadSettings() {
        soundEnabled = GameSettings.isSoundEnabled();
        isVietnamese = GameSettings.isVietnamese();

        Gdx.app.log("SettingsScreen", "Settings loaded - Sound: " + soundEnabled + ", Vietnamese: " + isVietnamese);
    }

    private void saveSettings() {
        GameSettings.setSoundEnabled(soundEnabled);
        GameSettings.setVietnamese(isVietnamese);

        Gdx.app.log("SettingsScreen", "Settings saved - Sound: " + soundEnabled + ", Vietnamese: " + isVietnamese);
    }

    private void createSettingsUI() {
        Table table = new Table();
        table.setFillParent(true);

        // Create button styles
        regularButtonStyle = new TextButtonStyle();
        regularButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        regularButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        regularButtonStyle.font = buttonFont;
        regularButtonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1);

        onButtonStyle = new TextButtonStyle();
        onButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonOnTexture));
        onButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        onButtonStyle.font = buttonFont;
        onButtonStyle.fontColor = Color.WHITE;

        offButtonStyle = new TextButtonStyle();
        offButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonOffTexture));
        offButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        offButtonStyle.font = buttonFont;
        offButtonStyle.fontColor = Color.WHITE;

        // Create buttons
        createSoundButton();
        createLanguageButton();
        createBackButton();

        // Add button listeners
        soundButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                soundEnabled = !soundEnabled;
                saveSettings(); // Save immediately
                updateSoundButton(); // Update button appearance
                Gdx.app.log("SettingsScreen", "Sound toggled to: " + soundEnabled);
            }
        });

        languageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isVietnamese = !isVietnamese;
                saveSettings(); // Save immediately
                updateAllButtons(); // Update all button texts
                Gdx.app.log("SettingsScreen", "Language toggled to: " + (isVietnamese ? "Vietnamese" : "English"));
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Make sure settings are saved before going back
                saveSettings();

                // Nếu đang từ GameScreen thì quay lại GameScreen, không phải MainMenuScreen
                if (game.getScreen() instanceof GameScreen) {
                    // Refresh settings in the current GameScreen
                    ((GameScreen) game.getScreen()).refreshSettings();
                } else {
                    // Otherwise go to main menu
                    game.setScreen(new MainMenuScreen(game));
                }
                dispose();
            }
        });

        // Add buttons to table with spacing
        table.add().padTop(100); // Top padding
        table.row();
        table.add(soundButton).padBottom(30).width(180).height(50);
        table.row();
        table.add(languageButton).padBottom(30).width(180).height(50);
        table.row();
        table.add(backButton).padBottom(50).width(180).height(50);

        // Add table to stage
        stage.addActor(table);
    }

    private void createSoundButton() {
        String text = getSoundButtonText();
        TextButtonStyle style = soundEnabled ? onButtonStyle : offButtonStyle;
        soundButton = new TextButton(text, style);
    }

    private void createLanguageButton() {
        String text = getLanguageButtonText();
        languageButton = new TextButton(text, regularButtonStyle);
    }

    private void createBackButton() {
        String text = getBackButtonText();
        backButton = new TextButton(text, regularButtonStyle);
    }

    private void updateSoundButton() {
        if (soundButton != null) {
            soundButton.setText(getSoundButtonText());
            soundButton.setStyle(soundEnabled ? onButtonStyle : offButtonStyle);
        }
    }

    private void updateLanguageButton() {
        if (languageButton != null) {
            languageButton.setText(getLanguageButtonText());
        }
    }

    private void updateBackButton() {
        if (backButton != null) {
            backButton.setText(getBackButtonText());
        }
    }

    private void updateAllButtons() {
        updateSoundButton();
        updateLanguageButton();
        updateBackButton();
    }

    private String getSoundButtonText() {
        if (soundEnabled) {
            return isVietnamese ? "ÂM THANH: BAT" : "SOUND: ON";
        } else {
            return isVietnamese ? "ÂM THANH: TAT" : "SOUND: OFF";
        }
    }

    private String getLanguageButtonText() {
        return isVietnamese ? "NGÔN NGU: VN" : "LANGUAGE: ENG";
    }

    private String getBackButtonText() {
        return isVietnamese ? "QUAY LAI" : "BACK";
    }

    @Override
    public void render(float delta) {
        // Clear screen
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // Update camera
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // Draw background and panel
        game.batch.begin();

        // Draw background
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        }

        // Draw panel in center
        if (panelImage != null) {
            float panelWidth = 400;
            float panelHeight = 500;
            float panelX = (RaindropGame.GAME_WIDTH - panelWidth) / 2;
            float panelY = (RaindropGame.GAME_HEIGHT - panelHeight) / 2;

            game.batch.draw(panelImage, panelX, panelY, panelWidth, panelHeight);
        }

        // Draw title
        if (titleFont != null) {
            String title = isVietnamese ? "CÀI ĐẶT" : "SETTINGS";
            float titleWidth = titleFont.draw(game.batch, title, 0, 0).width;
            titleFont.draw(game.batch, title,
                (RaindropGame.GAME_WIDTH - titleWidth) / 2,
                RaindropGame.GAME_HEIGHT / 2 + 180);
        }

        game.batch.end();

        // Draw stage (buttons)
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    // Static getter methods for other screens to access settings
    public static boolean isSoundEnabled() {
        return GameSettings.isSoundEnabled();
    }

    public static boolean isVietnamese() {
        return GameSettings.isVietnamese();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        // Reload settings when screen is shown
        loadSettings();
        if (soundButton != null && languageButton != null && backButton != null) {
            updateAllButtons();
        }
    }

    @Override
    public void hide() {
        // Save settings when leaving screen
        saveSettings();
    }

    @Override
    public void pause() {
        // Save settings when pausing
        saveSettings();
    }

    @Override
    public void resume() {
        // Reload settings when resuming
        loadSettings();
        if (soundButton != null && languageButton != null && backButton != null) {
            updateAllButtons();
        }
    }

    @Override
    public void dispose() {
        // Save settings before disposing
        saveSettings();

        if (backgroundImage != null) backgroundImage.dispose();
        if (panelImage != null) panelImage.dispose();
        if (buttonTexture != null) buttonTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
        if (buttonOnTexture != null) buttonOnTexture.dispose();
        if (buttonOffTexture != null) buttonOffTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        stage.dispose();
    }
}
