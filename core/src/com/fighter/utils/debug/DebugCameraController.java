package com.fighter.utils.debug;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;

public class DebugCameraController {

    private static final Logger LOG = new Logger(DebugCameraController.class.getName(), Logger.DEBUG);

    // Attributes
    private Vector2 position = new Vector2();
    private Vector2 startPosition = new Vector2();
    private float zoom = 1.0f;

    private DebugCameraConfig config;

    // Constructor
    public DebugCameraController() {
        config = new DebugCameraConfig();
    }

    // Public methods
    public void setStartPosition(float x, float y) {
        startPosition.set(x, y);
        position.set(x, y);
    }

    public void applyTo(OrthographicCamera camera) {
        camera.position.set(position, 0);
        camera.zoom = zoom;

        // Must update camera after modifying it
        camera.update();
    }

    public void handleDebugInput(float delta) {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
            return;
        }

        float moveSpeed = config.getMoveSpeed() * delta;
        float zoomSpeed = config.getZoomSpeed() * delta;

        // Move controls
        if (config.isLeftPressed())
            moveLeft(moveSpeed);
        if (config.isRightPressed())
            moveRight(moveSpeed);
        if (config.isUpPressed())
            moveUp(moveSpeed);
        if (config.isDownPressed())
            moveDown(moveSpeed);

        // Zoom controls
        if (config.isZoomInPressed()) {
            zoomIn(zoomSpeed);
        } else if (config.isZoomOutPressed()) {
            zoomOut(zoomSpeed);
        }

        // Reset controls
        if (config.isResetKeyPressed()) {
            reset();
        }

        // Log controls
        if (config.isLogKeyPressed()) {
            logDebug();
        }
    }

    // Private methods
    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    private void moveCamera(float xSpeed, float ySpeed) {
        setPosition(position.x + xSpeed, position.y + ySpeed);
    }

    private void setZoom(float value) {
        // If value > MAX_ZOOM_IN -> MAX_ZOOM_IN, value < MAX_ZOOM_OUT -> MAX_ZOOM_OUT
        zoom = MathUtils.clamp(value, config.getMaxZoomIn(), config.getMaxZoomOut());
    }

    private void zoomIn(float zoomSpeed) {
        setZoom(zoom + zoomSpeed);
    }

    private void zoomOut(float zoomSpeed) {
        setZoom(zoom - zoomSpeed);
    }

    private void reset() {
        position.set(startPosition);
        setZoom(1.0f);
    }

    private void logDebug() {
        LOG.debug("Position: " + position + ", Zoom: " + zoom);
    }

    private void moveLeft(float speed) {
        moveCamera(-speed, 0);
    }

    private void moveRight(float speed) {
        moveCamera(speed, 0);
    }

    private void moveUp(float speed) {
        moveCamera(0, speed);
    }

    private void moveDown(float speed) {
        moveCamera(0, -speed);
    }
}

