package com.fighter.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fighter.FighterGame;
import com.fighter.assets.AssetDescriptors;
import com.fighter.assets.RegionNames;
import com.fighter.config.GameConfig;
import com.fighter.entity.Ground;
import com.fighter.entity.Platform;

public class MapLayout {

    // == Attributes ==
    private FighterGame game;
    private AssetManager assetManager;

    private Image background;

    private Ground ground;
    private Platform platform_1;

    // == Constructors ==
    public MapLayout(FighterGame game, World world) {
        this.game = game;
        assetManager = game.getAssetManager();

        TextureAtlas mapAtlas = assetManager.get(AssetDescriptors.TEST_MAP);
        background = new Image(mapAtlas.findRegion(RegionNames.TEST_BACKGROUND));
        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);

        ground = new Ground(game, world);
        platform_1 = new Platform(game, world, 2f, 2f, 2f, 0.45f);
    }

    // == Public Methods ==
    public void renderDebug(ShapeRenderer renderer, Viewport viewport, OrthographicCamera camera) {
        ground.renderDebug(renderer, viewport, camera);
        platform_1.renderDebug(renderer, viewport, camera);
    }

    public Image getBackground() {
        return background;
    }

    public void addToStage(Stage stage) {
        //stage.addActor(background);
        stage.addActor(ground);
        stage.addActor(platform_1);
    }
}
