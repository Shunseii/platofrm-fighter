package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.fighter.config.GameConfig;


public abstract class ActorBase extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(ActorBase.class.getName(), Logger.DEBUG);

    private static final float TEST_MOVE_SPEED = 4.0f;
    private static final float TEST_JUMP_SPEED = 1.5f * GameConfig.WORLD_HEIGHT;
    private static final float TEST_MAX_JUMP_DURATION = 0.1f;

    protected static final float CHARACTER_DENSITY = 0.5f;
    protected static final float CHARACTER_FRICTION = 0.4f;
    protected static final float CHARACTER_HEIGHT = 1.0f;
    protected static final float CHARACTER_WIDTH = 0.5f;

    // == Attributes ==
    protected AssetManager assetManager;

    protected BodyDef bodyDef;
    protected Body body;
    protected FixtureDef fixtureDef;
    protected Fixture fixture;

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

        setRegions();
        init();
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
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                MathUtils.radiansToDegrees * body.getAngle()//getRotation()
        );
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
        if (jumpState != JumpState.JUMPING) velocity.y -= GameConfig.GRAVITY;
        position.mulAdd(velocity, delta);

        if (position.y < 0) {
            position.y = 0;
            velocity.y = 0;
            jumpState = JumpState.GROUNDED;
        }

        if (jumpState != JumpState.JUMPING && velocity.y != 0) {
            jumpState = JumpState.FALLING;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveRight(delta);
            body.setLinearVelocity(2.0f, body.getLinearVelocity().y);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveLeft(delta);
            body.setLinearVelocity(-2.0f, body.getLinearVelocity().y);
        } else {
            walkState = WalkState.STANDING;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            switch (jumpState) {
                case GROUNDED:
                    body.applyForce(0f, 50f, body.getWorldCenter().x, body.getWorldCenter().y, true);
                    startJump();
                    break;
                case JUMPING:
                    endJump();
                    break;
            }
        }


        setPosition(position.x, position.y);
    }

    private void moveRight(float delta) {
        if (walkState != WalkState.WALKING) {
            walkStartTime = TimeUtils.nanoTime();
        }
        walkState = WalkState.WALKING;
        facing = Direction.RIGHT;
        position.x += delta * TEST_MOVE_SPEED;
    }

    private void moveLeft(float delta) {
        if (walkState != WalkState.WALKING) {
            walkStartTime = TimeUtils.nanoTime();
        }
        walkState = WalkState.WALKING;
        facing = Direction.LEFT;
        position.x -= delta * TEST_MOVE_SPEED;
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
