package com.fighter.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetPaths;
import com.fighter.assets.RegionNames;
import com.fighter.config.GameConfig;

public class Platform extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(Platform.class.getName(), Logger.DEBUG);
    private final float PLATFORM_DENSITY = 0.0f;
    private final float DEFAULT_WIDTH = 0.5f;
    private final float DEFAULT_HEIGHT = 0.5f;

    // Offset to place body at bottom left instead of center
    private final float OFFSET = 0.5f;

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

        xPosition = xPos - OFFSET;
        yPosition = yPos + OFFSET;

        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

        init();
    }

    public Platform(FighterGame game, World world, float xPos, float yPos, float width, float height) {
        this.game = game;
        this.world = world;

        xPosition = xPos - OFFSET;
        yPosition = yPos + OFFSET;

        this.width = width;
        this.height = height;

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
        TextureRegion tile = mapAtlas.findRegion(RegionNames.TEST_TILE);

        // TODO repeat texture
        /*
        batch.draw(
                tile,
                xPosition - (width / 2f), yPosition - (height / 2f),
                getOriginX(), getOriginY(),
                width, height,
                getScaleX(), getScaleY(),
                getRotation()
        );*/
        LOG.debug("Size: " + tile.getRegionWidth());

        for (float i = xStart; i < width; ++i) {
            batch.draw(tile,
                    i, yStart,
                    getOriginX(), getOriginY(),
                    1f, height,
                    getScaleX(), getScaleY(),
                    getRotation()
            );
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
}
