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

    // == Init ==
    @Override
    protected void init() {
        CHARACTER_DENSITY = 0.11f;
        CHARACTER_FRICTION = 0.0f;
        CHARACTER_RESTITUTION = 0.0f;
        CHARACTER_DAMPING = 2.0f;

        SPRITE_HEIGHT = 1.7f;
        SPRITE_WIDTH = 2.0f;

        CHARACTER_HEIGHT = 1.1f;
        CHARACTER_WIDTH = 0.7f;
        CHARACTER_SPEED = 2.0f;

        ATTACK_RANGE = 0.85f;
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

    // == Public Methods ==
    @Override
    public void startJump() {
        super.startJump();

        if (walkState != WalkState.JUMPSTART) {
            if (isJumping()) {
                jump();
            } else {
                stateTime = 0;
            }
        }

        Animation<TextureRegion> startAnimation = (facing == Direction.RIGHT) ?
                rightJumpstartAnimation : leftJumpstartAnimation;

        if (numFootContacts < 1) return;

        walkState = WalkState.JUMPSTART;
        currentRegion = startAnimation.getKeyFrame(stateTime, false);

        if (startAnimation.getKeyFrameIndex(stateTime) == 4) {
            jump();
        }
    }

    // == Protected Methods ==
    @Override
    protected void setRegions() {
        TextureAtlas testAtlas = assetManager.get(AssetDescriptors.TEST_PLAYER);

        leftWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.KNIGHT_RUN_LEFT),
                        Animation.PlayMode.LOOP
                );
        rightWalkAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.KNIGHT_RUN_RIGHT),
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
                        Animation.PlayMode.NORMAL
                );
        rightAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.KNIGHT_ATTACK1_RIGHT),
                        Animation.PlayMode.NORMAL
                );

        leftJumpAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.KNIGHT_JUMP_LEFT),
                        Animation.PlayMode.LOOP
                );

        rightJumpAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION,
                        testAtlas.findRegions(RegionNames.KNIGHT_JUMP_RIGHT),
                        Animation.PlayMode.LOOP
                );

        leftJumpstartAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 5f,
                        testAtlas.findRegions(RegionNames.KNIGHT_JUMPSTART_LEFT),
                        Animation.PlayMode.NORMAL
                );

        rightJumpstartAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 5f,
                        testAtlas.findRegions(RegionNames.KNIGHT_JUMPSTART_RIGHT),
                        Animation.PlayMode.NORMAL
                );
    }

    @Override
    protected void attackRaycast() {
        Animation attackAnimation = (facing == Direction.LEFT) ?
                leftAttackAnimation : rightAttackAnimation;

        if (attackAnimation.getKeyFrameIndex(stateTime) >= 6 && attackAnimation.getKeyFrameIndex(stateTime) <= 9
                && !attackHit) {
            final float OFFSET = 0.1f;
            int castDirection = (facing == Direction.RIGHT) ? 1 : -1;

            ShapeRenderer shapeRenderer = new ShapeRenderer();

            shapeRenderer.setProjectionMatrix(testBatch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.line(getX() + castDirection * (-CHARACTER_WIDTH / 2f + OFFSET), getY(),
                    getX() + castDirection * ATTACK_RANGE, getY());
            shapeRenderer.end();

            world.rayCast(rayCastCallback, getX() + castDirection * (-CHARACTER_WIDTH / 2f + OFFSET), getY(),
                    getX() + castDirection * ATTACK_RANGE, getY());
        } else if (attackAnimation.getKeyFrameIndex(stateTime) < 6 || attackAnimation.getKeyFrameIndex(stateTime) > 9) {
            attackHit = false;
        }
    }
}
