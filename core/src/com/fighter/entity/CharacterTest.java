package com.fighter.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.fighter.assets.AssetDescriptors;
import com.fighter.assets.RegionNames;
import com.fighter.config.GameConfig;

public class CharacterTest extends ActorBase {

    // == Constants ==
    private static final Logger LOG = new Logger(CharacterTest.class.getName(), Logger.DEBUG);
    private static final float FRAME_DURATION = 0.1f;

    // == Constructors ==
    public CharacterTest(AssetManager assetManager, World world) {
        super(assetManager, world);
    }

    // == Protected methods ==
    @Override
    protected void init() {
        X_START = (GameConfig.WORLD_WIDTH - 1) / 2f;
        Y_START = 1f;

        position = new Vector2(X_START, Y_START);
        velocity = new Vector2();

        CHARACTER_DENSITY = 0.5f;
        CHARACTER_FRICTION = 0.4f;
        CHARACTER_HEIGHT = 1.0f;
        CHARACTER_WIDTH = 0.5f;
        CHARACTER_SPEED = 2.0f;

        MAX_JUMPS = 2;
        JUMP_FORCE = 5.0f;

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f);

        fixtureDef.shape = bodyShape;
        fixtureDef.density = CHARACTER_DENSITY;
        fixtureDef.friction = CHARACTER_FRICTION;
        fixtureDef.restitution = 0.0f;

        fixture = body.createFixture(fixtureDef);

        bodyShape.dispose();
    }

    @Override
    protected void setRegions() {
        TextureAtlas testAtlas = assetManager.get(AssetDescriptors.TEST_PLAYER);

        rightRegion = testAtlas.findRegion(RegionNames.TEST_RIGHT_STAND);
        leftRegion = testAtlas.findRegion(RegionNames.TEST_LEFT_STAND);

        leftWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions("test_left_walk"),
                        Animation.PlayMode.LOOP
                );

        rightWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions("test_right_walk"),
                        Animation.PlayMode.LOOP
                );
    }
}
