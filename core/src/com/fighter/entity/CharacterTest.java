package com.fighter.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.fighter.assets.AssetDescriptors;
import com.fighter.assets.RegionNames;

public class CharacterTest extends CharacterBase {

    // == Constants ==
    private final Logger LOG = new Logger(CharacterTest.class.getName(), Logger.DEBUG);

    private final float FRAME_DURATION = 0.15f;

    // == Constructors ==
    public CharacterTest(AssetManager assetManager, World world) {
        super(assetManager, world);
    }

    // == Protected methods ==
    @Override
    protected void init() {
        CHARACTER_DENSITY = 1.0f;
        CHARACTER_FRICTION = 1.0f;
        CHARACTER_RESTITUTION = 0.0f;

        CHARACTER_HEIGHT = 0.8f;
        CHARACTER_WIDTH = 0.8f;
        CHARACTER_SPEED = 2.0f;

        MAX_JUMPS = 2;
        JUMP_FORCE = 7.0f;

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f);

        fixtureDef.shape = bodyShape;
        fixtureDef.density = CHARACTER_DENSITY;
        fixtureDef.friction = CHARACTER_FRICTION;
        fixtureDef.restitution = CHARACTER_RESTITUTION;

        fixture = body.createFixture(fixtureDef);

        bodyShape.dispose();
    }

    @Override
    protected void setRegions() {
        TextureAtlas testAtlas = assetManager.get(AssetDescriptors.TEST_PLAYER);

        rightRegion = testAtlas.findRegions(RegionNames.TEST_RIGHT_STAND).get(0);
        leftRegion = testAtlas.findRegions(RegionNames.TEST_LEFT_STAND).get(0);

        leftWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_WALK),
                        Animation.PlayMode.LOOP
                );

        rightWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_WALK),
                        Animation.PlayMode.LOOP
                );

        leftStandAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_STAND),
                        Animation.PlayMode.LOOP
                );

        rightStandAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_STAND),
                        Animation.PlayMode.LOOP
                );
    }
}
