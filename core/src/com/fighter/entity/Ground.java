package com.fighter.entity;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.config.GameConfig;

public class Ground extends Actor {

    // == Constants ==
    private final float GROUND_DENSITY = 0.0f;
    private final float GROUND_WIDTH = GameConfig.WORLD_WIDTH;
    private final float GROUND_HEIGHT = 1f;
    private final float X_POS = GROUND_WIDTH / 2f;
    private final float Y_POS = GROUND_HEIGHT / 2f;

    // == Attributes ==
    private BodyDef bodyDef;
    private Body body;

    // == Constructors ==
    public Ground(World world) {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(X_POS, Y_POS));

        body = world.createBody(bodyDef);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(X_POS, Y_POS);

        body.createFixture(bodyShape, GROUND_DENSITY);
        body.setUserData(this);

        bodyShape.dispose();
    }

    // == Public methods ==
    public void renderDebug(ShapeRenderer renderer, Viewport viewport, OrthographicCamera camera) {
        viewport.apply();

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.rect(0, 0, GROUND_WIDTH, GROUND_HEIGHT);

        renderer.end();
    }
}
