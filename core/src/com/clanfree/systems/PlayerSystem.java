package com.clanfree.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.clanfree.components.MovementComponent;
import com.clanfree.components.PlayerComponent;
import com.clanfree.components.TransformComponent;
import com.clanfree.configuration.ConfigManager;
import com.clanfree.mode.GameMode;




public class PlayerSystem extends EntitySystem{
	GameMode gameMode;
	
	private Entity player;
	
	private int horizontal = 0;
	private int vertical = 0;

	
	public PlayerSystem(GameMode gameMode, Entity player) {
		this.player = player;
		this.gameMode = gameMode;
	}
	
	@Override
	public void update(float dt) {
		TransformComponent tp = player.getComponent(TransformComponent.class);
		MovementComponent mp = player.getComponent(MovementComponent.class);
		PlayerComponent pc = player.getComponent(PlayerComponent.class);
		
		if (pc.isDead) {
			gameMode.endGame();
			return;
		}
		
		tp.body.applyForceToCenter(mp.accel.cpy().scl(tp.body.getMass()), true);
		
		Vector2 keyAccel = new Vector2(horizontal, vertical);
		if (keyAccel.len() != 0) {
			keyAccel.scl(tp.body.getMass() * PlayerComponent.MOVE_ACC / keyAccel.len());
			tp.body.applyForceToCenter(keyAccel, true);
		}		
		
		if (!mp.velocity.epsilonEquals(0, 0, ConfigManager.epsilon)) {
			tp.rotation = MathUtils.atan2(mp.velocity.y, mp.velocity.x) -
				MathUtils.atan2(1, 0);
		}		
	}

	public void setAccX(float value) {
		MovementComponent mp = player.getComponent(MovementComponent.class); 
		mp.accel.x = value * PlayerComponent.MOVE_ACC;
	}
	
	public void setAccY(float value) {
		MovementComponent mp = player.getComponent(MovementComponent.class); 
		mp.accel.y = value * PlayerComponent.MOVE_ACC;		
	}
	
	public void goUp() {
		vertical += 1;
	}

	public void goDown() {
		vertical -= 1;
	}
	
	public void goLeft() {
		horizontal -= 1;
	}
	
	public void goRight() {
		horizontal += 1;
	}
}
