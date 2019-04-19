package com.fighter.map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.fighter.FighterGame;
import com.fighter.assets.AssetDescriptors;
import com.fighter.assets.RegionNames;
import com.fighter.config.GameConfig;

// TODO finish MapLayout class
public class MapLayout {

    // == Attributes ==
    private FighterGame game;
    private AssetManager assetManager;

    private Image background;

    // == Constructors ==
    public MapLayout(FighterGame game, World world) {
        this.game = game;
        assetManager = game.getAssetManager();

        TextureAtlas mapAtlas = assetManager.get(AssetDescriptors.TEST_MAP);
        background = new Image(mapAtlas.findRegion(RegionNames.TEST_BACKGROUND));
        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);

        // Create platforms and ground
    }

    // == Public Methods ==
    public Image getBackground() {
        return background;
    }
}
