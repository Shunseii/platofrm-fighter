package com.fighter.screen.loading;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetDescriptors;
import com.fighter.config.GameConfig;
import com.fighter.screen.game.GameScreen;
import com.fighter.utils.GdxUtils;

public class LoadingScreen extends ScreenAdapter {

    // == Constants ==
    private static final float PROGRESS_BAR_WIDTH = GameConfig.HUD_WIDTH / 2f;
    private static final float PROGRESS_BAR_HEIGHT = GameConfig.HUD_HEIGHT / 8f;

    // == Attributes ==
    private boolean changeScreen = false;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;
    private BitmapFont font;
    private SpriteBatch batch;

    private float progress;
    private float waitTime = 0.75f;

    private final FighterGame game;
    private final AssetManager assetManager;

    // == Constructors ==
    public LoadingScreen(FighterGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
    }

    // == Public methods ==
    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera);
        renderer = new ShapeRenderer();
        batch = new SpriteBatch();

        assetManager.load(AssetDescriptors.TEST_FONT);
        assetManager.finishLoading();

        assetManager.load(AssetDescriptors.TEST_PLAYER);

        font = assetManager.get(AssetDescriptors.TEST_FONT);
    }

    @Override
    public void render(float delta) {
        update(delta);

        GdxUtils.clearScreen();

        viewport.apply(); // Inform OpenGL to use this viewport -> for multiple viewports
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        draw();

        renderer.end();

        if (changeScreen) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }


    @Override
    public void hide() {
        // Note: Screens don't dispose automatically
        dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    // == Private methods ==
    private void update(float delta) {
        // Progress is between 0 - 1
        progress = assetManager.getProgress();

        // True <=> all assets are loaded
        if (assetManager.update()) {
            waitTime -= delta;

            if (waitTime <= 0) {
                changeScreen = true;
            }
        }
    }

    private void draw() {
        float progressBarX = (GameConfig.HUD_WIDTH - PROGRESS_BAR_WIDTH) / 2f;
        float progressBarY = (GameConfig.HUD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f;

        drawText(progressBarX, progressBarY + PROGRESS_BAR_HEIGHT * 2);

        renderer.rect(progressBarX, progressBarY,
                progress * PROGRESS_BAR_WIDTH,
                PROGRESS_BAR_HEIGHT);
    }

    private void drawText(float x, float y) {
        String text = "Loading Assets: " + MathUtils.round(progress * 100) + "%";

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, text, x, y);
        batch.end();
    }
}
