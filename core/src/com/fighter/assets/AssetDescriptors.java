package com.fighter.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetDescriptors {

    public static final AssetDescriptor<TextureAtlas> TEST_PLAYER =
            new AssetDescriptor<TextureAtlas>(AssetPaths.TEST_PLAYER, TextureAtlas.class);

    public static final AssetDescriptor<BitmapFont> TEST_FONT =
            new AssetDescriptor<BitmapFont>(AssetPaths.TEST_FONT, BitmapFont.class);

    private AssetDescriptors() {
    }
}
