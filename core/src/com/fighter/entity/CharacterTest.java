package com.fighter.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.fighter.assets.AssetDescriptors;
import com.fighter.assets.RegionNames;

public class CharacterTest extends CharacterBase {

    // == Constants ==
    private final Logger LOG = new Logger(CharacterTest.class.getName(), Logger.DEBUG);

    private final int ATTACK = 5;
    private final int HEALTH = 100;

    private final float FRAME_DURATION = 0.15f;

    // == Constructors ==
    public CharacterTest(AssetManager assetManager, World world, Vector2 startPosition, int entityNumber) {
        super(assetManager, world, startPosition, entityNumber);
        fixture.setUserData(this);
        body.setUserData(this);

        health = HEALTH;
        attack = ATTACK;
        currHealth = HEALTH;
        rayCastCallback = new MyRaycastCallback();
    }

    // == Init ==
    @Override
    protected void init() {
        CHARACTER_DENSITY = 0.11f;
        CHARACTER_FRICTION = 0.0f;
        CHARACTER_RESTITUTION = 0.0f;
        CHARACTER_DAMPING = 2.0f;

        SPRITE_HEIGHT = 0.8f;
        SPRITE_WIDTH = 0.8f;

        CHARACTER_HEIGHT = 0.65f;
        CHARACTER_WIDTH = 0.8f;
        CHARACTER_SPEED = 2.0f;

        ATTACK_RANGE = 0.70f;
        MAX_JUMPS = 2;
        JUMP_FORCE = 9.0f;

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f);

        fixtureDef.shape = bodyShape;
        fixtureDef.density = CHARACTER_DENSITY;
        fixtureDef.friction = CHARACTER_FRICTION;
        fixtureDef.restitution = CHARACTER_RESTITUTION;

        fixture = body.createFixture(fixtureDef);

        bodyShape.dispose();
    }

    // == Public Methods ==
    @Override
    public void guard() {
    }

    // == Protected Methods ==
    @Override
    protected void setRegions() {
        TextureAtlas testAtlas = assetManager.get(AssetDescriptors.TEST_PLAYER);

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


        leftAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_ATTACK),
                        Animation.PlayMode.NORMAL
                );
        rightAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_ATTACK),
                        Animation.PlayMode.NORMAL
                );

        leftJumpAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_STAND),
                        Animation.PlayMode.LOOP
                );

        rightJumpAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_STAND),
                        Animation.PlayMode.LOOP
                );

        leftJumpstartAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_STAND),
                        Animation.PlayMode.NORMAL
                );

        rightJumpstartAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_STAND),
                        Animation.PlayMode.NORMAL
                );

        rightGuardAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_STAND),
                        Animation.PlayMode.LOOP
                );

        leftGuardAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_STAND),
                        Animation.PlayMode.LOOP
                );
    }

    @Override
    protected void attackRaycast() {
        Animation attackAnimation = (facing == Direction.LEFT) ?
                leftAttackAnimation : rightAttackAnimation;

        if (attackAnimation.getKeyFrameIndex(stateTime) == 3) {
            final float OFFSET = 0.1f;
            int castDirection = (facing == Direction.RIGHT) ? 1 : -1;

            ShapeRenderer shapeRenderer = new ShapeRenderer();

            shapeRenderer.setProjectionMatrix(rayBatch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.line(getX() + castDirection * (-CHARACTER_WIDTH / 2f + OFFSET), getY(),
                    getX() + castDirection * ATTACK_RANGE, getY());
            shapeRenderer.end();

            world.rayCast(rayCastCallback, getX() + castDirection * (-CHARACTER_WIDTH / 2f + OFFSET), getY(),
                    getX() + castDirection * ATTACK_RANGE, getY());
        }
    }
}
