package com.fighter;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.fighter.screen.game.GameScreen;
import com.fighter.screen.loading.LoadingScreen;

public class FighterGame extends Game {

	// == Attributes ==
	private SpriteBatch batch;
	private AssetManager assetManager;
	private World world;

	// == Public methods ==
	public SpriteBatch getBatch() {
		return batch;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public World getWorld() {
		return world;
	}

	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		batch = new SpriteBatch();
		assetManager = new AssetManager();
		assetManager.getLogger().setLevel(Logger.DEBUG);
		world = new World(new Vector2(0, -10), true);

		setScreen(new LoadingScreen(this));
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		assetManager.dispose();
		world.dispose();
	}
}
