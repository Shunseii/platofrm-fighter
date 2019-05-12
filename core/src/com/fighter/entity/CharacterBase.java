package com.fighter.entity;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
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
import com.badlogic.gdx.utils.TimeUtils;
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
    protected StateMachine<CharacterBase, CharacterState> actionState;
    protected Direction facing;

    protected float stateTime;

    // Animations
    protected Animation<TextureRegion> leftStandAnimation;
    protected Animation<TextureRegion> rightStandAnimation;
    protected Animation<TextureRegion> leftWalkAnimation;
    protected Animation<TextureRegion> rightWalkAnimation;
    protected Animation<TextureRegion> leftAttackAnimation;
    protected Animation<TextureRegion> rightAttackAnimation;
    protected Animation<TextureRegion> leftJumpAnimation;
    protected Animation<TextureRegion> rightJumpAnimation;
    protected Animation<TextureRegion> leftJumpstartAnimation;
    protected Animation<TextureRegion> rightJumpstartAnimation;
    protected Animation<TextureRegion> rightGuardAnimation;
    protected Animation<TextureRegion> leftGuardAnimation;

    protected TextureRegion currentRegion;

    protected RayCastCallback rayCastCallback;

    public int numFootContacts;

    protected int numOfJumps;
    protected int entityNumber;

    protected long lastHit = TimeUtils.millis();
    protected Batch rayBatch;


    // TODO Add Hit, and death animations
    // == Constructors ==
    public CharacterBase(AssetManager assetManager, World world, Vector2 startPosition, int entityNumber) {
        this.assetManager = assetManager;
        this.world = world;
        this.entityNumber = entityNumber;

        actionState = new DefaultStateMachine<CharacterBase, CharacterState>(this, CharacterState.STANDING);

        stateTime = 0;
        numFootContacts = 0;
        numOfJumps = 0;

        facing = Direction.RIGHT;

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

        currentRegion = rightStandAnimation.getKeyFrame(0f, true);
    }

    // == Public methods ==
    @Override
    public void act(float delta) {
        super.act(delta);
        update(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float bodyX = body.getPosition().x - (SPRITE_WIDTH / 2f);
        float bodyY = body.getPosition().y - (CHARACTER_HEIGHT / 2f);

        rayBatch = batch;

        batch.draw(currentRegion,
                bodyX, bodyY,
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation()
        );
    }

    public void drawHealth(ShapeRenderer renderer, Camera textCamera, Camera camera) {
        float healthPercent = (float) currHealth / health;

        float ratew = textCamera.viewportWidth / camera.viewportWidth;
        float rateh = textCamera.viewportHeight / camera.viewportHeight;

        float x = textCamera.position.x - (camera.position.x - body.getPosition().x) * ratew;
        float y = textCamera.position.y - (camera.position.y - body.getPosition().y) * rateh;

        float healthX = x - 35f;
        float healthY = y + 55f;
        float healthWidth = 65f;
        float healthHeight = 10f;

        Color oldColor = renderer.getColor();

        renderer.setProjectionMatrix(textCamera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        // Outline
        renderer.setColor(Color.BLACK);
        renderer.box(healthX - 1f, healthY - 1f, 0,
                healthWidth + 2f, healthHeight + 2f, 0);

        renderer.setColor(Color.RED);
        renderer.box(healthX, healthY, 0,
                healthWidth, healthHeight, 0);

        renderer.setColor(Color.GREEN);
        renderer.box(healthX, healthY, 0,
                healthWidth * healthPercent, healthHeight, 0);

        renderer.end();

        renderer.setColor(oldColor);
    }

    public void moveRight() {
        actionState.changeState(CharacterState.MOVING_RIGHT);
    }

    public void moveLeft() {
        actionState.changeState(CharacterState.MOVING_LEFT);
    }

    public void jump() {
        actionState.changeState(CharacterState.JUMPING);
    }

    public void jumpLeft() {
        actionState.changeState(CharacterState.JUMPING_LEFT);
    }

    public void jumpRight() {
        actionState.changeState(CharacterState.JUMPING_RIGHT);
    }

    public void attack() {
        actionState.changeState(CharacterState.ATTACKING);
    }

    public void guard() {
        actionState.changeState(CharacterState.GUARDING);
    }

    public void stand() {
        actionState.changeState(CharacterState.STANDING);
    }

    public void takeDamage(int damage, Direction knockback) {
        if (TimeUtils.timeSinceMillis(lastHit) < GameConfig.IFRAME_DURATION) return;

        int forceDirection = (knockback == Direction.RIGHT) ? 1 : -1;
        currHealth -= (isGuarding()) ? damage / 2 : damage;
        lastHit = TimeUtils.millis();

        if (currHealth < 0) currHealth = 0;

        if (isGuarding()) return;

        actionState.changeState(CharacterState.KNOCKED_BACK);

        // TODO Apply force proportional to the damage taken
        body.applyLinearImpulse(new Vector2(forceDirection * 0.15f, 0.25f), new Vector2(0, 0), true);
    }

    public boolean isJumping() {
        return numFootContacts < 1;
    }

    public boolean isGuarding() {
        return actionState.getCurrentState() == CharacterState.GUARDING;
    }

    public boolean isAttacking() {
        return actionState.getCurrentState() == CharacterState.ATTACKING;
    }

    public boolean inAir() {
        return body.getLinearVelocity().y > 0 || numFootContacts < 1;
    }

    public boolean isFalling() {
        return actionState.getCurrentState() == CharacterState.FALLING;
    }

    public boolean isKnockedBack() {
        return actionState.getCurrentState() == CharacterState.KNOCKED_BACK;
    }

    // == Abstract methods ==
    abstract void setRegions();

    abstract void attackRaycast();

    abstract void init();

    // == Private methods ==
    private void update(float delta) {
        stateTime += delta;
        actionState.update();
        setPosition(body.getPosition().x, body.getPosition().y);
    }

    private void createFootSensor() {
        Vector2 center = new Vector2(0.0f, -CHARACTER_HEIGHT / 2f);
        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(CHARACTER_WIDTH / 2.1f, 0.03f, center, 0.0f);

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
            }
            return 1f;
        }
    }

    // == Enums ==
    enum Direction {
        LEFT,
        RIGHT
    }

    public enum CharacterState implements State<CharacterBase> {

        STANDING() {
            @Override
            public void update(CharacterBase C) {
                if (C.inAir())
                    C.actionState.changeState(FALLING);

                C.currentRegion = (C.facing == Direction.RIGHT) ?
                        C.rightStandAnimation.getKeyFrame(C.stateTime, true) :
                        C.leftStandAnimation.getKeyFrame(C.stateTime, true);
            }

            @Override
            public void enter(CharacterBase C) {
                if (C.actionState.getPreviousState() != STANDING) {
                    C.stateTime = 0f;
                    C.numOfJumps = 0;
                    C.body.setLinearVelocity(0, C.body.getLinearVelocity().y);
                }
            }
        },

        FALLING() {
            @Override
            public void update(CharacterBase C) {
                if (!C.inAir())
                    C.actionState.changeState(STANDING);

                C.currentRegion = (C.facing == Direction.LEFT) ?
                        C.leftJumpAnimation.getKeyFrame(C.stateTime, true) :
                        C.rightJumpAnimation.getKeyFrame(C.stateTime, true);
            }
        },

        JUMPING() {
            @Override
            public void update(CharacterBase C) {
                if (!C.inAir())
                    C.actionState.changeState(STANDING);

                C.currentRegion = (C.facing == Direction.LEFT) ?
                        C.leftJumpAnimation.getKeyFrame(C.stateTime, true) :
                        C.rightJumpAnimation.getKeyFrame(C.stateTime, true);
            }

            @Override
            public void enter(CharacterBase C) {
                if (!C.isJumping() || C.numOfJumps < C.MAX_JUMPS) {
                    C.stateTime = 0f;
                    ++C.numOfJumps;
                    C.body.setLinearVelocity(C.body.getLinearVelocity().x, C.JUMP_FORCE);
                }
            }
        },

        MOVING_LEFT() {
            @Override
            public void update(CharacterBase C) {
                C.currentRegion = C.leftWalkAnimation.getKeyFrame(C.stateTime, true);
                C.facing = Direction.LEFT;
            }

            @Override
            public void enter(CharacterBase C) {
                if (C.actionState.getPreviousState() != MOVING_LEFT) {
                    C.stateTime = 0f;
                }
                C.body.setLinearVelocity(-C.CHARACTER_SPEED, C.body.getLinearVelocity().y);
            }

            @Override
            public void exit(CharacterBase C) {
                C.body.setLinearVelocity(0, C.body.getLinearVelocity().y);
            }
        },

        MOVING_RIGHT() {
            @Override
            public void update(CharacterBase C) {
                C.currentRegion = C.rightWalkAnimation.getKeyFrame(C.stateTime, true);
                C.facing = Direction.RIGHT;
            }

            @Override
            public void enter(CharacterBase C) {
                if (C.actionState.getPreviousState() != MOVING_RIGHT) {
                    C.stateTime = 0f;
                }
                C.body.setLinearVelocity(C.CHARACTER_SPEED, C.body.getLinearVelocity().y);
            }

            @Override
            public void exit(CharacterBase C) {
                C.body.setLinearVelocity(0, C.body.getLinearVelocity().y);
            }
        },

        ATTACKING() {
            @Override
            public void update(CharacterBase C) {
                if (!C.inAir()) C.body.setLinearVelocity(0, C.body.getLinearVelocity().y);

                C.currentRegion = (C.facing == Direction.LEFT) ?
                        C.leftAttackAnimation.getKeyFrame(C.stateTime) :
                        C.rightAttackAnimation.getKeyFrame(C.stateTime);

                C.attackRaycast();

                if (C.leftAttackAnimation.isAnimationFinished(C.stateTime)) {
                    C.actionState.changeState(STANDING);
                }
            }
        },

        GUARDING() {
            @Override
            public void update(CharacterBase C) {
                C.currentRegion = (C.facing == Direction.RIGHT) ?
                        C.rightGuardAnimation.getKeyFrame(C.stateTime, true) :
                        C.leftGuardAnimation.getKeyFrame(C.stateTime, true);
            }
        },

        KNOCKED_BACK() {
            @Override
            public void update(CharacterBase C) {
                C.body.setLinearDamping(7f);

                if (!C.isJumping() && Math.abs(C.body.getLinearVelocity().x) <= 0.1f)
                    C.actionState.changeState(STANDING);

                //TODO Add hit animation here
            }

            @Override
            public void enter(CharacterBase C) {
                C.body.setLinearVelocity(0, 0);
            }

            @Override
            public void exit(CharacterBase C) {
                C.body.setLinearDamping(0.0f);
            }
        },

        JUMPING_LEFT() {
            @Override
            public void update(CharacterBase C) {
                C.currentRegion = C.leftJumpAnimation.getKeyFrame(C.stateTime, true);
                C.facing = Direction.LEFT;
            }

            @Override
            public void enter(CharacterBase C) {
                C.body.setLinearVelocity(-C.CHARACTER_SPEED, C.body.getLinearVelocity().y);
            }
        },

        JUMPING_RIGHT() {
            @Override
            public void update(CharacterBase C) {
                C.currentRegion = C.rightJumpAnimation.getKeyFrame(C.stateTime, true);
                C.facing = Direction.RIGHT;
            }

            @Override
            public void enter(CharacterBase C) {
                C.body.setLinearVelocity(C.CHARACTER_SPEED, C.body.getLinearVelocity().y);
            }
        };

        @Override
        public void enter(CharacterBase characterBase) {
            if (characterBase.actionState.getCurrentState() != characterBase.actionState.getPreviousState()) {
                characterBase.stateTime = 0f;
            }
        }

        @Override
        public void update(CharacterBase characterBase) {

        }

        @Override
        public void exit(CharacterBase characterBase) {

        }

        @Override
        public boolean onMessage(CharacterBase characterBase, Telegram telegram) {
            return false;
        }
    }
}
