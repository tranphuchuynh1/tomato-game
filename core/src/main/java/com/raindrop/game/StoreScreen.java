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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class StoreScreen implements Screen {
    final RaindropGame game;
    OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private BitmapFont storeFont;
    private BitmapFont titleFont;

    // Textures
    private Texture backgroundImage;
    private Texture panelTexture;
    private Texture buttonTexture;
    private Texture buttonDownTexture;

    // Store items
    private Texture bowlTexture;
    private Texture boxTexture;
    private Texture bucketTexture;
    private Texture spaceshipTexture;
    private Texture tomatoTexture;

    // Back button
    private TextButton backButton;

    public StoreScreen(final RaindropGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT);

        // Create viewport and stage for UI
        viewport = new FitViewport(RaindropGame.GAME_WIDTH, RaindropGame.GAME_HEIGHT, camera);
        stage = new Stage(viewport, game.batch);

        // Enable stage to receive input events
        Gdx.input.setInputProcessor(stage);

        loadAssets();
        createFonts();
        createStoreUI();
    }

    private void loadAssets() {
        try {
            // Load background and panel
            backgroundImage = new Texture(Gdx.files.internal("background-screen.png"));
            panelTexture = new Texture(Gdx.files.internal("pause_panel.png"));

            // Load item textures
            bowlTexture = new Texture(Gdx.files.internal("bowl.png"));
            boxTexture = new Texture(Gdx.files.internal("box.png"));
            bucketTexture = new Texture(Gdx.files.internal("bucket.png"));
            spaceshipTexture = new Texture(Gdx.files.internal("spaceship.png"));
            tomatoTexture = new Texture(Gdx.files.internal("tomato.png"));

            // Create button textures
            buttonTexture = createButtonTexture(120, 40, new Color(0.8f, 0.6f, 0.3f, 0.8f)); // Sand color
            buttonDownTexture = createButtonTexture(120, 40, new Color(0.7f, 0.5f, 0.2f, 0.9f)); // Darker sand color
        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error loading assets: " + e);
        }
    }

    private void createFonts() {
        try {
            // Use FreeType to generate fonts that support Vietnamese
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Arial-Unicode.ttf"));

            // Create store item font
            FreeTypeFontParameter itemParameter = new FreeTypeFontParameter();
            itemParameter.size = 18;
            itemParameter.color = new Color(0.2f, 0.1f, 0f, 1); // Dark brown text
            itemParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";
            storeFont = generator.generateFont(itemParameter);

            // Create title font with larger size for header
            FreeTypeFontParameter titleParameter = new FreeTypeFontParameter();
            titleParameter.size = 32; // tang kich thuoc text"CUA HANG" o day
            titleParameter.color = new Color(0.2f, 0.1f, 0f, 1); // Dark brown text
            titleParameter.borderWidth = 1;
            titleParameter.borderColor = new Color(0, 0, 0, 0.3f);
            titleParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ";
            titleFont = generator.generateFont(titleParameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("StoreScreen", "Error creating fonts: " + e);
            // Fallback to default font if custom font fails
            storeFont = new BitmapFont();
            titleFont = new BitmapFont();
        }
    }

    private Texture createButtonTexture(int width, int height, Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);

        // Add a border
        pixmap.setColor(new Color(0.9f, 0.7f, 0.4f, 1f));
        pixmap.drawRectangle(0, 0, width-1, height-1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void createStoreUI() {
        // Create main table
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        // Add store panel image
        Image panelImage = new Image(panelTexture);
        float panelWidth = RaindropGame.GAME_WIDTH * 0.85f;
        float panelHeight = RaindropGame.GAME_HEIGHT * 0.85f;

        // Create container table that will be positioned over the panel
        Table containerTable = new Table();
        containerTable.setFillParent(true);

        // Create store content table
        Table storeTable = new Table();

        // Create store title with larger font
        LabelStyle titleStyle = new LabelStyle(titleFont, Color.WHITE);
        Label titleLabel = new Label("CỬA HÀNG", titleStyle);

        // Define button style
        TextButtonStyle buyButtonStyle = new TextButtonStyle();
        buyButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        buyButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        buyButtonStyle.font = storeFont;
        buyButtonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1);

        // Create label style for item names and prices
        LabelStyle itemStyle = new LabelStyle(storeFont, Color.WHITE);

        // Create items grid with 2 columns
        Table itemsTable = new Table();

        // First row (Bowl and Box)
        Table row1 = new Table();
        addStoreItem(row1, bowlTexture, "Bát", 25, buyButtonStyle, itemStyle);
        row1.add().width(40); // Spacing between items
        addStoreItem(row1, boxTexture, "Hộp", 50, buyButtonStyle, itemStyle);

        // Second row (Bucket and Spaceship)
        Table row2 = new Table();
        addStoreItem(row2, bucketTexture, "Xô", 100, buyButtonStyle, itemStyle);
        row2.add().width(40); // Spacing between items

        // Add Spaceship item (special with money price)
        Table spaceshipTable = new Table();

        // Item image
        Image spaceshipImage = new Image(spaceshipTexture);
        spaceshipTable.add(spaceshipImage).width(90).height(90).padBottom(5);
        spaceshipTable.row();

        // Item name
        Label spaceshipLabel = new Label("Phi thuyền", itemStyle);
        spaceshipTable.add(spaceshipLabel).padBottom(5);
        spaceshipTable.row();

        // Item price
        Label spaceshipPrice = new Label("5.000đ", itemStyle);
        spaceshipTable.add(spaceshipPrice).padBottom(5);
        spaceshipTable.row();

        // Buy button
        TextButton spaceshipBuyButton = new TextButton("MUA", buyButtonStyle);
        spaceshipBuyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("StoreScreen", "Attempted to buy Spaceship - needs payment integration");
            }
        });
        spaceshipTable.add(spaceshipBuyButton).width(100).height(35);

        row2.add(spaceshipTable);

        // Add rows to items table
        itemsTable.add(row1).padBottom(20);
        itemsTable.row();
        itemsTable.add(row2);

        // Add back button
        TextButtonStyle backButtonStyle = new TextButtonStyle();
        backButtonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        backButtonStyle.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        backButtonStyle.font = storeFont;
        backButtonStyle.fontColor = new Color(0.2f, 0.1f, 0f, 1);

        backButton = new TextButton("QUAY LẠI", backButtonStyle);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Return to main menu
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        // Add all elements to the store table with appropriate spacing
        storeTable.add(titleLabel).padTop(20).padBottom(50);
        storeTable.row();
        storeTable.add(itemsTable);
        storeTable.row();
        storeTable.add(backButton).width(140).height(40).padTop(50);

        // Add the panel image to the main table
        mainTable.add(panelImage).width(panelWidth).height(panelHeight);

        // Add the store table to the container with centering
        containerTable.add(storeTable).expand().center();

        // Add tables to stage
        stage.addActor(mainTable);
        stage.addActor(containerTable);
    }

    private void addStoreItem(Table itemsTable, Texture itemTexture, String name, int price,
                              TextButtonStyle buttonStyle, LabelStyle labelStyle) {
        Table itemTable = new Table();

        // Item image
        Image itemImage = new Image(itemTexture);
        itemTable.add(itemImage).width(90).height(90).padBottom(5);
        itemTable.row();

        // Item name
        Label nameLabel = new Label(name, labelStyle);
        itemTable.add(nameLabel).padBottom(5);
        itemTable.row();

        // Price with tomato icon
        Table priceTable = new Table();
        Label priceLabel = new Label(String.valueOf(price) + " ", labelStyle);
        Image tomatoIcon = new Image(tomatoTexture);

        priceTable.add(priceLabel);
        priceTable.add(tomatoIcon).width(16).height(16);

        itemTable.add(priceTable).padBottom(5);
        itemTable.row();

        // Buy button
        final String itemName = name;
        final int itemPrice = price;

        TextButton buyButton = new TextButton("MUA", buttonStyle);
        buyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.log("StoreScreen", "Attempted to buy " + itemName + " for " + itemPrice + " tomatoes");
            }
        });

        itemTable.add(buyButton).width(100).height(35);

        // Add item to items grid
        itemsTable.add(itemTable).pad(10);
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

        // Draw stage (store UI)
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
        if (backgroundImage != null) backgroundImage.dispose();
        if (panelTexture != null) panelTexture.dispose();
        if (buttonTexture != null) buttonTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
        if (bowlTexture != null) bowlTexture.dispose();
        if (boxTexture != null) boxTexture.dispose();
        if (bucketTexture != null) bucketTexture.dispose();
        if (spaceshipTexture != null) spaceshipTexture.dispose();
        if (tomatoTexture != null) tomatoTexture.dispose();
        if (storeFont != null) storeFont.dispose();
        if (titleFont != null) titleFont.dispose();
        stage.dispose();
    }
}
