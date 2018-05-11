package com.zergwar.notui;

import java.awt.Graphics2D;

public class NotUIParticleShield extends NotUIParticle
{
	public NotUIParticleShield(float x, float y) {
		super(60, x, y);
	}
	
	@Override
	public void tick() {
		super.tick();
		setY(getY() - .3f);
	}
	
	@Override
	public void renderParticle(Graphics2D g)
	{
		g.drawImage(NotUIUtils.loadTex("shield.png"), (int)getX() - 12, (int)getY() - 12, 24, 24, null);
	}
}
