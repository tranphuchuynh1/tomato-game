package com.raindrop.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RaindropGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;

    // Define game dimensions as constants for easy reference
    public static final int GAME_WIDTH = 500;
    public static final int GAME_HEIGHT = 1000;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // Default Arial font
        font.getData().setScale(2.0f); // Make font twice as big
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
