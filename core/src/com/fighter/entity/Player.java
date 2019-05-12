package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.fsm.StackStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.fighter.config.GameConfig;

public class Player extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(Player.class.getName(), Logger.DEBUG);

    private float X_START = (GameConfig.WORLD_WIDTH - 1) / 2f;
    private float Y_START = 1f;

    // == Attributes ==
    public CharacterBase character;
    public StateMachine<Player, InputState> inputState;

    // == Constructors ==
    public Player(AssetManager assetManager, World world, int entityNumber) {
        this.character = new CharacterTestKnight(assetManager, world, new Vector2(X_START, Y_START), entityNumber);
        inputState = new StackStateMachine<Player, InputState>(this, InputState.STANDING);
    }

    // == Public methods ==
    @Override
    public void act(float delta) {
        super.act(delta);
        character.act(delta);
        update();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        character.draw(batch, parentAlpha);
    }

    public void drawHealth(ShapeRenderer renderer, Camera textCamera, Camera camera) {
        character.drawHealth(renderer, textCamera, camera);
    }

    // == Private methods ==
    private void update() {
        // TODO Detect double click

        if (!character.isKnockedBack())
            inputState.update();
    }

    public enum InputState implements State<Player> {

        STANDING() {
            @Override
            public void update(Player player) {
                player.character.stand();

                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    player.inputState.changeState(JUMPING);
                }

                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    player.inputState.changeState(WALKING_RIGHT);
                }

                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    player.inputState.changeState(WALKING_LEFT);
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                    player.inputState.changeState(ATTACKING);
                }

                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    player.inputState.changeState(GUARDING);
                }
            }
        },

        WALKING_LEFT() {
            @Override
            public void update(Player player) {
                if (!Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    player.inputState.changeState(STANDING);

                player.character.moveLeft();

                if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    player.inputState.changeState(JUMPING);
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                    player.inputState.changeState(ATTACKING);
                }

                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    player.inputState.changeState(GUARDING);
                }
            }
        },

        WALKING_RIGHT() {
            @Override
            public void update(Player player) {
                if (!Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                    player.inputState.changeState(STANDING);

                player.character.moveRight();

                if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    player.inputState.changeState(JUMPING);
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                    player.inputState.changeState(ATTACKING);
                }

                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    player.inputState.changeState(GUARDING);
                }
            }
        },

        JUMPING() {
            @Override
            public void update(Player player) {
                if (!player.character.inAir()) {
                    player.inputState.changeState(STANDING);
                }

                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    player.character.jumpRight();
                }

                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    player.character.jumpLeft();
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
                    player.inputState.changeState(ATTACKING);
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    player.character.jump();
                }
            }

            @Override
            public void enter(Player player) {
                player.character.jump();
            }
        },

        GUARDING() {
            @Override
            public void update(Player player) {
                if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                    player.inputState.changeState(STANDING);

                player.character.guard();
            }

            @Override
            public void enter(Player player) {
                player.character.guard();
            }
        },

        ATTACKING() {
            @Override
            public void update(Player player) {
                if (!player.character.isAttacking()) player.inputState.changeState(STANDING);
            }

            @Override
            public void enter(Player player) {
                player.character.attack();
            }
        };

        @Override
        public void enter(Player player) {

        }

        @Override
        public void update(Player player) {

        }

        @Override
        public void exit(Player player) {

        }

        @Override
        public boolean onMessage(Player player, Telegram telegram) {
            return false;
        }
    }
}
