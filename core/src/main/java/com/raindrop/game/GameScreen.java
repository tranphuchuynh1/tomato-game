package com.raindrop.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final RaindropGame game;
    private ScoreManager scoreManager;
    private ItemManager itemManager;
    private ScoreHistoryManager scoreHistoryManager;

    private long totalScore = 0; // Tổng điểm tích lũy
    private boolean scoreSaved = false; // Flag để kiểm tra đã lưu điểm chưa
    // Assets
    Texture raindropImage;
    Texture bucketImage;
    Texture backgroundImage;
    Texture pauseButtonImage;
    Texture pausePanelImage;
    Texture boomImage;
    Sound raindropSound;
    Sound explosionSound; // Sound for boom collision
    Music rainMusic;

    // Animation for explosion effect
    Animation<TextureRegion> explosionAnimation;
    float explosionTimer;
    boolean isExploding;
    float explosionX, explosionY;

    // Camera and Input handling
    OrthographicCamera camera;
    Vector3 touchPos;

    // Game objects
    Rectangle bucket;
    Array<Rectangle> raindrops;
    Array<Rectangle> booms; // Array of bombs
    Rectangle pauseButton;
    Rectangle continueButton;
    Rectangle exitButton;
    Rectangle playAgainButton; // New button for Game Over screen

    // Game state
    long lastDropTime;
    long lastBoomTime; // Time when last bomb was spawned
    int dropsGathered;
    boolean isPaused;
    boolean showPauseMenu;
    boolean isGameOver; // New state for Game Over

    // Time vars
    private static final float BOOM_SPAWN_TIME = 4f; // Spawn bombs every 4 seconds
    private float boomSpawnTimer = 0;

    // Colors for custom buttons
    Color continueButtonColor;
    Color exitButtonColor;
    Color buttonTextColor;
    Color panelColor;

    // Custom Vietnamese font
    BitmapFont vietnameseFont;
    BitmapFont vietnameseTitleFont; // Larger font for titles

    public GameScreen(final RaindropGame game) {
        this.game = game;
        this.scoreManager = game.scoreManager; // Lấy từ game instance
        this.itemManager = game.itemManager;
        this.scoreHistoryManager = game.scoreHistoryManager;

        try {
            // Load images
            raindropImage = new Texture(Gdx.files.internal("tomato.png"));

            // call hàm tải hình ảnh dựa trên mặt hàng được trang bị
            loadBucketTexture();

            bucketImage = new Texture(Gdx.files.internal("basket.png"));
            backgroundImage = new Texture(Gdx.files.internal("backgr-play.png"));
            pauseButtonImage = new Texture(Gdx.files.internal("pause_button.png"));
            pausePanelImage = new Texture(Gdx.files.internal("pause_panel.png"));
            boomImage = new Texture(Gdx.files.internal("boom.png"));

            // Load sounds
            raindropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
            rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
            // Try to load explosion sound, if available
            try {
                explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));
            } catch (Exception e) {
                // If not available, use the drop sound as fallback
                explosionSound = raindropSound;
                Gdx.app.log("GameScreen", "Explosion sound not found, using drop sound instead");
            }

            // Create simple explosion animation - would be better with proper explosion frames
            // This is a placeholder - ideally you would have proper explosion sprites
            TextureRegion[] explosionFrames = new TextureRegion[5];
            for (int i = 0; i < 5; i++) {
                // Just scale the boom image differently for a simple effect
                explosionFrames[i] = new TextureRegion(boomImage);
            }
            explosionAnimation = new Animation<>(0.1f, explosionFrames);
            explosionTimer = 0;
            isExploding = false;

            // Create Vietnamese fonts
            createVietnameseFonts();

        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error loading assets: " + e);
        }



        rainMusic.setLooping(true);
        updateSoundSettings();
        rainMusic.play();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);

        // Initialize touch pos vector
        touchPos = new Vector3();

        // Create bucket
        bucket = new Rectangle();
        bucket.x = (float) RaindropGame.GAME_WIDTH / 2 - (float) 64 / 2; // Center horizontally
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

        // Create pause button (top right corner)
        pauseButton = new Rectangle();
        pauseButton.width = 70;
        pauseButton.height = 70;
        pauseButton.x = RaindropGame.GAME_WIDTH - pauseButton.width - 10; // 10px from right edge
        pauseButton.y = RaindropGame.GAME_HEIGHT - pauseButton.height - 10; // 10px from top edge

        // Create buttons for pause menu and game over screen
        int buttonWidth = 120;
        int buttonHeight = 50;

        continueButton = new Rectangle();
        continueButton.width = buttonWidth;
        continueButton.height = buttonHeight;
        continueButton.x = (float) RaindropGame.GAME_WIDTH / 2 - buttonWidth - 20; // Left of center
        continueButton.y = (float) RaindropGame.GAME_HEIGHT / 2 - buttonHeight / 2 - 20; // Centered vertically, adjusted down

        exitButton = new Rectangle();
        exitButton.width = buttonWidth;
        exitButton.height = buttonHeight;
        exitButton.x = (float) RaindropGame.GAME_WIDTH / 2 + 20; // Right of center
        exitButton.y = (float) RaindropGame.GAME_HEIGHT / 2 - buttonHeight / 2 - 20; // Same level as continue button

        // Play again button (for Game Over)
        playAgainButton = new Rectangle();
        playAgainButton.width = buttonWidth;
        playAgainButton.height = buttonHeight;
        playAgainButton.x = continueButton.x; // Same position as continue button
        playAgainButton.y = continueButton.y; // Same position as continue button

        // Initialize colors for custom buttons
        continueButtonColor = new Color(0.9f, 0.7f, 0.4f, 0.8f); // Màu cam giống StoreScreen
        exitButtonColor = new Color(0.9f, 0.7f, 0.4f, 0.8f); // Cùng màu cam
        buttonTextColor = new Color(0.2f, 0.1f, 0f, 1); // Màu text nâu đậm giống StoreScreen
        panelColor = new Color(0.76f, 0.6f, 0.42f, 1); // Brown (hex #C39A6B) // Brown (hex #C39A6B)

        raindrops = new Array<Rectangle>();
        booms = new Array<Rectangle>(); // Initialize booms array
        spawnRaindrop();
        loadCurrentTotalScore();

        isPaused = false;
        showPauseMenu = false;
        isGameOver = false;
    }

    private void updateSoundSettings() {
        boolean soundEnabled = GameSettings.isSoundEnabled();

        if (soundEnabled) {
            if (!rainMusic.isPlaying() && !isPaused && !isGameOver) {
                rainMusic.play();
            }
            // Set volume to normal
            rainMusic.setVolume(1.0f);
        } else {
            // Pause music if sound is disabled
            rainMusic.pause();
            // Set volume to 0 as backup
            rainMusic.setVolume(0.0f);
        }

        Gdx.app.log("GameScreen", "Sound settings updated - Enabled: " + soundEnabled);
    }

    private void loadBucketTexture() {
        String equippedItemId = itemManager.getCurrentEquippedItem();
        String bucketTexturePath = "basket.png"; // default

        if (equippedItemId != null) {
            ItemManager.Item equippedItem = itemManager.getItem(equippedItemId);
            if (equippedItem != null) {
                // Map item ID to corresponding bucket texture
                switch (equippedItemId) {
                    case "bowl":
                        bucketTexturePath = "bowl.png";
                        break;
                    case "box":
                        bucketTexturePath = "box.png";
                        break;
                    case "bucket":
                        bucketTexturePath = "bucket.png";
                        break;
                    case "spaceship":
                        bucketTexturePath = "spaceship.png";
                        break;
                    default:
                        bucketTexturePath = "basket.png";
                        break;
                }
            }
        }

        try {
            bucketImage = new Texture(Gdx.files.internal(bucketTexturePath));
            Gdx.app.log("GameScreen", "Loaded bucket texture: " + bucketTexturePath);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Could not load bucket texture: " + bucketTexturePath + ", using default");
            bucketImage = new Texture(Gdx.files.internal("basket.png"));
        }
    }

    private void loadCurrentTotalScore() {
        if (scoreManager != null) {
            scoreManager.getCurrentTotalScore(new ScoreManager.ScoreCallback() {
                @Override
                public void onSuccess(long currentTotal) {
                    totalScore = currentTotal;
                    Gdx.app.log("GameScreen", "Loaded total score: " + totalScore);
                }

                @Override
                public void onFailure(String error) {
                    Gdx.app.error("GameScreen", "Failed to load total score: " + error);
                }
            });
        }
    }

    private void saveScoreToFirebase() {
        if (scoreSaved || dropsGathered == 0 || scoreManager == null) {
            return;
        }

        scoreSaved = true;

        // Lưu vào tổng điểm
        scoreManager.addScoreToTotal(dropsGathered, new ScoreManager.ScoreCallback() {
            @Override
            public void onSuccess(long newTotal) {
                totalScore = newTotal;
                Gdx.app.log("GameScreen", "Score saved! New total: " + newTotal);
            }

            @Override
            public void onFailure(String error) {
                Gdx.app.error("GameScreen", "Failed to save score: " + error);
                scoreSaved = false;
            }
        });

        // SỬA: Đảm bảo lưu vào lịch sử Firebase (không phải dummy)
        if (scoreHistoryManager != null) {
            scoreHistoryManager.saveScore("Player1", dropsGathered, new ScoreHistoryManager.SaveScoreCallback() {
                @Override
                public void onSuccess() {
                    Gdx.app.log("GameScreen", "Score saved to Firebase history: " + dropsGathered);
                }

                @Override
                public void onFailure(String error) {
                    Gdx.app.error("GameScreen", "Failed to save score to Firebase history: " + error);
                }
            });
        } else {
            Gdx.app.error("GameScreen", "ScoreHistoryManager is null - cannot save to Firebase");
        }
    }


    private void createVietnameseFonts() {
        try {
            // Use FreeType to generate a font that supports Vietnamese characters
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Arial-Unicode.ttf"));

            // Regular font
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 20;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = new Color(0, 0, 0, 0.3f);
            // Include Vietnamese character set
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";

            vietnameseFont = generator.generateFont(parameter);

            // Title font (larger size)
            parameter.size = 30;
            parameter.borderWidth = 2;
            vietnameseTitleFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error creating Vietnamese font: " + e);
            // Fallback to default font if custom font fails
            vietnameseFont = new BitmapFont();
            vietnameseTitleFont = new BitmapFont();
        }
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, RaindropGame.GAME_WIDTH - 64);
        raindrop.y = RaindropGame.GAME_HEIGHT + 20; // Start just above the screen
        raindrop.width = 40;
        raindrop.height = 40;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    // New method to spawn boom
    private void spawnBooms() {
        // Spawn two booms
        for (int i = 0; i < 2; i++) {
            Rectangle boom = new Rectangle();
            boom.x = MathUtils.random(0, RaindropGame.GAME_WIDTH - 64);
            boom.y = RaindropGame.GAME_HEIGHT + 20; // Start just above the screen
            boom.width = 50; // Slightly bigger than raindrops
            boom.height = 50;
            booms.add(boom);
        }
        lastBoomTime = TimeUtils.nanoTime();
    }

    private void resetGame() {
        // Clear objects
        raindrops.clear();
        booms.clear();

        // Reset score
        dropsGathered = 0;
        scoreSaved = false;

        // Reset game state
        isGameOver = false;
        isPaused = false;
        isExploding = false;

        // Start music again only if sound is enabled
        if (GameSettings.isSoundEnabled()) {
            rainMusic.play();
        }

        // Reset bucket position
        bucket.x = (float) RaindropGame.GAME_WIDTH / 2 - (float) 64 / 2;

        // Spawn initial raindrop
        spawnRaindrop();
    }

    @Override
    public void render(float delta) {
        // Clear screen
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // Update camera
        camera.update();

        // Update explosion animation if active
        if (isExploding) {
            explosionTimer += delta;
            // If explosion animation finished
            if (explosionTimer > explosionAnimation.getAnimationDuration()) {
                isExploding = false;
            }
        }

        // Handle input and update game state only if not paused and not game over
        if (!isPaused && !isGameOver) {
            handleInput();
            updateGameState(delta);
        } else if (isPaused) {
            handlePauseMenuInput();
        } else if (isGameOver) {
            handleGameOverInput();
        }

        // Always draw
        draw(delta);
    }

    private void handleInput() {
        // Check if pause button is pressed
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (pauseButton.contains(touchPos.x, touchPos.y)) {
                isPaused = true;
                showPauseMenu = true;
                if (rainMusic.isPlaying()) {
                    rainMusic.pause();
                }
                return;
            }
        }
        // Regular game input
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - (float) 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // Keep bucket within screen bounds
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > RaindropGame.GAME_WIDTH - 64) bucket.x = RaindropGame.GAME_WIDTH - 64;
    }

    private void handlePauseMenuInput() {
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (continueButton.contains(touchPos.x, touchPos.y)) {
                // Resume game
                isPaused = false;
                showPauseMenu = false;

                // Kiểm tra cài đặt âm thanh trước khi phát nhạc
                if (GameSettings.isSoundEnabled()) {
                    rainMusic.play();
                }
            } else if (exitButton.contains(touchPos.x, touchPos.y)) {
                // Return to main menu
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
    }


    // New method to handle game over menu input
    private void handleGameOverInput() {
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (playAgainButton.contains(touchPos.x, touchPos.y)) {
                // Reset and play again
                resetGame();
            } else if (exitButton.contains(touchPos.x, touchPos.y)) {
                // Return to main menu
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
    }

    private void updateGameState(float delta) {
        // Spawn normal raindrops
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnRaindrop();
        }

        // Update timer for boom spawning - every 4 seconds
        boomSpawnTimer += delta;
        if (boomSpawnTimer >= BOOM_SPAWN_TIME) {
            spawnBooms();
            boomSpawnTimer = 0; // Reset timer
        }

        // Update raindrop positions, remove those overlapped below or hit bucket
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * delta;

            if (raindrop.y + 64 < 0) iter.remove();

            if (raindrop.overlaps(bucket)) {
                dropsGathered++;

                // Chỉ phát âm thanh nếu được bật
                if (GameSettings.isSoundEnabled()) {
                    raindropSound.play();
                }

                iter.remove();
            }
        }

        // Update boom positions, check for collisions with bucket
        Iterator<Rectangle> boomIter = booms.iterator();
        while (boomIter.hasNext()) {
            Rectangle boom = boomIter.next();
            boom.y -= 250 * delta;

            if (boom.y + 64 < 0) boomIter.remove();

            if (boom.overlaps(bucket)) {
                // Game over!
                isGameOver = true;
                rainMusic.pause();

                // Play explosion sound only if enabled
                if (GameSettings.isSoundEnabled() && explosionSound != null) {
                    explosionSound.play();
                }

                // Save score
                saveScoreToFirebase();

                // Start explosion animation
                isExploding = true;
                explosionTimer = 0;
                explosionX = boom.x;
                explosionY = boom.y;

                // Remove this boom
                boomIter.remove();
            }
        }
    }

    private void draw(float delta) {
        if (game.batch != null && bucketImage != null && raindropImage != null) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();

            // Draw background first
            if (backgroundImage != null) {
                game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
            }

            // Draw score using Vietnamese font
            vietnameseFont.setColor(0, 0, 0, 1); // Yellow color (RGBA)
            vietnameseFont.draw(game.batch, "Score: " + dropsGathered, 10, RaindropGame.GAME_HEIGHT - 30);
            vietnameseFont.setColor(1, 1, 1, 1); // Reset to white for other text

            // Draw bucket and raindrops
            game.batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
            for (Rectangle raindrop : raindrops) {
                game.batch.draw(raindropImage, raindrop.x, raindrop.y, raindrop.width, raindrop.height);
            }

            // Draw booms
            for (Rectangle boom : booms) {
                game.batch.draw(boomImage, boom.x, boom.y, boom.width, boom.height);
            }

            // Draw explosion animation if active
            if (isExploding) {
                TextureRegion explosionFrame = explosionAnimation.getKeyFrame(explosionTimer, true);
                float size = 100 + explosionTimer * 50; // Grow explosion over time
                game.batch.draw(explosionFrame,
                    explosionX - size/2 + 25, // Center the explosion
                    explosionY - size/2 + 25,
                    size, size);
            }

            // Draw pause button
            if (pauseButtonImage != null && !isGameOver) {
                game.batch.draw(pauseButtonImage, pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
            }

            // Draw pause menu if game is paused
            if (showPauseMenu && !isGameOver) {
                drawPauseMenu();
            }

            // Draw game over screen if game is over
            if (isGameOver) {
                drawGameOverScreen();
            }

            game.batch.end();
        }
    }


    private void drawPauseMenu() {
        // Draw semi-transparent overlay
        game.batch.setColor(0, 0, 0, 0.5f);
        game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        game.batch.setColor(1, 1, 1, 1); // Reset color

        // Draw pause panel - larger, as requested
        float panelWidth = 400;  // Increased from 300
        float panelHeight = 250; // Increased from 200

        // Draw panel background with brown color
        game.batch.setColor(panelColor);
        game.batch.draw(pausePanelImage != null ? pausePanelImage : backgroundImage,
            (float) RaindropGame.GAME_WIDTH / 2 - panelWidth / 2,
            (float) RaindropGame.GAME_HEIGHT / 2 - panelHeight / 2,
            panelWidth, panelHeight);
        game.batch.setColor(1, 1, 1, 1); // Reset color

        // Draw "Tạm Dừng" title using our Vietnamese title font - CĂNG GIỮA VÀ TO HƠN
        vietnameseTitleFont.getData().setScale(1.2f); // Tăng kích thước
        String pauseTitle = "PAUSE";
        // FIX: Properly calculate text width for centering
        vietnameseTitleFont.setColor(1, 1, 1, 1);
        vietnameseTitleFont.draw(game.batch, pauseTitle,
            (float) RaindropGame.GAME_WIDTH / 2 - pauseTitle.length() * 7, // Căn giữa tạm thời
            (float) RaindropGame.GAME_HEIGHT / 2 + 80);
        vietnameseTitleFont.getData().setScale(1.0f); // Reset scale

        // Draw continue button - FIX: Use orange color for entire button background
        game.batch.setColor(continueButtonColor); // Orange background
        game.batch.draw(backgroundImage, continueButton.x, continueButton.y,
            continueButton.width, continueButton.height);

        // Draw exit button - FIX: Use orange color for entire button background
        game.batch.setColor(exitButtonColor); // Orange background
        game.batch.draw(backgroundImage, exitButton.x, exitButton.y,
            exitButton.width, exitButton.height);

        // Reset color for text
        game.batch.setColor(1, 1, 1, 1);

        // Draw button texts using our Vietnamese font - căn giữa text trong nút
        String continueText = "CONTINUE";
        String exitText = "EXIT";

        // FIX: Properly center text in continue button
        vietnameseFont.setColor(buttonTextColor);
        vietnameseFont.draw(game.batch, continueText,
            continueButton.x + (continueButton.width - continueText.length() * 6) / 2,
            continueButton.y + continueButton.height / 2 + 5);

        // FIX: Properly center text in exit button
        vietnameseFont.draw(game.batch, exitText,
            exitButton.x + (exitButton.width - exitText.length() * 6) / 2,
            exitButton.y + exitButton.height / 2 + 5);

        // Reset color
        game.batch.setColor(1, 1, 1, 1);
    }

    // New method to draw game over screen
    private void drawGameOverScreen() {
        // Draw semi-transparent overlay
        game.batch.setColor(0, 0, 0, 0.5f);
        game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        game.batch.setColor(1, 1, 1, 1); // Reset color

        // Draw panel - same as pause panel, using pausePanelImage
        float panelWidth = 400;
        float panelHeight = 250;

        // Draw panel background with brown color
        game.batch.setColor(panelColor);
        game.batch.draw(pausePanelImage != null ? pausePanelImage : backgroundImage,
            (float) RaindropGame.GAME_WIDTH / 2 - panelWidth / 2,
            (float) RaindropGame.GAME_HEIGHT / 2 - panelHeight / 2,
            panelWidth, panelHeight);
        game.batch.setColor(1, 1, 1, 1); // Reset color

        // Draw "Game Over" title - FIX: TO HƠN VÀ CĂNG GIỮA HƠN
        vietnameseTitleFont.setColor(1, 0, 0, 1); // Red color for Game Over
        vietnameseTitleFont.getData().setScale(1.5f); // FIX: Tăng kích thước lên 1.5f thay vì 1.3f
        String gameOverTitle = "Game Over";
        // FIX: Better centering calculation
        vietnameseTitleFont.draw(game.batch, gameOverTitle,
            (float) RaindropGame.GAME_WIDTH / 2 - gameOverTitle.length() * 7, // FIX: Căn giữa tốt hơn
            (float) RaindropGame.GAME_HEIGHT / 2 + 90); // FIX: Tăng vị trí lên một chút
        vietnameseTitleFont.setColor(1, 1, 1, 1); // Reset color
        vietnameseTitleFont.getData().setScale(1.0f); // Reset scale

        // Draw score - căn giữa
        vietnameseFont.setColor(1, 1, 0, 1); // Yellow for score
        String scoreText = "Total Score: " + dropsGathered;
        vietnameseFont.draw(game.batch, scoreText,
            (float) RaindropGame.GAME_WIDTH / 2 - scoreText.length() * 4, // FIX: Better centering
            (float) RaindropGame.GAME_HEIGHT / 2 + 30);
        vietnameseFont.setColor(1, 1, 1, 1); // Reset color

        // Draw play again button - FIX: Use orange color for entire button background
        game.batch.setColor(continueButtonColor); // Orange background
        game.batch.draw(backgroundImage, playAgainButton.x, playAgainButton.y,
            playAgainButton.width, playAgainButton.height);

        // Draw exit button - FIX: Use orange color for entire button background
        game.batch.setColor(exitButtonColor); // Orange background
        game.batch.draw(backgroundImage, exitButton.x, exitButton.y,
            exitButton.width, exitButton.height);

        // Reset color for text
        game.batch.setColor(1, 1, 1, 1);

        // Draw button texts - căn giữa text trong nút
        String playAgainText = "CONTINUE";
        String exitText = "EXIT";

        // FIX: Properly center text in play again button
        vietnameseFont.setColor(buttonTextColor);
        vietnameseFont.draw(game.batch, playAgainText,
            playAgainButton.x + (playAgainButton.width - playAgainText.length() * 6) / 2,
            playAgainButton.y + playAgainButton.height / 2 + 5);

        // FIX: Properly center text in exit button
        vietnameseFont.draw(game.batch, exitText,
            exitButton.x + (exitButton.width - exitText.length() * 6) / 2,
            exitButton.y + exitButton.height / 2 + 5);

        // Reset color
        game.batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        loadBucketTexture();
        if (!isGameOver) {
            rainMusic.play();
        }
        updateSoundSettings();
    }

    @Override
    public void hide() {
        rainMusic.pause();
    }

    @Override
    public void pause() {
        if (!isGameOver) {
            isPaused = true;
            rainMusic.pause();
        }
    }

    @Override
    public void resume() {
        if (!isGameOver) {
            isPaused = false;
            rainMusic.play();
        }
        updateSoundSettings();
    }

    public void refreshSettings() {
        updateSoundSettings();
    }

    @Override
    public void dispose() {
        raindropImage.dispose();
        bucketImage.dispose();
        backgroundImage.dispose();
        if (pauseButtonImage != null) pauseButtonImage.dispose();
        if (pausePanelImage != null) pausePanelImage.dispose();
        if (boomImage != null) boomImage.dispose();
        raindropSound.dispose();
        if (explosionSound != null) explosionSound.dispose();
        rainMusic.dispose();

        // Dispose custom fonts
        if (vietnameseFont != null) vietnameseFont.dispose();
        if (vietnameseTitleFont != null) vietnameseTitleFont.dispose();
    }
}
