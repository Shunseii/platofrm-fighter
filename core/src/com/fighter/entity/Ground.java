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
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetPaths;
import com.fighter.assets.RegionNames;
import com.fighter.config.GameConfig;

public class Ground extends Actor {

    // == Constants ==
    private final float GROUND_DENSITY = 0.0f;
    private final float GROUND_WIDTH = GameConfig.WORLD_WIDTH;
    private final float GROUND_HEIGHT = 0.85f;

    private final float TEXTURE_HEIGHT = GROUND_HEIGHT + 0.125f;

    private final float X_POS = GROUND_WIDTH / 2f;
    private final float Y_POS = GROUND_HEIGHT / 2f;

    // == Attributes ==
    private BodyDef bodyDef;
    private Body body;

    private FighterGame game;
    private AssetManager assetManager;

    // == Constructors ==
    public Ground(FighterGame game, World world) {
        this.game = game;
        this.assetManager = game.getAssetManager();

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(X_POS, Y_POS));

        body = world.createBody(bodyDef);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(X_POS, Y_POS);

        body.createFixture(bodyShape, GROUND_DENSITY);
        body.setUserData(this);

        bodyShape.dispose();
    }

    // == Public methods ==
    @Override
    public void draw(Batch batch, float parentAlpha) {
        float xStart = X_POS - (GROUND_WIDTH / 2f);
        float yStart = Y_POS - (GROUND_HEIGHT / 2f);

        TextureAtlas mapAtlas = assetManager.get(AssetPaths.TEST_MAP);
        TextureRegion tile = mapAtlas.findRegion(RegionNames.TEST_TILE);

        // If GROUND_WIDTH is < 1, draw texture with that GROUND_WIDTH
        if (GROUND_WIDTH < 1) {
            drawPlatform(batch, tile, xStart, yStart, GROUND_WIDTH, TEXTURE_HEIGHT);
            return;
        }

        // If GROUND_WIDTH > 1, repeatedly draw texture at given breakpoint
        float breakPoint = GROUND_WIDTH / (GROUND_WIDTH + 1);

        for (float i = xStart; i < (GROUND_WIDTH + xStart); i += breakPoint) {
            float currWidth = breakPoint;
            float diffNextIteration = (GROUND_WIDTH + xStart) - (i + breakPoint);
            float tolerance = 0.001f;

            // Check if remaining GROUND_WIDTH of next iteration is < breakPoint
            if (diffNextIteration < breakPoint - tolerance) {
                // If GROUND_WIDTH of next iteration is close to currWidth (diff of 0.2f),
                // then draw at that GROUND_WIDTH in next iteration
                if (Math.abs(diffNextIteration - currWidth) <= 0.2f) {
                    drawPlatform(batch, tile, i, yStart, currWidth, TEXTURE_HEIGHT);
                    continue;
                }

                // If GROUND_WIDTH of next iteration is too small, add it to current breakPoint
                currWidth = breakPoint + diffNextIteration;
                drawPlatform(batch, tile, i, yStart, currWidth, TEXTURE_HEIGHT);
                return;
            }
            drawPlatform(batch, tile, i, yStart, currWidth, TEXTURE_HEIGHT);
        }
    }

    public void renderDebug(ShapeRenderer renderer, Viewport viewport, OrthographicCamera camera) {
        viewport.apply();

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.rect(0, 0, GROUND_WIDTH, GROUND_HEIGHT);

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
