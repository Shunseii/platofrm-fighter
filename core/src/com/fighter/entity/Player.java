package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Logger;
import com.fighter.config.GameConfig;


// TODO Use state machine for player input
public class Player extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(Player.class.getName(), Logger.DEBUG);

    private float X_START = (GameConfig.WORLD_WIDTH - 1) / 2f;
    private float Y_START = 1f;

    // == Attributes ==
    private CharacterBase character;

    // == Constructors ==
    public Player(AssetManager assetManager, World world, int entityNumber) {
        this.character = new CharacterTestKnight(assetManager, world, new Vector2(X_START, Y_START), entityNumber);
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
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            character.guard();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            character.moveRight();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            character.moveLeft();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            character.attack();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            character.startJump();
        }
    }
}
