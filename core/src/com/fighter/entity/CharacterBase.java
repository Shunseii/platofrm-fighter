package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.fighter.config.GameConfig;


public abstract class CharacterBase extends Actor {

    // == Constants ==
    private final Logger LOG = new Logger(CharacterBase.class.getName(), Logger.DEBUG);

    protected float CHARACTER_DENSITY;
    protected float CHARACTER_FRICTION;
    protected float CHARACTER_RESTITUTION;
    protected float CHARACTER_HEIGHT;
    protected float CHARACTER_WIDTH;
    protected float CHARACTER_SPEED;

    protected int MAX_JUMPS;
    protected float JUMP_FORCE;

    // == Attributes ==
    protected AssetManager assetManager;

    // Stats
    protected int health;
    protected int currHealth;
    protected int attack;

    // Physics Body
    protected BodyDef bodyDef;
    protected Body body;
    protected FixtureDef fixtureDef;
    protected Fixture fixture;
    protected Fixture footFixture;

    protected World world;

    // States
    protected Direction facing;
    protected WalkState walkState;
    protected AttackState attackState;

    protected float stateTime;

    // Animations
    protected Animation<TextureRegion> leftStandAnimation;
    protected Animation<TextureRegion> rightStandAnimation;
    protected Animation<TextureRegion> leftWalkAnimation;
    protected Animation<TextureRegion> rightWalkAnimation;
    protected Animation<TextureRegion> leftAttackAnimation;
    protected Animation<TextureRegion> rightAttackAnimation;

    protected TextureRegion leftRegion;
    protected TextureRegion rightRegion;
    protected TextureRegion currentRegion;

    protected ContactListener contactListener = new MyContactListener();
    protected RayCastCallback rayCastCallback;

    protected int numFootContacts;
    protected int numOfJumps;

    public Batch batch1;

    // == Constructors ==
    public CharacterBase(AssetManager assetManager, World world, float xStart, float yStart) {
        this.assetManager = assetManager;
        this.world = world;

        stateTime = 0;
        numFootContacts = 0;
        numOfJumps = 1;

        world.setContactListener(contactListener);

        facing = Direction.RIGHT;
        walkState = WalkState.STANDING;
        attackState = AttackState.IDLE;

        bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(xStart, yStart);

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
        batch1 = batch;

        if (walkState == WalkState.STANDING && attackState == AttackState.IDLE) {
            if (facing == Direction.RIGHT) {
                currentRegion = rightStandAnimation.getKeyFrame(stateTime, true);
            } else {
                currentRegion = leftStandAnimation.getKeyFrame(stateTime, true);
            }
        } else if (attackState == AttackState.ATTACKING) {
            currentRegion = (facing == Direction.LEFT) ?
                    leftAttackAnimation.getKeyFrame(stateTime) : rightAttackAnimation.getKeyFrame(stateTime);

            if (leftAttackAnimation.isAnimationFinished(stateTime)) {
                attackState = AttackState.IDLE;
            }
        }

        batch.draw(currentRegion,
                body.getPosition().x - (CHARACTER_WIDTH / 2f), body.getPosition().y - (CHARACTER_HEIGHT / 2f),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX() , getScaleY(),
                getRotation()
        );
    }

    public void moveRight() {
        if (attackState == AttackState.ATTACKING && numFootContacts >= 1) return;

        walkState = WalkState.WALKING;
        if (attackState != AttackState.ATTACKING) facing = Direction.RIGHT;
        body.setLinearVelocity(CHARACTER_SPEED, body.getLinearVelocity().y);

        currentRegion = rightWalkAnimation.getKeyFrame(stateTime, true);
    }

    public void moveLeft() {
        if (attackState == AttackState.ATTACKING && numFootContacts >= 1) return;

        walkState = WalkState.WALKING;
        if (attackState != AttackState.ATTACKING) facing = Direction.LEFT;
        body.setLinearVelocity(-CHARACTER_SPEED, body.getLinearVelocity().y);

        currentRegion = leftWalkAnimation.getKeyFrame(stateTime, true);
    }

    public void jump() {
        if (attackState == AttackState.ATTACKING) return;

        if (numFootContacts >= 1 || numOfJumps < MAX_JUMPS) {
            ++numOfJumps;
            body.setLinearVelocity(body.getLinearVelocity().x, JUMP_FORCE);
        }
    }

    public void attack() {
        if (attackState == AttackState.IDLE) {
            stateTime = 0;
            attackState = AttackState.ATTACKING;
        }

        Animation attackAnimation = (facing == Direction.LEFT) ?
                leftAttackAnimation : rightAttackAnimation;

        if (attackAnimation.getKeyFrameIndex(stateTime) == 3) {

            ShapeRenderer shapeRenderer = new ShapeRenderer();

            shapeRenderer.setProjectionMatrix(batch1.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.line(getX(), getY(),
                    getX() + 1f, getY());
            shapeRenderer.end();

            world.rayCast(rayCastCallback, getX(), getY(),
                    getX() + 1f, getY());
        }
        //if (attackState == AttackState.ATTACKING && !leftAttackAnimation.isAnimationFinished(stateTime)) {
            //currentRegion = leftAttackAnimation.getKeyFrame(stateTime, false);
        //}

        //if (leftAttackAnimation.isAnimationFinished(stateTime)) {
        //    attackState = AttackState.IDLE;
        //}
    }

    public void takeDamage(int damage, Direction knockback) {
        int forceDirection = (knockback == Direction.RIGHT) ? 1 : -1;
        currHealth -= damage;

        // TODO Apply force proportional to the damage taken
        body.applyForceToCenter(forceDirection * 1f, 2f, true);
    }

    // == Abstract methods ==
    abstract void setRegions();

    abstract void init();

    // == Private methods ==
    private void update(float delta) {
        stateTime += Gdx.graphics.getDeltaTime();

        if (attackState == AttackState.ATTACKING) attack();

        if (numFootContacts >= 1) numOfJumps = 1;

        walkState = WalkState.STANDING;
        if (numFootContacts >= 1) body.setLinearVelocity(0, body.getLinearVelocity().y);

        setPosition(body.getPosition().x, body.getPosition().y);
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

    // == Enums ==
    enum Direction {
        LEFT,
        RIGHT
    }

    enum WalkState {
        WALKING,
        STANDING
    }

    enum AttackState {
        ATTACKING,
        IDLE
    }

    // Contact Listener to check if foot sensor is colliding with ground
    public class MyContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            //check if fixture A was the foot sensor
            Object fixtureUserData = contact.getFixtureA().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData.equals(3))
                ++numFootContacts;

            //check if fixture B was the foot sensor
            fixtureUserData = contact.getFixtureB().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData.equals(3))
                ++numFootContacts;
        }

        @Override
        public void endContact(Contact contact) {
            //check if fixture A was the foot sensor
            Object fixtureUserData = contact.getFixtureA().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData.equals(3))
                --numFootContacts;

            //check if fixture B was the foot sensor
            fixtureUserData = contact.getFixtureB().getUserData();
            if (fixtureUserData == null) return;

            if (fixtureUserData.equals(3))
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
