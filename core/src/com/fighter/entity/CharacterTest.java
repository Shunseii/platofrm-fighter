package com.fighter.entity;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
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

    // == Protected methods ==
    @Override
    protected void init() {
        CHARACTER_DENSITY = 0.0f;
        CHARACTER_FRICTION = 1.0f;
        CHARACTER_RESTITUTION = 0.0f;

        CHARACTER_HEIGHT = 0.8f;
        CHARACTER_WIDTH = 0.8f;
        CHARACTER_SPEED = 2.0f;

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


        leftAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.TEST_LEFT_ATTACK),
                        Animation.PlayMode.LOOP
                );
        rightAttackAnimation =
                new Animation<TextureRegion>(
                        FRAME_DURATION / 2f,
                        testAtlas.findRegions(RegionNames.TEST_RIGHT_ATTACK),
                        Animation.PlayMode.NORMAL
                );
    }

    class MyRaycastCallback implements RayCastCallback {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 vector2, Vector2 vector21, float v) {
            if (fixture.getUserData() instanceof CharacterBase){
                CharacterBase hitObject = (CharacterBase) fixture.getUserData();

                Direction damageDirection = (facing == Direction.RIGHT) ? Direction.RIGHT : Direction.LEFT;
                hitObject.takeDamage(attack, damageDirection);
            }
            return 1;
        }
    }
}
