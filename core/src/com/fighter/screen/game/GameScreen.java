package com.fighter.screen.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetDescriptors;
import com.fighter.assets.RegionNames;
import com.fighter.config.GameConfig;
import com.fighter.entity.CharacterTest;
import com.fighter.entity.Ground;
import com.fighter.utils.GdxUtils;
import com.fighter.utils.ViewportUtils;
import com.fighter.utils.debug.DebugCameraController;

public class GameScreen implements Screen {

    // == Attributes ==
    private final FighterGame game;
    private final AssetManager assetManager;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Stage stage;
    private ShapeRenderer renderer;
    private BitmapFont font;

    private Box2DDebugRenderer debugRenderer;
    private World world;

    private DebugCameraController dbc;

    // TODO Add Box2D Body + Fixture to CharacterTest
    private CharacterTest player;

    private Ground ground;

    // == Constructors ==
    public GameScreen(FighterGame game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.batch = game.getBatch();
        this.world = game.getWorld();
    }

    // == Public methods ===
    @Override
    public void show() {
        debugRenderer = new Box2DDebugRenderer();
        dbc = new DebugCameraController();
        dbc.setStartPosition(GameConfig.WORLD_CENTER_X, GameConfig.WORLD_CENTER_Y);

        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();
        stage = new Stage(viewport, batch);
        player = new CharacterTest(assetManager, world);

        ground = new Ground(world);

        stage.addActor(player);
    }

    @Override
    public void render(float delta) {
        dbc.handleDebugInput(delta);
        dbc.applyTo(camera);

        GdxUtils.clearScreen();

        viewport.apply();
        renderGameplay();

        ground.render(renderer, viewport, camera);

        renderDebug();
        debugRenderer.render(world, camera.combined);

        // TODO Update timeStep to be variable - not static
        world.step(1/60f, 6, 2);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        //ViewportUtils.debugPixelPerUnit(viewport);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    // == Private methods ==
    private void renderDebug() {
        viewport.apply();

        ViewportUtils.drawGrid(viewport, renderer);

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        drawDebug();

        renderer.end();
    }

    private void drawDebug() {
        Color oldColor = renderer.getColor();

        renderer.setColor(Color.MAGENTA);
        renderer.rect(
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

        renderer.setColor(oldColor);
    }

    private void renderGameplay() {
        batch.setProjectionMatrix(camera.combined);
        stage.act();
        stage.draw();
    }
}
