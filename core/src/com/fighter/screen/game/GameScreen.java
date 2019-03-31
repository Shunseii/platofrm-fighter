package com.fighter.screen.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetDescriptors;
import com.fighter.config.GameConfig;
import com.fighter.entity.CharacterBase;
import com.fighter.entity.CharacterTest;
import com.fighter.entity.Ground;
import com.fighter.entity.Player;
import com.fighter.utils.GdxUtils;
import com.fighter.utils.ViewportUtils;
import com.fighter.utils.debug.DebugCameraController;

public class GameScreen implements Screen {

    // == Constants ==
    private static final Logger LOG = new Logger(GameScreen.class.getName(), Logger.DEBUG);

    // == Attributes ==
    private final FighterGame game;
    private final AssetManager assetManager;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Stage stage;
    private ShapeRenderer renderer;
    private BitmapFont font;

    private DebugCameraController dbc;
    private Box2DDebugRenderer debugRenderer;

    private World world;
    private Ground ground;

    private ContactListener contactListener = new MyContactListener();

    private Player player;

    private Array<Actor> entities = new Array<Actor>();

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

        world.setContactListener(contactListener);

        font = assetManager.get(AssetDescriptors.TEST_FONT);

        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        CharacterBase enemy = new CharacterTest(assetManager, world, new Vector2(7,1),
                entities.size + 1);
        entities.add(enemy);

        stage = new Stage(viewport, batch);
        player = new Player(assetManager, world, entities.size + 1);
        entities.add(player);

        ground = new Ground(world);

        stage.addActor(player);
        stage.addActor(enemy);
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
        world.step(1 / 60f, 6, 2);
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

        /*renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        drawDebug();

        renderer.end();*/
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

    // Contact Listener to check if foot sensor is colliding with ground
    public class MyContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            //check if fixture A was the foot sensor
            Object fixtureUserData = contact.getFixtureA().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData instanceof Integer) {
                CharacterBase character = (CharacterBase) contact.getFixtureA().getBody().getUserData();
                ++character.numFootContacts;
            }
            //check if fixture B was the foot sensor
            fixtureUserData = contact.getFixtureB().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData instanceof Integer) {
                CharacterBase character = (CharacterBase) contact.getFixtureB().getBody().getUserData();
                ++character.numFootContacts;
            }
        }

        @Override
        public void endContact(Contact contact) {
            //check if fixture A was the foot sensor
            Object fixtureUserData = contact.getFixtureA().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData instanceof Integer) {
                CharacterBase character = (CharacterBase) contact.getFixtureA().getBody().getUserData();
                --character.numFootContacts;
            }

            //check if fixture B was the foot sensor
            fixtureUserData = contact.getFixtureB().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData instanceof Integer) {
                CharacterBase character = (CharacterBase) contact.getFixtureB().getBody().getUserData();
                --character.numFootContacts;
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold manifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse contactImpulse) {

        }
    }
}
