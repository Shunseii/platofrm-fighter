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

    private CharacterTest player;

    private Body body;

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
        player = new CharacterTest(assetManager);

        stage.addActor(player);
        createGround();
    }

    @Override
    public void render(float delta) {
        dbc.handleDebugInput(delta);
        dbc.applyTo(camera);

        GdxUtils.clearScreen();

        viewport.apply();
        renderGameplay();
        viewport.apply();

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.rect(0, 0, GameConfig.WORLD_WIDTH, 1.0f);
        renderer.circle(body.getPosition().x, body.getPosition().y, 1.0f, 30);

        renderer.end();

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

    private void createGround() {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(5, 5);

        // Create our body in the world using our body definition
        body = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6

        CircleShape circle = new CircleShape();
        circle.setRadius(1f);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

        // Remember to dispose of any shapes after you're done with them!
        // BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();

        // Create our body definition
        BodyDef groundBodyDef = new BodyDef();
        // Set its world position
        groundBodyDef.position.set(new Vector2(0 + GameConfig.WORLD_WIDTH / 2f, 0.5f));

        // Create a body from the defintion and add it to the world
        Body groundBody = world.createBody(groundBodyDef);

        // Create a polygon shape
        PolygonShape groundBox = new PolygonShape();
        // Set the polygon shape as a box which is twice the size of our view port and 20 high
        // (setAsBox takes half-width and half-height as arguments)
        groundBox.setAsBox(GameConfig.WORLD_WIDTH / 2f, 0.5f);
        // Create a fixture from our polygon shape and add it to our ground body
        groundBody.createFixture(groundBox, 0.0f);
        // Clean up after ourselves
        groundBox.dispose();
    }
}
