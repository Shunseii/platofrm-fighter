package com.fighter.entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.fighter.assets.AssetDescriptors;
import com.fighter.config.GameConfig;


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

    // TODO Fix sprite width/height and center sprites
    @Override
    public void draw(Batch batch, float parentAlpha) {
        float bodyX = body.getPosition().x - (CHARACTER_WIDTH / 2f);
        float bodyY = body.getPosition().y - (CHARACTER_HEIGHT / 2f);

        testBatch = batch;

        if (walkState == WalkState.STANDING && attackState == AttackState.IDLE) {
            if (facing == Direction.RIGHT) {
                currentRegion = rightStandAnimation.getKeyFrame(stateTime, true);
            } else {
                currentRegion = leftStandAnimation.getKeyFrame(stateTime, true);
            }
        }

        SPRITE_HEIGHT = currentRegion.getRegionHeight() / (GameConfig.WORLD_HEIGHT * 6.5f);
        SPRITE_WIDTH = currentRegion.getRegionWidth() / (GameConfig.WORLD_WIDTH * 5);

        batch.draw(currentRegion,
                bodyX - SPRITE_WIDTH / 3f, bodyY,
                getOriginX(), getOriginY(),
                SPRITE_WIDTH, SPRITE_HEIGHT,//getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation()
        );
    }

    public void drawHealth(Batch batch, ShapeRenderer renderer, Camera textCamera, Camera camera) {
        float healthPercent = (float) currHealth / health;

        float ratew = textCamera.viewportWidth / camera.viewportWidth;
        float rateh = textCamera.viewportHeight / camera.viewportHeight;

        float x = textCamera.position.x - (camera.position.x - body.getPosition().x) * ratew;
        float y = textCamera.position.y - (camera.position.y - body.getPosition().y) * rateh;

        Color oldColor = renderer.getColor();

        renderer.setProjectionMatrix(textCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.setColor(Color.RED);
        renderer.box(x - 35f, y + 40f, 0,
                CHARACTER_WIDTH * 85f, CHARACTER_HEIGHT * 10f, 0);

        renderer.setColor(Color.GREEN);
        renderer.box(x - 35f, y + 40f, 0,
                CHARACTER_WIDTH * 85f * healthPercent, CHARACTER_HEIGHT * 10f, 0);

        renderer.end();

        renderer.setColor(oldColor);
    }

    public void moveRight() {
        if (attackState == AttackState.ATTACKING && !isJumping() ||
                attackState == AttackState.GUARDING ||
                walkState == WalkState.KNOCKBACK) return;

        walkState = WalkState.WALKING;
        if (attackState != AttackState.ATTACKING) facing = Direction.RIGHT;
        body.setLinearVelocity(CHARACTER_SPEED, body.getLinearVelocity().y);

        if (attackState == AttackState.IDLE) {
            currentRegion = rightWalkAnimation.getKeyFrame(stateTime, true);
        }
    }

    public void moveLeft() {
        if (attackState == AttackState.ATTACKING && !isJumping() ||
                attackState == AttackState.GUARDING ||
                walkState == WalkState.KNOCKBACK) return;

        walkState = WalkState.WALKING;
        if (attackState != AttackState.ATTACKING) facing = Direction.LEFT;
        body.setLinearVelocity(-CHARACTER_SPEED, body.getLinearVelocity().y);

        if (attackState == AttackState.IDLE) {
            currentRegion = leftWalkAnimation.getKeyFrame(stateTime, true);
        }
    }

    public void jump() {
        if (attackState != AttackState.IDLE ||
                walkState == WalkState.KNOCKBACK) return;

        if (!isJumping() || numOfJumps < MAX_JUMPS) {
            ++numOfJumps;
            body.setLinearVelocity(body.getLinearVelocity().x, JUMP_FORCE);
        }
    }

    public void attack() {
        if (walkState == WalkState.KNOCKBACK) {
            attackState = AttackState.IDLE;
            return;
        }

        if (attackState == AttackState.IDLE) {
            stateTime = 0;
            attackState = AttackState.ATTACKING;
        }

        currentRegion = (facing == Direction.LEFT) ?
                leftAttackAnimation.getKeyFrame(stateTime) : rightAttackAnimation.getKeyFrame(stateTime);

        Animation attackAnimation = (facing == Direction.LEFT) ?
                leftAttackAnimation : rightAttackAnimation;

        // TODO Change into protected method
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
        int forceDirection = (knockback == Direction.RIGHT) ? 1 : -1;
        currHealth -= (isGuarding()) ? damage / 2 : damage;

        if (currHealth < 0) currHealth = 0;

        if (isGuarding()) return;

        if (walkState != WalkState.KNOCKBACK) {
            walkState = WalkState.KNOCKBACK;
            body.setLinearVelocity(0, 0);
        }

        // TODO Apply force proportional to the damage taken
        body.applyLinearImpulse(new Vector2(forceDirection * 0.15f, 0.25f), new Vector2(0, 0), true);
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

        if (body.getLinearVelocity().y == 0)
            body.setLinearVelocity(0, body.getLinearVelocity().y);

        if (attackState == AttackState.ATTACKING) attack();

        if (!isJumping()) numOfJumps = 1;

        // If character is not moving, they are standing
        if (body.getLinearVelocity().x == 0 && body.getLinearVelocity().y == 0)
            walkState = WalkState.STANDING;

        attackState = (attackState != AttackState.ATTACKING) ?
                AttackState.IDLE : AttackState.ATTACKING;

        setPosition(body.getPosition().x, body.getPosition().y);
    }

    private void createFootSensor() {
        Vector2 center = new Vector2(0.0f, -0.35f);
        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(CHARACTER_WIDTH / 3f, 0.03f, center, 0.0f);

        fixtureDef.isSensor = true;
        fixtureDef.shape = footShape;
        fixtureDef.density = 1.0f;

        footFixture = body.createFixture(fixtureDef);
        footFixture.setUserData(entityNumber);

        footShape.dispose();
    }

    class MyRaycastCallback implements RayCastCallback {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 vector2, Vector2 vector21, float v) {
            if (fixture.getUserData() instanceof CharacterBase) {
                CharacterBase hitObject = (CharacterBase) fixture.getUserData();

                Direction damageDirection = (facing == Direction.RIGHT) ? Direction.RIGHT : Direction.LEFT;
                hitObject.takeDamage(attack, damageDirection);

                attackHit = true;
            }
            return 1f;
        }
    }

    // == Enums ==
    enum Direction {
        LEFT,
        RIGHT
    }

    enum WalkState {
        WALKING,
        STANDING,
        KNOCKBACK
    }

    enum AttackState {
        ATTACKING,
        GUARDING,
        IDLE
    }
}
