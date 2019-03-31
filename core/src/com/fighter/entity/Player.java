package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
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
    private CharacterBase character;

    // == Constructors ==
    public Player(AssetManager assetManager, World world, int entityNumber) {
        this.character = new CharacterTest(assetManager, world, new Vector2(X_START, Y_START), entityNumber);
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

    // == Private methods ==
    private void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            character.moveRight();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            character.moveLeft();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            character.jump();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            character.attack();
        }
    }
}
