package com.fighter.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Player extends Actor {

    // == Attributes ==
    private CharacterBase character;

    // == Constructors ==
    public Player(AssetManager assetManager, World world) {
        this.character = new CharacterTest(assetManager, world);
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

        if (Gdx.input.isKeyPressed(Input.Keys.C)) {
            character.attack();
        }
    }
}
