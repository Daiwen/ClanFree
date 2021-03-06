package com.clanfree.mode;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.clanfree.game.ClanFree;
import com.clanfree.game.WorldBuilder;
import com.clanfree.components.CameraComponent;
import com.clanfree.components.GoreComponent;
import com.clanfree.components.TransformComponent;
import com.clanfree.configuration.ConfigManager;
import com.clanfree.controls.KeyboardPlayerControls;
import com.clanfree.controls.PlayerControls;
import com.clanfree.physics.PhysicsManager;
import com.clanfree.sound.SoundManager;
import com.clanfree.systems.AnimationSystem;
import com.clanfree.systems.ArrowSystem;
import com.clanfree.systems.CameraSystem;
import com.clanfree.systems.GoreSystem;
import com.clanfree.systems.PhysicsSystem;
import com.clanfree.systems.PlayerSystem;
import com.clanfree.systems.RenderingSystem;
import com.clanfree.systems.ZombieSystem;

public class GameMode extends ScreenAdapter {
	private ClanFree game;
	
	private SpriteBatch batch;
	private BitmapFont font;
	
	private Engine engine;
	
	Controller controller = null;
	ControllerListener controllerListener;
	
	private OrthographicCamera cam;
	
	private int zombieCount = 0;

	private boolean isEnd = false;
	private boolean isStart = false;
	private boolean isCountDown = true;
	private boolean isFinished = false;
	private long cdTime = 0;
	

	private Entity player;

	private long time;
	private InputAdapter inputs;

	private long cnt;


	
	public GameMode(ClanFree g) {
		game = g;				
	}
	
	public void show() {		
		zombieCount = 0;
		isEnd = false;
		isStart = false;
		isCountDown = true;
		isFinished = false;
		cdTime = 0;
		cnt = 0;
		
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.scale(50f);
		
		engine = new Engine();
		WorldBuilder.getBuilder().init(engine);
				
		Vector2 spawn = new Vector2(5000*500, 5000*500);
		
		/* Init Character */
		if (Controllers.getControllers().size > 0)
			controller = Controllers.getControllers().first();
		player = WorldBuilder.getBuilder().buildPlayer(spawn);
		Entity arrow = WorldBuilder.getBuilder().buildArrow(player, spawn);
		
		PlayerSystem ps = new PlayerSystem(this, player); 
		ArrowSystem as = new ArrowSystem(arrow);
		
		if (controller != null) {
			controllerListener = new PlayerControls(ps, as);
			controller.addListener(controllerListener);
		}
		
		inputs = new KeyboardPlayerControls(ps, as);
		game.setInputProcessor(inputs);
		
		
		cam = new OrthographicCamera(ConfigManager.camWidth  * ConfigManager.minBlockSize,
								 ConfigManager.camHeight * ConfigManager.minBlockSize);
		
		createCamera(player);
		
		engine.addSystem(ps);
		engine.addSystem(as);
		engine.addSystem(new ZombieSystem(this, engine, player));
		engine.addSystem(new PhysicsSystem());
		engine.addSystem(new CameraSystem());
		engine.addSystem(new AnimationSystem());
		engine.addSystem(new RenderingSystem(batch,cam));
		engine.addSystem(new GoreSystem(engine, batch, cam));
	}
	
	private void createCamera(Entity target) {
		Entity entity = new Entity();
		CameraComponent camera = new CameraComponent();
		camera.camera = cam;
		camera.target = target;
		entity.add(camera);
		engine.addEntity(entity);
	}
	
	@Override
	public void render(float dt) {
		if (isEnd) {
			SoundManager.getInstance().endBackgroundMusic();
			SoundManager.getInstance().stopArrow();
			PhysicsManager.getInstance().clear();
			//for all entities create a particle stuff and remove said entity
			ImmutableArray<Entity> array = engine.getEntitiesFor(Family.getFor(TransformComponent.class));
			
			for(int i = 0; i < array.size(); i++) {
				TransformComponent tc = array.get(i).getComponent(TransformComponent.class);
				WorldBuilder.getBuilder().buildGore(tc.pos);
				engine.removeEntity(array.get(i));
			}
			
			isEnd = false;
			isFinished = true;
		}
			
		if (engine.getEntitiesFor(Family.getFor(GoreComponent.class)).size() == 0 && isFinished) {
			engine.removeAllEntities();
			if(controller != null)
				controller.removeListener(controllerListener);
			game.startCreditMode(System.currentTimeMillis()-time, zombieCount);
		}
		
						
		if (isStart) {
			time = System.currentTimeMillis();
			SoundManager.getInstance().startBackGroundMusic();
			SoundManager.getInstance().startArrow();
			isStart = false;
		}
		
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (isCountDown) {
			if (cdTime == 0){
				cdTime = System.currentTimeMillis();				
			}
			if (System.currentTimeMillis() - cdTime >= 5000){
				isStart = true;
				isCountDown = false;
			}
			
		} else {
			spawnZombies();
		}
		
		PhysicsManager.getInstance().update(dt);
		engine.update(dt);
		
		if(isCountDown) {
			TransformComponent tc = player.getComponent(TransformComponent.class);
			
			String number = Float.toString((5*1000-(System.currentTimeMillis() - cdTime))/1000.0f);
			
			batch.begin();
			font.draw(batch, number, tc.pos.x-900, tc.pos.y+300);
			batch.end();
		}
			
	}
	
	private void spawnZombies() {
			if((System.currentTimeMillis() - time)/1000 <= cnt ||
				cnt >= ConfigManager.spawnMax)
				return;
		
			for (int i = 0; i < cnt; i++) {
				SoundManager.getInstance().startZombie();
				Vector2 pos = getNewZombiePos();
				WorldBuilder.getBuilder().buildZombie(pos);
			}
			
			if (cnt < ConfigManager.spawnMax)			
				cnt++;
	}

	private Vector2 getNewZombiePos() {
		float x = MathUtils.random(-1f, 1f);
		float y = MathUtils.random(-1f, 1f);
		
		TransformComponent tc = player.getComponent(TransformComponent.class);
		
		Vector2 p = new Vector2(x, y);
		p.setLength(1);
		p.scl(3000);
		p.add(tc.pos);
				
		return p;
	}

	public void endGame() {
		isEnd = true;
	}
	
	public void killZombie() {
		zombieCount++;
	}
}
