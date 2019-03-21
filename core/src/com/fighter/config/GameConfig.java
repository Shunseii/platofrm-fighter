package com.fighter.config;

public class GameConfig {

    // == Constants ==
    public static final float WIDTH = 800.0f; // Pixels
    public static final float HEIGHT = 480.0f; // Pixels

    public static final float HUD_WIDTH = 800.0f; // Pixels
    public static final float HUD_HEIGHT = 480.0f; // Pixels

    public static final float WORLD_WIDTH = 10.0f;
    public static final float WORLD_HEIGHT = 6.0f;

    public static final float WORLD_CENTER_X = WORLD_WIDTH / 2; // World units
    public static final float WORLD_CENTER_Y = WORLD_HEIGHT / 2; // World units

    public static final float GRAVITY = WORLD_HEIGHT / 10f;

    // == Constructors ==
    private GameConfig() {}
}
