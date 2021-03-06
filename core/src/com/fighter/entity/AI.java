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

// TODO Add AI states
public class AI extends Actor {

    // == Constants ==
    private static final Logger LOG = new Logger(Player.class.getName(), Logger.DEBUG);
    private float X_START = (GameConfig.WORLD_WIDTH - 1) / 2f + 3f;
    private float Y_START = 1f;

    private int sequence;

    // == Attributes ==
    private CharacterBase character;

    // == Constructors ==
    public AI(AssetManager assetManager, World world, int entityNumber) {
        this.character = new CharacterTest(assetManager, world, new Vector2(X_START, Y_START), entityNumber);
        sequence = 0;
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
        // TODO implement AI actions here
        if (sequence == 0) {
            character.moveLeft();
            ++sequence;
        } else {
            character.attack();
        }
    }
}
