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

    // == Attributes ==
    protected AssetManager assetManager;
    protected Rectangle collisionShape = new Rectangle();

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
    public ActorBase(AssetManager assetManager) {
        this.assetManager = assetManager;
        facing = Direction.RIGHT;
        jumpState = JumpState.GROUNDED;
        walkState = WalkState.STANDING;

        setRegions();
        init();
        setPosition(position.x, position.y);
        setSize(0.5f, 1f);
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
                getRotation()
        );
    }

    // == Protected methods ==
    @Override
    protected void positionChanged() {
        updateCollisionShape();
    }

    @Override
    protected void sizeChanged() {
        updateCollisionShape();
    }

    // == Abstract methods ==
    abstract void setRegions();
    abstract void init();

    // == Private methods ==
    private void updateCollisionShape() {
        float halfWidth = getWidth() / 2f;
        float halfHeight = getHeight() / 2f;

        collisionShape.setPosition(getX() + halfWidth, getY() + halfHeight);
    }

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
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveLeft(delta);
        } else {
            walkState = WalkState.STANDING;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            switch (jumpState) {
                case GROUNDED:
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
