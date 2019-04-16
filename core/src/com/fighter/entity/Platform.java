package com.fighter.entity;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Platform {

    // == Constants ==
    private final float PLATFORM_DENSITY = 0.0f;
    private final float PLATFORM_WIDTH = 1f;
    private final float PLATFORM_HEIGHT = 1f;

    private float xPosition;
    private float yPosition;

    // == Attributes ==
    private BodyDef bodyDef;
    private Body body;

    // == Constructors ==
    public Platform(World world, float xPos, float yPos) {
        xPosition = xPos;
        yPosition = yPos;

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(xPosition, yPosition));

        body = world.createBody(bodyDef);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(PLATFORM_WIDTH / 2f, PLATFORM_HEIGHT / 2f);

        body.createFixture(bodyShape, PLATFORM_DENSITY);
        body.setUserData(this);

        bodyShape.dispose();
    }

    // == Public methods ==
    public void render(ShapeRenderer renderer, Viewport viewport, OrthographicCamera camera) {
        viewport.apply();

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.rect(xPosition - PLATFORM_WIDTH / 2f, yPosition - PLATFORM_HEIGHT / 2f,
                PLATFORM_WIDTH, PLATFORM_HEIGHT);

        renderer.end();
    }
}
