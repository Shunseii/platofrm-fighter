package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.fighter.config.GameConfig;


public abstract class ActorBase extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(ActorBase.class.getName(), Logger.DEBUG);

    //private static final float TEST_MOVE_SPEED = 4.0f;
    private static final float TEST_JUMP_SPEED = 1.5f * GameConfig.WORLD_HEIGHT;
    private static final float TEST_MAX_JUMP_DURATION = 0.1f;

    protected static final float CHARACTER_DENSITY = 0.5f;
    protected static final float CHARACTER_FRICTION = 0.4f;
    protected static final float CHARACTER_HEIGHT = 1.0f;
    protected static final float CHARACTER_WIDTH = 0.5f;
    protected static final float CHARACTER_SPEED = 2.0f;

    // == Attributes ==
    protected AssetManager assetManager;

    protected BodyDef bodyDef;
    protected Body body;
    protected FixtureDef fixtureDef;
    protected Fixture fixture;
    protected Fixture footFixture;

    protected World world;

    protected Vector2 position;
    protected Vector2 velocity;

    protected Direction facing;
    protected JumpState jumpState;
    protected WalkState walkState;

    protected long jumpStartTime;
    protected long walkStartTime;

    protected TextureRegion leftRegion;
    protected TextureRegion rightRegion;
    protected Animation<TextureRegion> leftWalkAnimation;
    protected Animation<TextureRegion> rightWalkAnimation;

    // == Constructors ==
    public ActorBase(AssetManager assetManager, World world) {
        this.assetManager = assetManager;
        this.world = world;

        facing = Direction.RIGHT;
        jumpState = JumpState.GROUNDED;
        walkState = WalkState.STANDING;

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set((GameConfig.WORLD_WIDTH - 1) / 2f, 1f);

        body = world.createBody(bodyDef);

        fixtureDef = new FixtureDef();

        setRegions();
        init();

        createFootSensor();

        setPosition(position.x, position.y);
        setSize(CHARACTER_WIDTH, CHARACTER_HEIGHT);
    }

    // == Public methods ==
    @Override
    public void act(float delta) {
        super.act(delta);
        update(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion region = (facing == Direction.RIGHT ? rightRegion : leftRegion);

        if (facing == Direction.RIGHT && walkState == WalkState.WALKING) {
            float walkTimeSeconds = MathUtils.nanoToSec * (TimeUtils.nanoTime() - walkStartTime);
            region = rightWalkAnimation.getKeyFrame(walkTimeSeconds);
        } else if (facing == Direction.LEFT && walkState == WalkState.WALKING) {
            float walkTimeSeconds = MathUtils.nanoToSec * (TimeUtils.nanoTime() - walkStartTime);
            region = leftWalkAnimation.getKeyFrame(walkTimeSeconds);
        }

        batch.draw(region,
                body.getPosition().x - (CHARACTER_WIDTH / 2f), body.getPosition().y - (CHARACTER_HEIGHT / 2f),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation()
        );

        LOG.debug("X: " + body.getPosition().x + " Y: " + body.getPosition().y);
    }

    // == Protected methods ==
    @Override
    protected void positionChanged() {
    }

    @Override
    protected void sizeChanged() {
    }

    // == Abstract methods ==
    abstract void setRegions();
    abstract void init();

    // == Private methods ==
    private void update(float delta) {
        //if (jumpState != JumpState.JUMPING) velocity.y -= GameConfig.GRAVITY;
        //position.mulAdd(velocity, delta);

        /*if (position.y < 0) {
            position.y = 0;
            velocity.y = 0;
            jumpState = JumpState.GROUNDED;
        }*/

        /*if (jumpState != JumpState.JUMPING && velocity.y != 0) {
            jumpState = JumpState.FALLING;
        }*/

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveRight();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveLeft();
        } else {
            walkState = WalkState.STANDING;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            //switch (jumpState) {
            //    case GROUNDED:
                    body.applyForce(0f, 25f, body.getWorldCenter().x, body.getWorldCenter().y, true);
            //        startJump();
            //        break;
            //    case JUMPING:
            //        endJump();
            //        break;
            //}
        }

        setPosition(body.getPosition().x, body.getPosition().y);
    }

    private void moveRight() {
        walkState = WalkState.WALKING;
        facing = Direction.RIGHT;
        body.setLinearVelocity(CHARACTER_SPEED, body.getLinearVelocity().y);
    }

    private void moveLeft() {
        walkState = WalkState.WALKING;
        facing = Direction.LEFT;
        body.setLinearVelocity(-CHARACTER_SPEED, body.getLinearVelocity().y);
    }

    private void startJump() {
        jumpState = JumpState.JUMPING;
        jumpStartTime = TimeUtils.nanoTime();
        continueJump();
    }

    private void continueJump() {
        if (jumpState == JumpState.JUMPING) {
            float jumpDuration = MathUtils.nanoToSec * (TimeUtils.nanoTime() - jumpStartTime);

            if (jumpDuration < TEST_MAX_JUMP_DURATION) {
                velocity.y = TEST_JUMP_SPEED;
            } else {
                endJump();
            }
        }
    }

    private void endJump() {
        if (jumpState == JumpState.JUMPING) jumpState = JumpState.FALLING;
    }

    private void createFootSensor() {
        Vector2 center = new Vector2(0.0f, -0.5f);
        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(0.1f, 0.1f, center, 0.0f);

        fixtureDef.isSensor = true;
        fixtureDef.shape = footShape;
        fixtureDef.density = 1.0f;

        footFixture = body.createFixture(fixtureDef);

        footShape.dispose();
    }

    // == Enums ==
    enum Direction {
        LEFT,
        RIGHT
    }

    enum WalkState {
        WALKING,
        STANDING
    }

    enum JumpState {
        GROUNDED,
        JUMPING,
        FALLING
    }
}
