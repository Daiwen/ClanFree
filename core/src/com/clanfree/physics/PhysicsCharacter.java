package com.clanfree.physics;


import com.badlogic.gdx.physics.box2d.Contact;

public class PhysicsCharacter implements PhysicsObject {	
	public PhysicsCharacter() {
	}

	@Override
	public void BeginContactHandler(PhysicsDataStructure struct, Contact contact) {
		
	}

	@Override
	public void EndContactHandler(PhysicsDataStructure struct, Contact contact) {
		// TODO Auto-generated method stub

	}

	@Override
	public void PreContactHandler(PhysicsDataStructure b, Contact contact) {
		// TODO Auto-generated method stub

	}	
}
