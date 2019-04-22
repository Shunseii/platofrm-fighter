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

public class CharacterTestKnight extends CharacterBase {

    // == Constants ==
    private final Logger LOG = new Logger(CharacterTest.class.getName(), Logger.DEBUG);

    private final int ATTACK = 15;
    private final int HEALTH = 120;

    private final float FRAME_DURATION = 0.1f;

    // == Constructors ==
    public CharacterTestKnight(AssetManager assetManager, World world, Vector2 startPosition, int entityNumber) {
        super(assetManager, world, startPosition, entityNumber);
        fixture.setUserData(this);
        body.setUserData(this);

        health = HEALTH;
        attack = ATTACK;
        currHealth = HEALTH;
        rayCastCallback = new MyRaycastCallback();
    }

    // == Protected Methods ==
    @Override
    protected void init() {
        CHARACTER_DENSITY = 0.11f;
        CHARACTER_FRICTION = 0.0f;
        CHARACTER_RESTITUTION = 0.0f;
        CHARACTER_DAMPING = 2.0f;

        SPRITE_HEIGHT = 1.0f;
        SPRITE_WIDTH = 0.8f;

        CHARACTER_HEIGHT = 0.65f;
        CHARACTER_WIDTH = 0.8f;
        CHARACTER_SPEED = 2.0f;

        ATTACK_RANGE = 0.70f;
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

        leftWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(/*RegionNames.TEST_LEFT_WALK*/RegionNames.KNIGHT_IDLE_LEFT),
                        Animation.PlayMode.LOOP
                );
        rightWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(/*RegionNames.TEST_RIGHT_WALK*/RegionNames.KNIGHT_IDLE_RIGHT),
                        Animation.PlayMode.LOOP
                );

        leftStandAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.KNIGHT_IDLE_LEFT),
                        Animation.PlayMode.LOOP
                );
        rightStandAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.KNIGHT_IDLE_RIGHT),
                        Animation.PlayMode.LOOP
                );


        leftAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.KNIGHT_ATTACK1_LEFT),
                        Animation.PlayMode.LOOP
                );
        rightAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.KNIGHT_ATTACK1_RIGHT),
                        Animation.PlayMode.NORMAL
                );

        SPRITE_HEIGHT = leftStandAnimation.getKeyFrame(0).getRegionHeight();
        SPRITE_WIDTH = leftStandAnimation.getKeyFrame(0).getRegionWidth();
    }
}
