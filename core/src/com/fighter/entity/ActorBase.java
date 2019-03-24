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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.fighter.config.GameConfig;


public abstract class ActorBase extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(ActorBase.class.getName(), Logger.DEBUG);

    protected static float X_START;
    protected static float Y_START;

    protected static float CHARACTER_DENSITY;
    protected static float CHARACTER_FRICTION;
    protected static float CHARACTER_HEIGHT;
    protected static float CHARACTER_WIDTH;
    protected static float CHARACTER_SPEED;

    protected static int MAX_JUMPS;
    protected static float JUMP_FORCE;

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

    protected long walkStartTime;

    protected TextureRegion leftRegion;
    protected TextureRegion rightRegion;
    protected Animation<TextureRegion> leftWalkAnimation;
    protected Animation<TextureRegion> rightWalkAnimation;

    protected MyContactListener contactListener = new MyContactListener();
    protected int numFootContacts;
    protected int numOfJumps;

    // == Constructors ==
    public ActorBase(AssetManager assetManager, World world) {
        this.assetManager = assetManager;
        this.world = world;

        numFootContacts = 0;
        numOfJumps = 1;

        world.setContactListener(contactListener);

        facing = Direction.RIGHT;
        jumpState = JumpState.GROUNDED;
        walkState = WalkState.STANDING;

        bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(X_START, Y_START);

        body = world.createBody(bodyDef);

        fixtureDef = new FixtureDef();

        setRegions();
        init();

        createFootSensor();

        setPosition(body.getPosition().x, body.getPosition().y);
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
        if (numFootContacts >= 1) numOfJumps = 1;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveRight();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveLeft();
        } else {
            walkState = WalkState.STANDING;
            if (numFootContacts >= 1) body.setLinearVelocity(0, body.getLinearVelocity().y);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (numFootContacts >= 1 || numOfJumps < MAX_JUMPS) {
                ++numOfJumps;
                jump();
            }
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

    private void createFootSensor() {
        Vector2 center = new Vector2(0.0f, -0.5f);
        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(0.1f, 0.1f, center, 0.0f);

        fixtureDef.isSensor = true;
        fixtureDef.shape = footShape;
        fixtureDef.density = 1.0f;

        footFixture = body.createFixture(fixtureDef);
        footFixture.setUserData(3);

        footShape.dispose();
    }

    private void jump() {
        body.setLinearVelocity(body.getLinearVelocity().x, JUMP_FORCE);
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

    public class MyContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            Object fixtureUserData = contact.getFixtureA().getUserData();
            if (fixtureUserData == null) return;

            if ((Integer) fixtureUserData == 3)
                ++numFootContacts;
            //check if fixture B was the foot sensor
            fixtureUserData = contact.getFixtureB().getUserData();
            if (fixtureUserData == null) return;

            if ((Integer) fixtureUserData == 3)
                ++numFootContacts;
        }

        @Override
        public void endContact(Contact contact) {
            Object fixtureUserData = contact.getFixtureA().getUserData();
            if (fixtureUserData == null) return;

            if ((Integer) fixtureUserData == 3)
                --numFootContacts;
            //check if fixture B was the foot sensor
            fixtureUserData = contact.getFixtureB().getUserData();
            if (fixtureUserData == null) return;

            if ((Integer) fixtureUserData == 3)
                --numFootContacts;
        }

        @Override
        public void preSolve(Contact contact, Manifold manifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse contactImpulse) {

        }
    }
}
