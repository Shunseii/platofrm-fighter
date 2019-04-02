package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;


public abstract class CharacterBase extends Actor {

    // == Constants ==
    private final Logger LOG = new Logger(CharacterBase.class.getName(), Logger.DEBUG);

    protected float SPRITE_HEIGHT;
    protected float SPRITE_WIDTH;

    protected float CHARACTER_DENSITY;
    protected float CHARACTER_FRICTION;
    protected float CHARACTER_RESTITUTION;
    protected float CHARACTER_HEIGHT;
    protected float CHARACTER_WIDTH;
    protected float CHARACTER_SPEED;
    protected float CHARACTER_DAMPING;

    protected float ATTACK_RANGE;
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
    protected boolean attackHit = false;

    // Animations
    protected Animation<TextureRegion> leftStandAnimation;
    protected Animation<TextureRegion> rightStandAnimation;
    protected Animation<TextureRegion> leftWalkAnimation;
    protected Animation<TextureRegion> rightWalkAnimation;
    protected Animation<TextureRegion> leftAttackAnimation;
    protected Animation<TextureRegion> rightAttackAnimation;

    protected TextureRegion currentRegion;

    protected RayCastCallback rayCastCallback;

    public int numFootContacts;

    protected int numOfJumps;
    protected int entityNumber;

    public Batch testBatch;

    // == Constructors ==
    public CharacterBase(AssetManager assetManager, World world, Vector2 startPosition, int entityNumber) {
        this.assetManager = assetManager;
        this.world = world;
        this.entityNumber = entityNumber;

        stateTime = 0;
        numFootContacts = 0;
        numOfJumps = 1;

        facing = Direction.RIGHT;
        walkState = WalkState.STANDING;
        attackState = AttackState.IDLE;

        bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.linearDamping = CHARACTER_DAMPING;
        bodyDef.position.set(startPosition.x, startPosition.y);

        body = world.createBody(bodyDef);

        fixtureDef = new FixtureDef();
        fixtureDef.filter.groupIndex = -2;

        setRegions();
        init();

        createFootSensor();

        setPosition(body.getPosition().x, body.getPosition().y);
        setSize(SPRITE_WIDTH, SPRITE_HEIGHT);
    }

    // == Public methods ==
    @Override
    public void act(float delta) {
        super.act(delta);
        update(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        testBatch = batch;

        if (walkState == WalkState.STANDING && attackState == AttackState.IDLE) {
            if (facing == Direction.RIGHT) {
                currentRegion = rightStandAnimation.getKeyFrame(stateTime, true);
            } else {
                currentRegion = leftStandAnimation.getKeyFrame(stateTime, true);
            }
        }

        batch.draw(currentRegion,
                body.getPosition().x - (CHARACTER_WIDTH / 2f), body.getPosition().y - (CHARACTER_HEIGHT / 2f),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation()
        );
    }

    public void moveRight() {
        if (attackState == AttackState.ATTACKING && !isJumping() ||
                attackState == AttackState.GUARDING) return;

        walkState = WalkState.WALKING;
        if (attackState != AttackState.ATTACKING) facing = Direction.RIGHT;
        body.setLinearVelocity(CHARACTER_SPEED, body.getLinearVelocity().y);

        if (attackState == AttackState.IDLE)
            currentRegion = rightWalkAnimation.getKeyFrame(stateTime, true);
    }

    public void moveLeft() {
        if (attackState == AttackState.ATTACKING && !isJumping() ||
                attackState == AttackState.GUARDING) return;

        walkState = WalkState.WALKING;
        if (attackState != AttackState.ATTACKING) facing = Direction.LEFT;
        body.setLinearVelocity(-CHARACTER_SPEED, body.getLinearVelocity().y);

        if (attackState == AttackState.IDLE)
            currentRegion = leftWalkAnimation.getKeyFrame(stateTime, true);
    }

    public void jump() {
        if (attackState != AttackState.IDLE) return;

        if (!isJumping() || numOfJumps < MAX_JUMPS) {
            ++numOfJumps;
            body.setLinearVelocity(body.getLinearVelocity().x, JUMP_FORCE);
        }
    }

    public void attack() {
        if (attackState == AttackState.IDLE) {
            stateTime = 0;
            attackState = AttackState.ATTACKING;
        }

        currentRegion = (facing == Direction.LEFT) ?
                leftAttackAnimation.getKeyFrame(stateTime) : rightAttackAnimation.getKeyFrame(stateTime);

        Animation attackAnimation = (facing == Direction.LEFT) ?
                leftAttackAnimation : rightAttackAnimation;

        if (attackAnimation.getKeyFrameIndex(stateTime) == 3 && !attackHit) {
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
        } else if (attackAnimation.getKeyFrameIndex(stateTime) != 3) {
            attackHit = false;
        }

        if (leftAttackAnimation.isAnimationFinished(stateTime)) {
            attackState = AttackState.IDLE;
        }
    }

    public void guard() {
        if (attackState == AttackState.ATTACKING || isJumping()) return;
        attackState = AttackState.GUARDING;
    }

    public void takeDamage(int damage, Direction knockback) {
        // TODO ignore knockback and reduce/ignore when guarding
        int forceDirection = (knockback == Direction.RIGHT) ? 1 : -1;
        currHealth -= damage;

        // TODO Apply force proportional to the damage taken
        body.applyLinearImpulse(new Vector2(forceDirection * 0.25f, 0.25f), new Vector2(0, 0), true);
    }

    public boolean isJumping() {
        return numFootContacts < 1;
    }

    public boolean isGuarding() {
        return attackState == AttackState.GUARDING;
    }

    // == Abstract methods ==
    abstract void setRegions();

    abstract void init();

    // == Private methods ==
    private void update(float delta) {
        stateTime += Gdx.graphics.getDeltaTime();

        if (attackState == AttackState.ATTACKING) attack();

        if (!isJumping()) numOfJumps = 1;

        walkState = WalkState.STANDING;
        attackState = (attackState != AttackState.ATTACKING) ?
                AttackState.IDLE : AttackState.ATTACKING;

        setPosition(body.getPosition().x, body.getPosition().y);
    }

    private void createFootSensor() {
        Vector2 center = new Vector2(0.0f, -0.35f);
        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(CHARACTER_WIDTH / 2f, 0.05f, center, 0.0f);

        fixtureDef.isSensor = true;
        fixtureDef.shape = footShape;
        fixtureDef.density = 1.0f;

        footFixture = body.createFixture(fixtureDef);
        footFixture.setUserData(entityNumber);

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
        GUARDING,
        IDLE
    }
}
