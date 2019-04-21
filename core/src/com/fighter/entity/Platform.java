package com.fighter.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetPaths;
import com.fighter.assets.RegionNames;


// TODO Add drawPlatform code to a common separate class
public class Platform extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(Platform.class.getName(), Logger.DEBUG);
    private final float PLATFORM_DENSITY = 0.0f;
    private final float DEFAULT_WIDTH = 0.5f;
    private final float DEFAULT_HEIGHT = 0.5f;

    // == Attributes ==
    private BodyDef bodyDef;
    private Body body;

    private FighterGame game;
    private AssetManager assetManager;
    private World world;

    private float xPosition;
    private float yPosition;
    private float width;
    private float height;

    // == Constructors ==
    public Platform(FighterGame game, World world, float xPos, float yPos) {
        this.game = game;
        this.world = world;

        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

        xPosition = xPos + (this.width / 2f);
        yPosition = yPos + (this.height / 2f);

        init();
    }

    public Platform(FighterGame game, World world, float xPos, float yPos, float width, float height) {
        this.game = game;
        this.world = world;

        this.width = width;
        this.height = height;

        xPosition = xPos + (this.width / 2f);
        yPosition = yPos + (this.height / 2f);

        init();
    }

    // == init ==
    private void init() {
        assetManager = game.getAssetManager();

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(xPosition, yPosition));

        body = world.createBody(bodyDef);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(width / 2f, height / 2f);

        body.createFixture(bodyShape, PLATFORM_DENSITY);
        body.setUserData(this);

        bodyShape.dispose();
    }

    // == Public methods ==
    @Override
    public void draw(Batch batch, float parentAlpha) {
        float xStart = xPosition - (width / 2f);
        float yStart = yPosition - (height / 2f);

        TextureAtlas mapAtlas = assetManager.get(AssetPaths.TEST_MAP);
        TextureRegion tile = mapAtlas.findRegion(RegionNames.TEST_PLATFORM);

        // If width is < 1, draw texture with that width
        if (width < 1) {
            drawPlatform(batch, tile, xStart, yStart, width, height);
            return;
        }

        // If width > 1, repeatedly draw texture at given breakpoint
        float breakPoint = width / (width + 1);

        for (float i = xStart; i < (width + xStart); i += breakPoint) {
            float currWidth = breakPoint;
            float diffNextIteration = (width + xStart) - (i + breakPoint);
            float tolerance = 0.001f;

            // Check if remaining width of next iteration is < breakPoint
            if (diffNextIteration < breakPoint - tolerance) {
                // If width of next iteration is close to currWidth (diff of 0.2f),
                // then draw at that width in next iteration
                if (Math.abs(diffNextIteration - currWidth) <= 0.2f) {
                    drawPlatform(batch, tile, i, yStart, currWidth, height);
                    continue;
                }

                // If width of next iteration is too small, add it to current breakPoint
                currWidth = breakPoint + diffNextIteration;
                drawPlatform(batch, tile, i, yStart, currWidth, height);
                return;
            }
            drawPlatform(batch, tile, i, yStart, currWidth, height);
        }
    }

    public void renderDebug(ShapeRenderer renderer, Viewport viewport, OrthographicCamera camera) {
        viewport.apply();

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.rect(xPosition - width / 2f, yPosition - height / 2f,
                width, height);

        renderer.end();
    }

    // == Private Methods ==
    private void drawPlatform(Batch batch, TextureRegion region,
                              float x, float y, float width, float height) {
        batch.draw(region,
                x, y,
                getOriginX(), getOriginY(),
                width, height,
                getScaleX(), getScaleY(),
                getRotation()
        );
    }
}
