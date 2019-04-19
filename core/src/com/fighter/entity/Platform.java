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
    private final float DEFAULT_WIDTH = 1f;
    private final float DEFAULT_HEIGHT = 1f;

    // Offset to place body at bottom left instead of center
    private final float OFFSET = 0.5f;

    // == Attributes ==
    private BodyDef bodyDef;
    private Body body;

    private World world;

    private float xPosition;
    private float yPosition;
    private float width;
    private float height;

    // == Constructors ==
    public Platform(World world, float xPos, float yPos) {
        this.world = world;

        xPosition = xPos - OFFSET;
        yPosition = yPos + OFFSET;

        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

        init();
    }

    public Platform(World world, float xPos, float yPos, float width, float height) {
        this.world = world;

        xPosition = xPos - OFFSET;
        yPosition = yPos + OFFSET;

        this.width = width;
        this.height = height;
    }

    // == Public methods ==
    public void render(ShapeRenderer renderer, Viewport viewport, OrthographicCamera camera) {
        viewport.apply();

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.rect(xPosition - width / 2f, yPosition - height / 2f,
                width, height);

        renderer.end();
    }

    // == Private Methods ==
    private void init() {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(xPosition, yPosition));

        body = world.createBody(bodyDef);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(width / 2f, height / 2f);

        body.createFixture(bodyShape, PLATFORM_DENSITY);
        body.setUserData(this);

        bodyShape.dispose();
    }
}
