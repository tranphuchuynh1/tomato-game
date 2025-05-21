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

    public MainMenuScreen(final RaindropGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);

        // Create viewport and stage for UI
        viewport = new FitViewport(RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT, camera);
        stage = new Stage(viewport, game.batch);

        // Enable stage to receive input events
        Gdx.input.setInputProcessor(stage);

        try {
            // Load background image
            backgroundImage = new Texture(Gdx.files.internal("background-screen.png"));

            // Create button textures
            buttonTexture = createButtonTexture(200, 60, new Color(0.8f, 0.6f, 0.3f, 0.8f)); // Sand color
            buttonDownTexture = createButtonTexture(200, 60, new Color(0.7f, 0.5f, 0.2f, 0.9f)); // Darker sand color

            // Initialize custom font that supports Vietnamese
            createVietnameseFont();

            createMenu();
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading assets: " + e);
        }
    }

    private void createVietnameseFont() {
        try {
            // Use FreeType to generate a font that supports Vietnamese characters
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSans-Regular.ttf"));
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = 20;
            parameter.color = new Color(0.2f, 0.1f, 0f, 1); // Dark brown text
            parameter.borderWidth = 1;
            parameter.borderColor = new Color(0, 0, 0, 0.3f);
            // Include Vietnamese character set
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";

            menuFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error creating Vietnamese font: " + e);
            // Fallback to default font if custom font fails
            menuFont = new BitmapFont();
        }
    }

    private Texture createButtonTexture(int width, int height, Color color) {
        // This is a placeholder - in a real game, you would use actual button textures
        // For now, we'll use a simple colored pixel texture
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
        // Create a table to organize buttons
        Table table = new Table();
        table.setFillParent(true);

        // Create button style using our custom font
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        buttonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        buttonStyle.font = menuFont; // Use custom Vietnamese font
        buttonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1); // Dark brown text

        // Create buttons with Vietnamese text
        TextButton playButton = new TextButton("CHƠI GAME", buttonStyle);
        TextButton storyButton = new TextButton("CỬA HÀNG", buttonStyle);
        TextButton achievementsButton = new TextButton("THÀNH TÍCH", buttonStyle);
        TextButton settingsButton = new TextButton("CÀI ĐẶT", buttonStyle);
        TextButton exitButton = new TextButton("THOÁT", buttonStyle);

        // Add button listeners
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        // dieu huong button cua hang o day
        storyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new StoreScreen(game));
                dispose();
            }
        });

        achievementsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MainMenuScreen", "Achievements button clicked - functionality not implemented yet");
                // Add your achievements screen here
            }
        });

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("MainMenuScreen", "Settings button clicked - functionality not implemented yet");
                // Add your settings screen here
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
        table.add(storyButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(achievementsButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(settingsButton).padBottom(20).width(200).height(60);
        table.row();
        table.add(exitButton).width(200).height(60);

        // Add table to stage
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // Update camera
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // Draw background
        game.batch.begin();
        if (backgroundImage != null) {
            game.batch.draw(backgroundImage, 0, 0, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);
        }
        game.batch.end();

        // Draw stage (buttons)
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
    }

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
        if (backgroundImage != null) {
            backgroundImage.dispose();
        }
        if (buttonTexture != null) {
            buttonTexture.dispose();
        }
        if (buttonDownTexture != null) {
            buttonDownTexture.dispose();
        }
        if (menuFont != null) {
            menuFont.dispose();
        }
        stage.dispose();
    }
}
